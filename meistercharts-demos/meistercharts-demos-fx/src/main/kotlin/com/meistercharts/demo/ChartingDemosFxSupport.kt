/**
 * Copyright 2023 Neckar IT GmbH, Mössingen, Germany
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.meistercharts.demo

import com.google.common.collect.ImmutableList
import com.meistercharts.algorithms.axis.AxisOrientationX
import com.meistercharts.algorithms.axis.AxisOrientationY
import com.meistercharts.algorithms.environment
import com.meistercharts.algorithms.layers.HideAfterTimeoutLayer
import com.meistercharts.algorithms.layers.Layer
import com.meistercharts.algorithms.layers.LayerVisibilityAdapter
import com.meistercharts.algorithms.layers.debug.EventsDebugLayer
import com.meistercharts.algorithms.layers.debug.FramesPerSecondLayer
import com.meistercharts.algorithms.layers.debug.MarkAsDirtyLayer
import com.meistercharts.algorithms.layers.debug.PaintPerformanceLayer
import com.meistercharts.algorithms.layers.debug.WhatsAtDebugLayer
import com.meistercharts.algorithms.layers.debug.WindowDebugLayer
import com.meistercharts.algorithms.layers.visibleIf
import com.meistercharts.algorithms.mainScreenDevicePixelRatio
import com.meistercharts.algorithms.tile.GlobalTilesCache
import com.meistercharts.canvas.DebugFeature
import com.meistercharts.canvas.DirtySupport
import com.meistercharts.canvas.debug
import com.meistercharts.canvas.i18nSupport
import com.meistercharts.canvas.pixelSnapSupport
import com.meistercharts.canvas.registerDirtyListener
import com.meistercharts.demo.layer.DumpPaintingVariablesLayer
import com.meistercharts.design.DebugDesign
import com.meistercharts.design.NeckarITDesign
import com.meistercharts.design.SegoeUiDesign
import com.meistercharts.design.corporateDesign
import com.meistercharts.design.initCorporateDesign
import com.meistercharts.fx.MeisterChartBuilderFX
import com.meistercharts.fx.MeisterChartFX
import com.meistercharts.fx.MeisterChartsPlatform
import com.meistercharts.fx.binding.toJavaFx
import com.meistercharts.fx.debug.ChartingStateDebugPane
import com.meistercharts.fx.debug.MeisterChartsMemoryLeakDetector
import com.meistercharts.version.MeisterChartsVersion
import it.neckar.commons.logback.LogbackConfigurer
import it.neckar.commons.logback.debug
import it.neckar.commons.logback.level
import it.neckar.logging.Logger
import it.neckar.logging.LoggerDelegate
import it.neckar.logging.LoggerFactory
import it.neckar.open.annotations.NonUiThread
import it.neckar.open.collections.cacheStatsHandler
import it.neckar.open.formatting.decimalFormat
import it.neckar.open.formatting.percentageFormat2digits
import it.neckar.open.i18n.Locale
import it.neckar.open.javafx.Components
import it.neckar.open.javafx.JavaFxTimer
import it.neckar.open.javafx.consumeImmediately
import it.neckar.open.javafx.inScrollPane
import it.neckar.open.javafx.map
import it.neckar.open.javafx.toRGBHex
import it.neckar.open.kotlin.lang.ceil
import it.neckar.open.kotlin.lang.enumEntries
import it.neckar.open.observable.DependentObjects
import it.neckar.open.observable.ObservableBoolean
import it.neckar.open.observable.ObservableDouble
import it.neckar.open.observable.ObservableObject
import it.neckar.open.resources.getResourceSafe
import it.neckar.open.time.TimeZone
import javafx.application.Application
import javafx.application.Platform
import javafx.beans.binding.Bindings
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import javafx.collections.ListChangeListener
import javafx.collections.transformation.FilteredList
import javafx.embed.swing.SwingFXUtils
import javafx.geometry.Insets
import javafx.geometry.Orientation
import javafx.scene.Node
import javafx.scene.Scene
import javafx.scene.SnapshotParameters
import javafx.scene.control.Label
import javafx.scene.control.SplitPane
import javafx.scene.control.TextField
import javafx.scene.control.Tooltip
import javafx.scene.control.TreeCell
import javafx.scene.control.TreeItem
import javafx.scene.control.TreeView
import javafx.scene.image.WritableImage
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.scene.web.WebView
import javafx.stage.Screen
import javafx.stage.Stage
import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import org.commonmark.ext.gfm.tables.TablesExtension
import org.commonmark.parser.Parser
import org.commonmark.renderer.html.HtmlRenderer
import org.tbee.javafx.scene.layout.MigPane
import java.io.File
import java.util.prefs.Preferences
import javax.imageio.ImageIO
import kotlin.time.Duration.Companion.seconds

/**
 * Helper class that creates the Charting Demos FX
 */
class ChartingDemosFxSupport(val demoDescriptors: List<ChartingDemoDescriptor<*>>) {
  init {
    DemoMessages.registerEnumTranslator()
  }

  private val decimalFormat = decimalFormat(4, 0, 1, true)

  private val model = ChartingDemosModel()

  /**
   * Holds the current MeisterCharts reference - used to call dispose on demo change
   */
  private var currentMeisterChart: MeisterChartFX? = null


  fun start(primaryStage: Stage, parameters: Application.Parameters) {
    LogbackConfigurer.configureLoggingConsoleOnly(org.slf4j.event.Level.DEBUG)

    val parser = ArgParser(this::class.simpleName ?: "???")
    val onScreen by parser.option(ArgType.Int, description = "Screen index")
    parser.parse(parameters.raw.toTypedArray())
    logger.info("Starting main frame on screen $onScreen")

    val preferences = Preferences.userNodeForPackage(ChartingDemosFX::class.java)
    val lastPredefinedConfigIndex = preferences.getInt("lastDemoDescriptorConfigIndex", -1)

    //The last demo descriptor that has been loaded from the preferences
    val lastDemoDescriptorFromPrefs: String? = preferences.get("lastDemoDescriptor", null)

    model.currentDescriptorWithConfigProperty.consumeImmediately {
      if (it != null) {
        preferences.put("lastDemoDescriptor", it.demoDescriptor::class.qualifiedName)

        val index = it.predefinedConfiguration?.let { predefinedConfig ->
          it.demoDescriptor.predefinedConfigurations.indexOf(predefinedConfig)
        } ?: -1

        preferences.putInt("lastDemoDescriptorConfigIndex", index)
      }
    }

    MeisterChartsPlatform.init()
    cacheStatsHandler = CollectingCacheStatsHandler()

    //Disable mark as dirty messages
    LoggerFactory.getLogger(DirtySupport::class).level = org.slf4j.event.Level.INFO

    logger.debug {
      "MeisterCharts version: ${MeisterChartsVersion.versionAsStringVerbose}"
    }

    primaryStage.title = "MeisterCharts Demos"


    //Holds the canvas pane
    val demoHolder = StackPane()
    model.currentDescriptorWithConfigProperty.consumeImmediately { demoDescriptorWithConfig ->
      demoHolder.children.clear()

      //Dispose the current MeisterCharts instance if there is one
      currentMeisterChart?.dispose()
      currentMeisterChart = null

      //Add the demo - if there is one
      if (demoDescriptorWithConfig != null) {
        demoHolder.children.add(createDemoContent(demoDescriptorWithConfig.demoDescriptor, demoDescriptorWithConfig.predefinedConfiguration))
      }
    }

    val treeView = createNavigation()

    treeView.selectionModel.selectedItems.addListener(ListChangeListener {
      val selectedItem = treeView.selectionModel.selectedItem ?: return@ListChangeListener
      val descriptor = selectedItem.value.descriptor ?: return@ListChangeListener

      model.currentDescriptorWithConfig = DemoDescriptorWithPredefinedConfiguration(descriptor, selectedItem.value.configuration)

      //Expand the selected item to make the sub configs visible
      selectedItem.expandedProperty().value = true
    })

    //Select the descriptor
    model.currentDescriptorWithConfigProperty.addListener { _, oldValue, newValue ->
      if (newValue == null) {
        return@addListener
      }

      treeView.root.children.forEach { treeItem ->
        val found = find(treeItem, newValue.demoDescriptor, newValue.predefinedConfiguration)
        if (found != null) {
          found.parent?.parent?.expandedProperty()?.set(true)
          found.parent?.expandedProperty()?.set(true)
          found.expandedProperty().set(true)
          val row = treeView.getRow(found)
          treeView.selectionModel.select(row)

          //Only scroll to visible for the first time
          if (oldValue == null) {
            treeView.scrollTo(row)
          }
        }
      }
    }


    //Update the selection based on the prefs
    lastDemoDescriptorFromPrefs?.let {
      try {
        fun TreeItem<TreeObject>.selectIfMatchesRecursively(): Boolean {
          val descriptor = this.value?.descriptor

          if (descriptor != null) {
            if (descriptor::class.qualifiedName == lastDemoDescriptorFromPrefs) {
              val predefinedConfig = descriptor.predefinedConfigurations.getOrNull(lastPredefinedConfigIndex)
              model.currentDescriptorWithConfig = DemoDescriptorWithPredefinedConfiguration(descriptor, predefinedConfig)
              return true
            }
          }

          this.children.forEach { treeItem ->
            val found = treeItem.selectIfMatchesRecursively()
            if (found) {
              return true
            }
          }

          return false
        }

        treeView.root.selectIfMatchesRecursively()

      } catch (e: Exception) {
        logger.error("Could not load demo <$it> due to ${e.message}")
      }
    }


    val filterText = TextField()
    model.filterTextProperty.bindBidirectional(filterText.textProperty())

    val root = SplitPane()
    root.stylesheets.add(javaClass.getResourceSafe("ChartingDemo.css").toExternalForm())
    root.styleClass.add("demo-root-pane")

    Components.vbox5(Components.hbox5(filterText, Components.button("Clr") {
      filterText.text = ""
    }), Components.button("Exp") {
      treeView.root.children.forEach { treeItem ->
        treeItem.isExpanded = true
      }
    }, treeView).let {
      VBox.setVgrow(treeView, Priority.ALWAYS)
      HBox.setHgrow(filterText, Priority.ALWAYS)

      root.items.add(it)
    }

    root.items.add(demoHolder)
    Platform.runLater {
      root.setDividerPositions(0.12)
    }

    root.style = "-fx-background-color: SILVER;"
    root.padding = Insets(10.0)

    primaryStage.scene = Scene(root)

    primaryStage.width = preferences.getDouble("window.width", 1680.0).coerceAtLeast(800.0)
    primaryStage.height = preferences.getDouble("window.height", 1024.0).coerceAtLeast(600.0)


    primaryStage.widthProperty().consumeImmediately {
      preferences.putDouble("window.width", it.toDouble())
    }
    primaryStage.heightProperty().consumeImmediately {
      preferences.putDouble("window.height", it.toDouble())
    }

    //Set the location ot the requested screen
    onScreen?.let { it ->
      val screens = Screen.getScreens()
      logger.debug("Available screens:")
      screens.forEach {
        val visualBounds = it.visualBounds
        logger.debug("* ${visualBounds.width}/${visualBounds.height} @ ${visualBounds.minX}/${visualBounds.minY}")
      }

      require(screens.size > it) {
        buildString {
          append("Invalid onScreen <$it>. Only these screen available:\n")
          append(screens.joinToString("\n") {
            val visualBounds = it.visualBounds
            "* ${visualBounds.width}/${visualBounds.height} @ ${visualBounds.minX}/${visualBounds.minY}"
          })
          append(" ")
        }
      }

      val screen = screens[it]
      primaryStage.x = screen.visualBounds.minX
      primaryStage.y = screen.visualBounds.minY

      logger.debug("Placing on screen $it with these bounds: ${screen.visualBounds}")
    }

    primaryStage.show()
  }

  fun stop() {
    MeisterChartsMemoryLeakDetector.dispose()
  }

  /**
   * Selects the tree item if it matches the given config
   */
  fun find(treeItem: TreeItem<TreeObject>, demoDescriptor: ChartingDemoDescriptor<*>, predefinedConfiguration: PredefinedConfiguration<*>?): TreeItem<TreeObject>? {
    val value = treeItem.value
    val descriptor = value?.descriptor
    if (descriptor != null && descriptor::class == demoDescriptor::class) {

      if (predefinedConfiguration != null) {
        if (predefinedConfiguration == treeItem.value.configuration) {
          return treeItem
        }
      } else {
        return treeItem
      }
    }

    treeItem.children.forEach {
      val found = find(it, demoDescriptor, predefinedConfiguration)
      if (found != null) {
        return found
      }
    }

    return null
  }


  /**
   * Prints the cache report
   */
  private fun printCacheReport(statsHandler: CollectingCacheStatsHandler = cacheStatsHandler as CollectingCacheStatsHandler) {
    logger.info("---------- Cache Report ---------------")

    statsHandler.caches.backingMap.forEach { cache, description ->
      logger.info(description + "(${cache.size} / ${cache.maxSize})")

      val hitPercentage = when {
        cache.cacheMissCounter == 0 -> 1.0
        cache.cacheHitCounter == 0 -> 0.0
        else -> 1.0 / cache.cacheMissCounter * cache.cacheHitCounter
      }
      logger.info("\t${percentageFormat2digits.format(hitPercentage)} - (\uD83D\uDC4E ${cache.cacheMissCounter} / \uD83D\uDC4D ${cache.cacheHitCounter})")
    }
  }

  /**
   * Creates a tab
   */
  @CalledForEachDemo
  private fun createDemoContent(descriptor: ChartingDemoDescriptor<*>, configuration: PredefinedConfiguration<*>?): Node {
    val demo = descriptor.createDemo(configuration as PredefinedConfiguration<Nothing>?)

    val demoContent = demo.createContent(descriptor)
    //Store the demo to avoid premature gc
    demoContent.properties["demo"] = demo

    return demoContent
  }

  /**
   * Creates the configuration pane.
   * This method is called for each demo!
   */
  @CalledForEachDemo
  private fun createConfigurationPane(meisterChart: MeisterChartFX, demo: ChartingDemo, canvasNodeProvider: () -> MeisterChartFX): Node {
    val layerSupport = meisterChart.layerSupport
    val chartSupport = layerSupport.chartSupport

    val pane = MigPane("fillx", "[][grow,left, fill]", "")
    pane.add(Components.headline1("Configuration/State"), "span")

    pane.add(Components.label("Mouse Location"))
    pane.add(Components.label(layerSupport.mouseEvents.mousePositionProperty.toJavaFx().map { it?.format() ?: "-" }), "span")

    pane.add(Components.label("Zoom"))
    pane.add(Components.label(layerSupport.chartSupport.rootChartState.zoomProperty.toJavaFx().map { it.format() }), "span")
    pane.add(Components.label("Translation"))
    pane.add(Components.label(layerSupport.chartSupport.rootChartState.windowTranslationProperty.toJavaFx().map { it.format() }), "span")

    pane.add(Components.label("Window Size"))
    pane.add(Components.label(layerSupport.chartSupport.rootChartState.windowSizeProperty.toJavaFx().map { it.format() }), "split 2, span")
    pane.add(Components.label(layerSupport.chartSupport.canvas.chartSizeClassificationProperty.toJavaFx().map { "${it.width.name} / ${it.height.name}" }), "")

    pane.add(Components.label("Content Area Size"))
    pane.add(Components.label(layerSupport.chartSupport.rootChartState.contentAreaSizeProperty.toJavaFx().map { it.format() }), "span")

    pane.add(Components.label("Y Axis Orientation"))
    pane.add(Components.hbox5(Components.comboBox(layerSupport.chartSupport.rootChartState.axisOrientationYProperty.toJavaFx(), AxisOrientationY.entries).also { it.cellFactory }), "span")

    pane.add(Components.label("X Axis Orientation"))
    pane.add(Components.hbox5(Components.comboBox(layerSupport.chartSupport.rootChartState.axisOrientationXProperty.toJavaFx(), AxisOrientationX.entries)), "span")

    pane.add(Components.label("Snap Configuration"))
    pane.add(Components.hbox5(Components.comboBox(layerSupport.chartSupport.pixelSnapSupport.snapConfigurationProperty.toJavaFx(), enumEntries())), "span")

    pane.add(Components.label("Global Tiles Cache"))
    pane.add(Components.label(
      SimpleStringProperty("-").also { property ->
        JavaFxTimer.repeat(1.seconds) {
          property.value = "Size: ${GlobalTilesCache.size}"
        }
      }
    ), "span")

    pane.add(Components.button("Clear Global Tiles Cache") {
      GlobalTilesCache.clearAll()
    }, "span")

    pane.add(Components.label("Background"))
    val backgroundColors = listOf(Color.WHITE, Color.RED, Color.GRAY, Color.BLACK)
    val backgroundProperty = SimpleObjectProperty(backgroundColors[0])
    meisterChart.canvasHolder.styleProperty().bind(backgroundProperty.map {
      "-fx-background-color: ${it.toRGBHex()};"
    })
    pane.add(Components.hbox5(Components.comboBox(backgroundProperty, backgroundColors) { "$it" }), "span")

    val textLocale: ObservableObject<Locale> = ObservableObject(chartSupport.i18nSupport.textLocale)
    val formatLocale: ObservableObject<Locale> = ObservableObject(chartSupport.i18nSupport.formatLocale)
    val timeZone: ObservableObject<TimeZone> = ObservableObject(chartSupport.i18nSupport.timeZone)

    //Avoid premature garbage collection
    pane.properties["textLocale"] = textLocale
    pane.properties["formatLocale"] = formatLocale
    pane.properties["timeZone"] = timeZone

    textLocale.consume {
      chartSupport.i18nSupport.textLocale = it
      layerSupport.markAsDirty()
    }

    formatLocale.consume {
      chartSupport.i18nSupport.formatLocale = it
      layerSupport.markAsDirty()
    }

    timeZone.consume {
      chartSupport.i18nSupport.timeZone = it
      layerSupport.markAsDirty()
    }

    pane.add(Components.label("Corporate Design"))
    val corporateDesignConfig = ObservableObject(corporateDesign).also {
      it.consumeImmediately { corporateDesign ->
        initCorporateDesign(corporateDesign)
        layerSupport.markAsDirty()
      }
    }
    //Avoid premature garbage collection
    pane.properties["corporateDesignConfig"] = corporateDesignConfig


    val comboBox = Components.comboBox(corporateDesignConfig.toJavaFx(), listOf(NeckarITDesign, SegoeUiDesign, DebugDesign)) { it.id }
    pane.add(comboBox, "span")

    pane.add(Components.label("Device Pixel Ratio"))
    val devicePixelRatioProperty = ObservableDouble(environment.devicePixelRatio).also {
      it.consume { value ->
        mainScreenDevicePixelRatio = value
        layerSupport.markAsDirty()
      }
    }
    val slider = Components.slider(devicePixelRatioProperty.toJavaFx(), 0.25, 2.0, 0.1).also {
      it.isSnapToTicks = true
      it.isShowTickLabels = true
      it.isShowTickMarks = true
    }
    pane.add(Components.hbox5(slider, Components.label(devicePixelRatioProperty.map { decimalFormat.format(it) }.toJavaFx())), "span")

    pane.add(Components.button("Set to 1.0") {
      devicePixelRatioProperty.value = 1.0
      layerSupport.markAsDirty()
    }, "skip 1, split 2, span")

    val initialMainScreenDevicePixelRatio = mainScreenDevicePixelRatio
    pane.add(Components.button("Set to Device Pixel Ratio") {
      devicePixelRatioProperty.value = initialMainScreenDevicePixelRatio
      layerSupport.markAsDirty()
    }, "span")

    pane.add(Components.label("Text Locale"))
    pane.add(Components.hbox5(Components.comboBox(textLocale.toJavaFx(), listOf(Locale.US, Locale.Germany, Locale("hu-HU"))) {
      it.locale
    }), "span")

    pane.add(Components.label("Format Locale"))
    pane.add(Components.hbox5(Components.comboBox(formatLocale.toJavaFx(), listOf(Locale.US, Locale.Germany, Locale("hu-HU"))) {
      it.locale
    }), "span")

    pane.add(Components.label("Time zone"))
    pane.add(Components.hbox5(Components.comboBox(timeZone.toJavaFx(), listOf(TimeZone.UTC, TimeZone.Berlin, TimeZone.Tokyo, TimeZone("America/Chicago"))) {
      it.zoneId
    }), "span")

    pane.add(Components.headline1("Debug Features"), "span, gaptop 15px")

    DebugFeature.entries.map { debugFeature ->
      val debugModeEnabled = ObservableBoolean().apply {
        consume {
          layerSupport.apply {
            this.debug.set(debugFeature, it)
            this.markAsDirty()
          }
        }
      }

      //Listener that is notified when the debug config changes (e.g. through the ToggleDebuggingModeLayer)
      meisterChart.chartSupport.debug.enabledFeaturesProperty.consume {
        debugModeEnabled.value = it.contains(debugFeature)
      }

      val checkBox = Components.checkBox(debugFeature.name, debugModeEnabled.toJavaFx())
      checkBox.properties["observableBoolean"] = debugModeEnabled

      pane.add(checkBox, "span")
      checkBox
    }

    pane.add(Components.button("Toggle All") {
      chartSupport.debug.toggle()
    }, "span")


    pane.add(Components.headline1("Debug Tools"), "span, gaptop 15px")

    pane.add(Components.button("Open Debug View") { _ ->
      ChartingStateDebugPane.show(layerSupport.chartSupport.rootChartState).let {
        //Close the stage when the demo is changed
        layerSupport.chartSupport.onDispose(it)
      }
    }, "span")

    pane.add(Components.button("Repaint") {
      layerSupport.markAsDirty()
    }, "span")

    pane.add(Components.button("Dump Layers") { _ ->
      logger.info("Layers:")
      layerSupport.layers.layers.forEach {
        logger.info("\t${it.extendedToString()}")
      }
    }, "span")

    pane.add(Components.button("Dump Cache State") {
      printCacheReport()
    }, "span")

    pane.add(Components.button("Screenshot") {
      val canvasNode = canvasNodeProvider()

      val writableImage = WritableImage(canvasNode.width.ceil().toInt(), canvasNode.height.ceil().toInt())
      canvasNode.snapshot(SnapshotParameters(), writableImage)

      ImageIO.write(SwingFXUtils.fromFXImage(writableImage, null), "png", File("/tmp/snapshot.png"))
    }, "span")


    val repaintPerformanceLayerVisible = ObservableBoolean(meisterChart.layerSupport.recordPaintStatistics)
    repaintPerformanceLayerVisible.consumeImmediately {
      meisterChart.layerSupport.recordPaintStatistics = it
    }

    repaintPerformanceLayerVisible.registerDirtyListener(layerSupport)
    meisterChart.layerSupport.layers.addLayer(PaintPerformanceLayer().visibleIf(repaintPerformanceLayerVisible))
    meisterChart.layerSupport.layers.addLayer(FramesPerSecondLayer().visibleIf(repaintPerformanceLayerVisible))
    pane.add(Components.checkBox("Performance Layer", repaintPerformanceLayerVisible.toJavaFx()), "span")

    val forceRepaint = ObservableBoolean()
    meisterChart.layerSupport.layers.addLayer(MarkAsDirtyLayer().visibleIf(forceRepaint))
    forceRepaint.consume {
      meisterChart.layerSupport.markAsDirty()
    }
    pane.add(Components.checkBox("Always Repaint", forceRepaint.toJavaFx()), "span")

    val whatsAtDebugLayerVisible = ObservableBoolean().also {
      it.consume {
        layerSupport.markAsDirty()
      }
    }
    meisterChart.layerSupport.layers.addLayer(WhatsAtDebugLayer().visibleIf(whatsAtDebugLayerVisible))
    pane.add(Components.checkBox("Whats At Debug", whatsAtDebugLayerVisible.toJavaFx()), "span")

    val windowDebugLayerVisible = ObservableBoolean().also {
      it.consume {
        layerSupport.markAsDirty()
      }
    }
    meisterChart.layerSupport.layers.addLayer(WindowDebugLayer().visibleIf(windowDebugLayerVisible))
    pane.add(Components.checkBox("Window Debug", windowDebugLayerVisible.toJavaFx()), "span")

    val paintingVariablesDebugLayerVisible = ObservableBoolean().also {
      it.consume {
        layerSupport.markAsDirty()
      }
    }
    meisterChart.layerSupport.layers.addLayer(DumpPaintingVariablesLayer().visibleIf(paintingVariablesDebugLayerVisible))
    pane.add(Components.checkBox("PaintingVariables Debug", paintingVariablesDebugLayerVisible.toJavaFx()), "span")

    meisterChart.layerSupport.layers.addLayer(EventsDebugLayer().visibleIf {
      layerSupport.debug[DebugFeature.LogEvents]
    })
    // do not add a check-box here; the feature is activated via the enabledFeaturesProperty

    //Append the provided configuration from the demo itself
    val javaFxDemoConfiguration = DemoConfigurationFX()

    demo.declare(javaFxDemoConfiguration, layerSupport)
    pane.add(javaFxDemoConfiguration.pane, "span, gaptop 15px")

    return pane
  }

  /**
   * To avoid premature garbage collection
   */
  private val globalDependentObjects: DependentObjects = DependentObjects()

  @CalledOnce
  private fun createNavigation(): TreeView<TreeObject> {
    val byCategory = demoDescriptors.toMutableList().groupBy {
      it.category
    }.toSortedMap()

    val treeRoot = TreeItem<TreeObject>().apply {
      byCategory.forEach { (category, descriptors) ->

        val childrenBackend = FXCollections.observableArrayList<TreeItem<TreeObject>>()

        descriptors
          .sortedBy {
            it.name
          }
          .forEach { descriptor ->

            val defaultConfigTreeItem = TreeItem(TreeObject.createDemo(descriptor, descriptor.defaultPredefinedConfig))
            childrenBackend.add(defaultConfigTreeItem)

            defaultConfigTreeItem.children.addAll(
              descriptor.predefinedConfigurations
                .drop(1)
                .map {
                  TreeItem(TreeObject.createConfig(descriptor, it))
                }
            )
          }

        val filteredChildren: FilteredList<TreeItem<TreeObject>> = FilteredList(childrenBackend)
        filteredChildren.predicateProperty().bind(model.filterPredicateProperty)
        globalDependentObjects.addDependentObject(filteredChildren)

        val categoryTreeItem = TreeItem(TreeObject.create(category))

        Bindings.bindContent(categoryTreeItem.children, filteredChildren)
        this.children.add(categoryTreeItem)
      }
    }

    //Expand the first three rows
    treeRoot.isExpanded = true
    treeRoot.children.take(4).forEach {
      it.isExpanded = true
    }

    return TreeView(treeRoot).apply {
      isShowRoot = false
      setCellFactory {
        ChartingDemoDescriptorCell()
      }
    }
  }

  @CalledForEachDemo
  private fun ChartingDemo.createContent(descriptor: ChartingDemoDescriptor<*>): Node {
    val meisterChart = MeisterChartBuilderFX(descriptor.name)
      .also {
        it.configure()
      }.build()

    MeisterChartsMemoryLeakDetector.monitor(meisterChart)
    currentMeisterChart = meisterChart

    val horizontalSplitPane = SplitPane()
    horizontalSplitPane.items.add(meisterChart)
    horizontalSplitPane.items.add(createConfigurationPane(meisterChart, this) { meisterChart }.inScrollPane())
    Platform.runLater {
      horizontalSplitPane.setDividerPositions(0.8)
    }

    val verticalSplitPane = SplitPane()
    verticalSplitPane.orientation = Orientation.VERTICAL
    verticalSplitPane.items.add(horizontalSplitPane)
    verticalSplitPane.items.add(descriptor.createDescriptionArea())
    verticalSplitPane.setDividerPositions(0.8)


    return verticalSplitPane
  }

  companion object {
    private val logger: Logger by LoggerDelegate()
  }
}

private fun Layer.extendedToString(): String {
  if (this is LayerVisibilityAdapter<*>) {
    return "VisibilityAdapter: ${this.delegate.extendedToString()}"
  }

  if (this is HideAfterTimeoutLayer<*>) {
    return "HideAfterTimeout: ${this.delegate.extendedToString()}"
  }

  return "${javaClass.name}($type)"
}

@CalledForEachDemo
private fun ChartingDemoDescriptor<*>.createDescriptionArea(): Node {
  return VBox(
    WebView().also {
      //it.engine.loadContent(markdown2html(description))
      it.engine.loadContent(description)
      VBox.setVgrow(it, Priority.ALWAYS)
    },
    Label(this::class.simpleName).also {
      VBox.setVgrow(it, Priority.NEVER)
    }
  )
}

/**
 * Cell for the charting demo descriptor
 */
internal class ChartingDemoDescriptorCell : TreeCell<TreeObject>() {
  override fun updateItem(item: TreeObject?, empty: Boolean) {
    super.updateItem(item, empty)
    text = item?.let {
      when (it.type) {
        TreeNodeType.Category -> requireNotNull(it.category).name
        TreeNodeType.Demo -> {
          requireNotNull(it.descriptor).createFullDemoDescription(it.configuration)
        }

        TreeNodeType.PredefinedConfig -> requireNotNull(it.configuration).description
      }
    }.orEmpty()

    tooltip = Tooltip(text)
  }
}

/**
 * Converts markdown to html
 */
@NonUiThread
fun markdown2html(markdown: String): String {
  val parser = Parser.builder().extensions(ImmutableList.of(TablesExtension.create())).build()
  val document = parser.parse(markdown)

  val renderer = HtmlRenderer.builder()
    //Escaping HTML to avoid problems with invalid XHTML and the XHTMLPanel
    .escapeHtml(true)
    .extensions(ImmutableList.of(TablesExtension.create())).build()
  return renderer.render(document)
}


/**
 * Every method annotated with this annotation is called for each demo
 */
annotation class CalledForEachDemo

annotation class CalledOnce

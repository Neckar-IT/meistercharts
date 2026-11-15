/*
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
@file:Suppress("RETURN_VALUE_NOT_USED") // Demo-scope subscriptions tied to the demo lifetime

package com.meistercharts.demojs

import com.meistercharts.algorithms.layers.PaintingPropertyKey
import com.meistercharts.algorithms.layers.debug.ContentAreaDebugLayer
import com.meistercharts.algorithms.layers.debug.ContentViewportDebugLayer
import com.meistercharts.algorithms.layers.debug.EventsDebugLayer
import com.meistercharts.algorithms.layers.debug.FramesPerSecondLayer
import com.meistercharts.algorithms.layers.debug.MarkAsDirtyLayer
import com.meistercharts.algorithms.layers.debug.PaintPerformanceLayer
import com.meistercharts.algorithms.layers.debug.WindowDebugLayer
import com.meistercharts.algorithms.layers.visibleIf
import com.meistercharts.algorithms.layers.visibleIfWithState
import com.meistercharts.canvas.DebugFeature
import com.meistercharts.canvas.DirtyReason
import com.meistercharts.canvas.SnapConfiguration
import com.meistercharts.canvas.debug
import com.meistercharts.canvas.devicePixelRatioSupport
import com.meistercharts.canvas.i18nSupport
import com.meistercharts.canvas.layer.registerDirtyListener
import com.meistercharts.canvas.paintingProperties
import com.meistercharts.canvas.pixelSnapSupport
import com.meistercharts.canvas.theme
import com.meistercharts.canvas.themeSupport
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoDescriptors
import com.meistercharts.demo.DemoRunConfiguration
import com.meistercharts.demo.MeisterchartDemoCssConstants
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.RunnableDemo
import com.meistercharts.demo.createFullDemoName
import com.meistercharts.demo.descriptors.history.TimeLineChartGestaltWithToolbarDemoDescriptor
import com.meistercharts.demo.layer.DumpPaintingVariablesLayer
import com.meistercharts.demo.registerVirtualNowHandlerForDemos
import com.meistercharts.design.DarkDesign
import com.meistercharts.design.DebugTheme
import com.meistercharts.design.DefaultTheme
import com.meistercharts.design.NeckarITDesign
import com.meistercharts.design.SegoeUiTheme
import com.meistercharts.design.Theme
import com.meistercharts.design.ThemeContext
import com.meistercharts.design.setDefaultTheme
import com.meistercharts.js.CanvasFontMetricsCalculatorJS
import com.meistercharts.js.MeisterChartsPlatform
import com.meistercharts.js.MeisterchartBuilderJS
import com.meistercharts.js.MeisterchartJS
import com.meistercharts.version.MeisterChartsVersion
import it.neckar.datetime.minimal.TimeZone
import it.neckar.geometry.AxisOrientationX
import it.neckar.geometry.AxisOrientationY
import it.neckar.logging.LoggerFactory
import it.neckar.logging.debug
import it.neckar.logging.info
import it.neckar.open.addElement
import it.neckar.open.collections.fastForEachIndexed
import it.neckar.open.i18n.Locale
import it.neckar.open.observable.ObservableBoolean
import it.neckar.open.observable.ObservableDouble
import it.neckar.open.observable.ObservableObject
import kotlinx.browser.document
import kotlinx.browser.localStorage
import kotlinx.browser.window
import kotlinx.dom.clear
import org.w3c.dom.CustomEvent
import org.w3c.dom.CustomEventInit
import org.w3c.dom.Element
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.HTMLTableElement
import org.w3c.dom.get
import org.w3c.dom.set
import kotlin.uuid.Uuid

/**
 * Can start and display demos
 */
class MeisterchartsDemosJS(
  /**
   * Contains the navigation (left side)
   */
  val navigationContainer: HTMLElement,
  /**
   * Contains meistercharts (center)
   */
  val meisterchartsContainer: HTMLElement,
  /**
   * Contains the description (bottom)
   */
  val descriptionContainer: HTMLElement,
  /**
   * Contains the configuration pane (right side)
   */
  val configurationPaneContainer: HTMLElement,


  /**
   * The demo run configuration
   */
  val demoRunConfiguration: DemoRunConfiguration,

  val allDemoDescriptors: List<MeisterchartsDemoDescriptor<*>>,
) {

  /**
   * Holds the current MeisterCharts reference - backing field
   */
  private var _currentMeisterChart: MeisterchartJS? = null

  /**
   * Returns the current MeisterCharts instance or throws an exception
   */
  val currentMeisterChart: MeisterchartJS
    get() {
      return _currentMeisterChart ?: throw IllegalStateException("No current MeisterChart available")
    }

  init {
    logger.debug {
      "MeisterCharts version: ${MeisterChartsVersion.versionAsStringVerbose}"
    }

    initConsoleLogFunctions("meistercharts")
    MeisterChartsPlatform.init()

    updateVisibilityOfPanes()

    createNavigationPane()
  }

  private fun clearCurrentDemo() {
    //Dispose the current MeisterCharts instance if there is one
    _currentMeisterChart?.dispose()
    _currentMeisterChart = null

    meisterchartsContainer.clear()
    descriptionContainer.clear()
    configurationPaneContainer.clear()
  }

  /**
   * Starts a single demo that matches the given search terms
   */
  fun startDemoByUuid(uuidFragment: String?) {
    if (uuidFragment == null) { // uuid is null when starting demos -> Fallback must be displayed and no exception should be thrown.
      val runnableDemo = run {
        //no match -> fall back to TimeLineChartGestaltWithToolbarDemoDescriptor
        val demoDescriptor = TimeLineChartGestaltWithToolbarDemoDescriptor()
        val predefinedConfiguration = demoDescriptor.predefinedConfigurations.firstOrNull()
        logger.info("Starting default demo")
        RunnableDemo(demoDescriptor, predefinedConfiguration)
      }
      startDemo(runnableDemo)
    } else {
      // checks if the demo descriptor has predefined configurations. If so a different exception must be thrown.
      @Suppress("DEPRECATION")
      val demoDescriptor = DemoDescriptors.findDescriptor(Uuid.parse(uuidFragment))
      if (demoDescriptor != null && demoDescriptor.predefinedConfigurations.isNotEmpty()) {
        throw IllegalStateException("Demo with the UUID: <$uuidFragment> has predefined configurations!. The demo is not runnable, please use the uuid of one of the predefined configurations instead! UUID of first predefined Configuration: <${demoDescriptor.predefinedConfigurations.first().uuid}>")
      }

      val runnableDemo: RunnableDemo = DemoDescriptors.byUuidFragment(uuidFragment) ?: throw IllegalStateException("No runnable Demo found for uuid: $uuidFragment") // when uuid can not be found -> Exception is thrown.
      startDemo(runnableDemo)
    }

  }

  /**
   * Starts the given demo
   */
  fun startDemo(runnableDemo: RunnableDemo) {
    clearCurrentDemo()

    val descriptor: MeisterchartsDemoDescriptor<*> = runnableDemo.demoDescriptor
    val configuration: PredefinedConfiguration<*>? = runnableDemo.configuration

    logger.info {
      "Starting demo: ${descriptor.createFullDemoName(configuration)} (${descriptor.uuid})"
    }

    val demo = descriptor.createDemo(configuration as PredefinedConfiguration<Nothing>?)

    demo.registerVirtualNowHandlerForDemos(demoRunConfiguration.demoVirtualTimeHandler) {
      println("######### DEMO PAUSED ############# Virtual Time stopped ticking")

      // repaint the demo to get the actual screenshot at the correct time
      demo.markAsDirty(DirtyReason.VirtualTimePaused)

      //Notify the outside world that the virtual time has stopped ticking, used for E2E tests
      document.dispatchEvent(
        CustomEvent(
          type = "meisterchartsDemo:Paused",
          eventInitDict = CustomEventInit(detail = "Virtual Time paused".toJsString(), bubbles = true, cancelable = false, composed = true)
        )
      )
      requireNotNull(document.body).classList.add(MeisterchartDemoCssConstants.VirtualTimePaused) //add the paused class to the body (for E2E tests)
    }

    val meistercharts = demo.createContent(descriptor)

    meisterchartsContainer.appendChild(meistercharts.holder)

    descriptionContainer.innerHTML = descriptor.description

    initConfigurationPane(demo, meistercharts)
  }

  private fun updateVisibilityOfPanes() {
    descriptionContainer.hidden = demoRunConfiguration.showDescriptionPane.not()
    navigationContainer.hidden = demoRunConfiguration.showNavigationPane.not()
    configurationPaneContainer.hidden = demoRunConfiguration.showConfigurationPane.not()
  }


  /**
   * Creates the content for the demo
   */
  private fun MeisterchartsDemo.createContent(descriptor: MeisterchartsDemoDescriptor<*>): MeisterchartJS {
    val meisterChart = MeisterchartBuilderJS(descriptor.name)
      .also {
        it.configure()
      }.build()

    _currentMeisterChart = meisterChart
    return meisterChart
  }

  /**
   * Creates the content for the navigation pane
   */
  fun createNavigationPane() {
    val navigationPane = (document.createElement("DIV") as HTMLElement).also {
      it.style.display = "flex"
      it.style.flexDirection = "column"
      it.style.height = "100%"
    }
    val navigationTop = (document.createElement("DIV") as HTMLElement).also {
      it.style.flexShrink = "0"
    }
    val navigationTree = (document.createElement("DIV") as HTMLElement).also {
      it.classList.add("navigationTree")
      it.classList.add("overflow-auto", "h-full")
    }

    navigationPane.appendChild(navigationTop)
    navigationPane.appendChild(navigationTree)
    navigationContainer.appendChild(navigationPane)

    val searchField = document.createElement("INPUT") as HTMLInputElement
    searchField.setAttribute("type", "search")
    searchField.setAttribute("name", "nav-search")
    searchField.setAttribute("placeholder", "search")
    searchField.addEventListener("input", {
      hideNavigationButtons(navigationTree, searchField.value)
    })
    searchField.classList.add("searchField")
    searchField.classList.add("border", "border-[#002e46]", "text-[#002e46]", "bg-white", "rounded", "px-3", "py-2", "p-[5px]", "w-full", "box-border")
    navigationTop.appendChild(searchField)


    val demoDescriptorClassNameToRestore = localStorage["lastDemoDescriptor"]
    val demoDescriptorConfigIndexToRestore = localStorage["lastDemoDescriptorConfigIndex"]?.toIntOrNull()

    var lastActiveNavigationButton: HTMLElement? = null
    val demoCreator: (MeisterchartsDemoDescriptor<*>) -> Unit = { descriptor ->
      val predefinedConfigurations = descriptor.predefinedConfigurations.ifEmpty {
        listOf(null)
      }
      logger.debug { "Preparing demo descriptor <${descriptor.name}> with ${predefinedConfigurations.size} configurations" }

      predefinedConfigurations.fastForEachIndexed { index, predefinedConfig ->
        val navigationButton = document.createElement("DIV") as HTMLElement
        navigationButton.classList.add("navButton")
        navigationButton.classList.add("px-[5px]", "py-[5px]", "cursor-pointer", "overflow-hidden", "whitespace-nowrap", "text-ellipsis", "w-[235px]", "hover:text-blue-500")

        descriptor.createFullDemoName(predefinedConfig).let {
          navigationButton.innerText = it
          navigationButton.setAttribute("title", it)
        }

        if (index != 0 && predefinedConfig != null) {
          navigationButton.classList.add("config")
          navigationButton.classList.add("px-[10px]", "py-[5px]", "text-gray-500", "w-[230px]")
        }

        navigationButton.addEventListener("click", {
          //TODO replace with data-active
          lastActiveNavigationButton?.classList?.remove("navButtonActive")
          navigationButton.classList.add("navButtonActive")
          lastActiveNavigationButton = navigationButton
          startDemo(RunnableDemo(descriptor, predefinedConfig))

          localStorage["lastDemoDescriptor"] = descriptor::class.simpleName.orEmpty()
          localStorage["lastDemoDescriptorConfigIndex"] = index.toString()
        })

        //Check if this button should be restored
        if (descriptor::class.simpleName == demoDescriptorClassNameToRestore
          && index == demoDescriptorConfigIndexToRestore
        ) {
          navigationButton.click()
        }

        navigationTree.appendChild(navigationButton)
      }
    }

    val byCategory = allDemoDescriptors
      .filter { demoRunConfiguration.matches(it) }
      .groupBy { it.category }

    DemoCategory.entries.forEach { category ->
      val descriptors = byCategory[category] ?: return@forEach
      val categoryLabel = document.createElement("H5") as HTMLElement
      categoryLabel.classList.add("categoryLabel")
      categoryLabel.classList.add("p-[5px]", "font-bold", "border-b", "border-gray-500")
      categoryLabel.innerText = category.name
      navigationTree.appendChild(categoryLabel)

      descriptors.sortedBy {
        it.name
      }.forEach(demoCreator)
    }
  }


  /**
   * Hide all navigation buttons that do not contain the given [searchFieldValue]
   */
  private fun hideNavigationButtons(navigationElement: Element, searchFieldValue: String) {
    CanvasFontMetricsCalculatorJS.logger.info {
      "hideNavigationButtons: $searchFieldValue"
    }
    for (i in 0 until navigationElement.children.length) {
      val navChildElement = navigationElement.children.item(i)
      if (navChildElement !is HTMLElement) {
        continue
      }
      if (navChildElement.classList.contains("navButton")) {
        if (navChildElement.textContent?.contains(searchFieldValue, true) == true) {
          navChildElement.style.display = "block"
        } else {
          navChildElement.style.display = "none"
        }
      }
    }
  }

  private fun initConfigurationPane(demo: MeisterchartsDemo, meisterChart: MeisterchartJS) {
    val configurationPane = configurationPaneContainer.addElement("div").also {
      it.classList.add("configurationPane")
      it.classList.add("shrink-0", "w-[380px]", "p-[5px_10px]", "border-l", "border-gray-400", "overflow-auto")
    }

    configurationPane.appendChild(createChartStateConfigurationTable(meisterChart))
    configurationPane.appendChild(createDebugOptionsConfigurationTable(meisterChart))
    configurationPane.appendChild(createDebugToolsConfigurationTable(meisterChart))

    val table = createConfigurationTable()
    table.singleColumnRow(document.headline1("Configuration"))
    val htmlDemoConfiguration = DemoConfigurationJS(table)
    demo.declare(htmlDemoConfiguration, meisterChart.layerSupport)

    configurationPane.appendChild(table)
  }

  private fun createConfigurationTable(): HTMLTableElement {
    val table = document.createElement("TABLE") as HTMLTableElement
    table.setAttribute("class", "configurationTable")
    val colgroup = document.createElement("COLGROUP")
    val col1st = document.createElement("COL").apply { setAttribute("style", "width:160px") }
    val col2nd = document.createElement("COL")
    colgroup.appendChild(col1st)
    colgroup.appendChild(col2nd)
    table.appendChild(colgroup)
    return table
  }

  private fun createChartStateConfigurationTable(meisterChart: MeisterchartJS): HTMLTableElement {
    val layerSupport = meisterChart.layerSupport
    val chartSupport = meisterChart.chartSupport

    val textLocale: ObservableObject<Locale> = ObservableObject(chartSupport.i18nSupport.textLocale)
    val formatLocale: ObservableObject<Locale> = ObservableObject(chartSupport.i18nSupport.formatLocale)
    val timeZone: ObservableObject<TimeZone> = ObservableObject(chartSupport.i18nSupport.timeZone)

    textLocale.consume {
      currentMeisterChart.chartSupport.i18nSupport.textLocale = it
      currentMeisterChart.layerSupport.markAsDirty(DirtyReason.ConfigurationChanged)
    }

    formatLocale.consume {
      currentMeisterChart.chartSupport.i18nSupport.formatLocale = it
      currentMeisterChart.layerSupport.markAsDirty(DirtyReason.ConfigurationChanged)
    }

    timeZone.consume {
      currentMeisterChart.chartSupport.i18nSupport.timeZone = it
      currentMeisterChart.layerSupport.markAsDirty(DirtyReason.ConfigurationChanged)
    }


    val defaultThemeConfig: ObservableObject<Theme> = ObservableObject(ThemeContext.defaultValue).also {
      it.consumeImmediately { newDesign ->
        setDefaultTheme(newDesign)
        currentMeisterChart.layerSupport.markAsDirty(DirtyReason.ConfigurationChanged)
      }
    }
    val themeConfig: ObservableObject<Theme> = ObservableObject(chartSupport.theme).also {
      it.consumeImmediately { newDesign ->
        chartSupport.themeSupport.selectedTheme = newDesign
        currentMeisterChart.layerSupport.markAsDirty(DirtyReason.ConfigurationChanged)
      }
    }

    val table = createConfigurationTable()

    table.singleColumnRow(document.headline1("Chart state"))

    table.twoColumnsRow(
      document.label("Zoom"),
      document.label(layerSupport.chartSupport.rootChartState.zoomProperty.map { it.format() }),
    )

    table.twoColumnsRow(
      document.label("Translation"),
      document.label(layerSupport.chartSupport.rootChartState.windowTranslationProperty.map { it.format() }),
    )

    table.twoColumnsRow(
      document.label("Window size"),
      document.label(layerSupport.chartSupport.rootChartState.windowSizeProperty.map { it.format() }),
    )

    table.twoColumnsRow(
      document.label("Content area size"),
      document.label(layerSupport.chartSupport.rootChartState.contentAreaSizeProperty.map { it.format() }),
    )

    table.twoColumnsRow(
      document.label("Sampling Period"),
      document.label("?").also { label ->
        layerSupport.chartSupport.onPaint { _, _, _, _, _ ->
          val samplingPeriod = layerSupport.chartSupport.paintingProperties.retrieveOrNull(PaintingPropertyKey.SamplingPeriod)
          label.textContent = samplingPeriod?.name
        }
      }
    )

    table.twoColumnsRow(
      document.label("Y-Axis orientation"),
      document.comboBox(layerSupport.chartSupport.rootChartState.axisOrientationYProperty, AxisOrientationY.entries),
    )

    table.twoColumnsRow(
      document.label("X-Axis orientation"),
      document.comboBox(layerSupport.chartSupport.rootChartState.axisOrientationXProperty, AxisOrientationX.entries),
    )

    table.twoColumnsRow(
      document.label("Snap configuration"),
      document.comboBox(layerSupport.chartSupport.pixelSnapSupport.snapConfigurationProperty, SnapConfiguration.entries),
    )

    table.twoColumnsRow(
      document.label("Default Theme"),
      document.comboBox(
        defaultThemeConfig, listOf(
          DefaultTheme.Instance,
          DarkDesign,
          NeckarITDesign,
          SegoeUiTheme,
          DebugTheme,
        )
      ) { it.id }
    )

    table.twoColumnsRow(
      document.label("Theme"),
      document.comboBox(
        themeConfig, listOf(
          DefaultTheme.Instance,
          DarkDesign,
          NeckarITDesign,
          SegoeUiTheme,
          DebugTheme,
        )
      ) { it.id }
    )

    table.twoColumnsRow(
      document.label("Device pixel ratio"),
      document.label(chartSupport.devicePixelRatioSupport.devicePixelRatioProperty)
    )

    val computedStyleHolder = window.getComputedStyle(meisterChart.holder)
    val computedZoomHolder = computedStyleHolder.getPropertyValue("zoom")
    val zoomObservable = ObservableDouble(computedZoomHolder.toDouble()).also {
      it.consume { newZoom ->
        meisterChart.holder.style.setProperty("zoom", newZoom.toString())
      }
    }
    table.twoColumnsRow(
      document.label("CSS zoom"),
      document.slider(zoomObservable, 0.1, 4.0, 0.1)
    )

    table.twoColumnsRow(
      document.label("Text locale"),
      document.comboBox(textLocale, listOf(Locale.US, Locale.Germany, Locale("hu-HU"))) {
        it.locale
      }
    )

    table.twoColumnsRow(
      document.label("Format locale"),
      document.comboBox(formatLocale, listOf(Locale.US, Locale.Germany, Locale("hu-HU"))) {
        it.locale
      }
    )

    table.twoColumnsRow(
      document.label("Time zone"),
      document.comboBox(timeZone, listOf(TimeZone.UTC, TimeZone.Berlin, TimeZone.Tokyo, TimeZone("America/Chicago"))) {
        it.zoneId
      }
    )

    return table
  }

  private fun createDebugOptionsConfigurationTable(meisterChart: MeisterchartJS): HTMLTableElement {
    val table = createConfigurationTable()

    table.singleColumnRow(document.headline1("Debug options"))

    DebugFeature.entries.map { debugFeature ->
      val debugModeEnabled = ObservableBoolean().apply {
        consume {
          currentMeisterChart.layerSupport.apply {
            this.debug.set(debugFeature, it)
            this.markAsDirty(DirtyReason.UiStateChanged)
          }
        }
      }

      //Listener that is notified when the debug config changes (e.g. through the ToggleDebuggingModeLayer)
      meisterChart.chartSupport.debug.enabledFeaturesProperty.consume {
        debugModeEnabled.value = it.contains(debugFeature)
      }

      table.singleColumnRow(document.checkBox(debugModeEnabled, debugFeature.name))
    }

    table.singleColumnRow(
      document.button("Toggle All") {
        currentMeisterChart.chartSupport.debug.toggle()
      }
    )

    return table
  }

  private fun createDebugToolsConfigurationTable(meisterChart: MeisterchartJS): HTMLTableElement {
    val layerSupport = meisterChart.layerSupport

    val table = createConfigurationTable()

    table.singleColumnRow(document.headline1("Debug tools"))

    // TODO button to open debug pane (see com.meistercharts.fx.debug.ChartingStateDebugPane)
    table.singleColumnRow((document.button("Repaint") { currentMeisterChart.layerSupport.markAsDirty(DirtyReason.UserInteraction) }))

    //Initialize from the current state - a demo may have enabled the statistics in its configure block
    val paintPerformanceLayerVisible = ObservableBoolean(layerSupport.recordPaintStatistics)
    paintPerformanceLayerVisible.consumeImmediately {
      layerSupport.recordPaintStatistics = it
    }

    paintPerformanceLayerVisible.registerDirtyListener(layerSupport, DirtyReason.UiStateChanged)
    layerSupport.layers.addLayer(PaintPerformanceLayer().visibleIfWithState(paintPerformanceLayerVisible))
    layerSupport.layers.addLayer(FramesPerSecondLayer().visibleIfWithState(paintPerformanceLayerVisible))

    table.singleColumnRow(document.checkBox(paintPerformanceLayerVisible, "Performance layer"))

    val forceRepaint = ObservableBoolean()
    layerSupport.layers.addLayer(MarkAsDirtyLayer().visibleIfWithState(forceRepaint))
    forceRepaint.consume {
      currentMeisterChart.layerSupport.markAsDirty(DirtyReason.UiStateChanged)
    }
    table.singleColumnRow(document.checkBox(forceRepaint, "Always Repaint"))

    val contentAreaDebugLayerVisible = ObservableBoolean().also {
      it.consume {
        currentMeisterChart.layerSupport.markAsDirty(DirtyReason.UiStateChanged)
      }
    }
    layerSupport.layers.addLayer(ContentAreaDebugLayer().visibleIfWithState(contentAreaDebugLayerVisible))
    table.singleColumnRow(document.checkBox(contentAreaDebugLayerVisible, "Content area debug"))

    val contentViewportDebugLayerVisible = ObservableBoolean().also {
      it.consume {
        currentMeisterChart.layerSupport.markAsDirty(DirtyReason.UiStateChanged)
      }
    }
    layerSupport.layers.addLayer(ContentViewportDebugLayer().visibleIfWithState(contentViewportDebugLayerVisible))
    table.singleColumnRow(document.checkBox(contentViewportDebugLayerVisible, "Content viewport debug"))

    val windowDebugLayerVisible = ObservableBoolean().also {
      it.consume {
        currentMeisterChart.layerSupport.markAsDirty(DirtyReason.UiStateChanged)
      }
    }
    layerSupport.layers.addLayer(WindowDebugLayer().visibleIfWithState(windowDebugLayerVisible))
    table.singleColumnRow(document.checkBox(windowDebugLayerVisible, "Window debug"))

    val paintingVariablesDebugLayerVisible = ObservableBoolean().also {
      it.consume {
        currentMeisterChart.layerSupport.markAsDirty(DirtyReason.UiStateChanged)
      }
    }
    layerSupport.layers.addLayer(DumpPaintingVariablesLayer().visibleIfWithState(paintingVariablesDebugLayerVisible))
    table.singleColumnRow(document.checkBox(paintingVariablesDebugLayerVisible, "Dump PaintingVariables"))


    meisterChart.layerSupport.layers.addLayer(EventsDebugLayer().visibleIf {
      layerSupport.debug[DebugFeature.LogEvents]
    })
    // do not add a check-box here; the feature is activated via the enabledFeaturesProperty

    val windowDebugLayerCssComicSans = ObservableBoolean().also {
      it.consumeImmediately {
        currentMeisterChart.holder.style.fontFamily = if (it) "Comic Sans MS" else ""
        currentMeisterChart.layerSupport.markAsDirty(DirtyReason.UiStateChanged)
      }
    }
    table.singleColumnRow(document.checkBox(windowDebugLayerCssComicSans, "CSS: Comic Sans MS"))

    return table
  }

  companion object {
    private val logger = LoggerFactory.getLogger("com.meistercharts.demojs.AbstractMeisterchartsDemosJS")
  }
}

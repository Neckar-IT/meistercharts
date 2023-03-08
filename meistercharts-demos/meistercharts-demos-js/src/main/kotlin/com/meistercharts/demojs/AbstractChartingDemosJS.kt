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
package com.meistercharts.demojs

import com.meistercharts.algorithms.axis.AxisOrientationX
import com.meistercharts.algorithms.axis.AxisOrientationY
import com.meistercharts.demo.layer.DumpPaintingVariablesLayer
import com.meistercharts.algorithms.layers.debug.ContentAreaDebugLayer
import com.meistercharts.algorithms.layers.debug.ContentViewportDebugLayer
import com.meistercharts.algorithms.layers.debug.EventsDebugLayer
import com.meistercharts.algorithms.layers.debug.FramesPerSecondLayer
import com.meistercharts.algorithms.layers.debug.MarkAsDirtyLayer
import com.meistercharts.algorithms.layers.debug.PaintPerformanceLayer
import com.meistercharts.algorithms.layers.debug.WindowDebugLayer
import com.meistercharts.algorithms.layers.visibleIf
import com.meistercharts.canvas.DebugFeature
import com.meistercharts.canvas.debug
import com.meistercharts.canvas.i18nSupport
import com.meistercharts.canvas.pixelSnapSupport
import com.meistercharts.canvas.registerDirtyListener
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoDescriptors
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.design.CorporateDesign
import com.meistercharts.design.DebugDesign
import com.meistercharts.design.NeckarITDesign
import com.meistercharts.design.SegoeUiDesign
import com.meistercharts.design.corporateDesign
import com.meistercharts.design.initCorporateDesign
import com.meistercharts.js.MeisterChartBuilderJS
import com.meistercharts.js.MeisterChartJS
import com.meistercharts.js.MeisterChartsPlatform
import it.neckar.open.i18n.Locale
import it.neckar.open.observable.ObservableBoolean
import it.neckar.open.observable.ObservableObject
import it.neckar.open.time.TimeZone
import com.meistercharts.version.MeisterChartsVersion
import it.neckar.logging.LoggerFactory
import it.neckar.logging.debug
import kotlinx.browser.document
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLTableElement

/**
 * Can start and display demos
 */
abstract class AbstractChartingDemosJS {
  /**
   * Contains the demo content itself
   */
  private val demoContentHolder = document.getElementById("demoContentHolder") ?: throw IllegalStateException("element with id <navigationTop> must not be null")

  /**
   * Holds the current MeisterCharts reference - backing field
   */
  private var _currentMeisterChart: MeisterChartJS? = null

  /**
   * Returns the current MeisterCharts instance or throws an exception
   */
  val currentMeisterChart: MeisterChartJS
    get() {
      return _currentMeisterChart ?: throw IllegalStateException("No current MeisterChart available")
    }

  init {
    logger.debug {
      "MeisterCharts version: ${MeisterChartsVersion.versionAsStringVerbose}"
    }

    MeisterChartsPlatform.init()
  }

  protected fun startDemo(descriptor: ChartingDemoDescriptor<*>, configuration: PredefinedConfiguration<*>?, showConfigurationPane: Boolean) {
    while (demoContentHolder.firstChild != null) {
      demoContentHolder.firstChild?.apply {
        demoContentHolder.removeChild(this)
      }
    }

    //Dispose the current MeisterCharts instance if there is one
    _currentMeisterChart?.dispose()
    _currentMeisterChart = null

    demoContentHolder.appendChild(descriptor.createDemo(configuration as PredefinedConfiguration<Nothing>?).createContent(descriptor, showConfigurationPane))
  }

  private fun ChartingDemo.createContent(descriptor: ChartingDemoDescriptor<*>, showConfigurationPane: Boolean): HTMLElement {
    val demoContent = (document.createElement("DIV") as HTMLElement).apply {
      classList.add("demoContent")
    }
    val demoControls = (document.createElement("DIV") as HTMLElement).apply {
      classList.add("demoControls")
      demoContent.appendChild(this)
    }
    val view = (document.createElement("DIV") as HTMLElement).apply {
      classList.add("view")
      demoControls.appendChild(this)
    }

    val meisterChart = MeisterChartBuilderJS(descriptor.name)
      .also {
        it.configure()
      }.build()

    _currentMeisterChart = meisterChart

    //Append the canvas element
    view.appendChild(meisterChart.holder)

    if (showConfigurationPane) {
      (document.createElement("DIV") as HTMLElement).apply {
        classList.add("configurationPane")
        initConfigurationPane(this, this@createContent, meisterChart)
        demoControls.appendChild(this)
      }
    }
    (document.createElement("DIV") as HTMLElement).apply {
      classList.add("descriptionPane")
      initDescriptionPane(this, descriptor)
      demoContent.appendChild(this)
    }


    return demoContent
  }

  private fun initConfigurationPane(pane: HTMLElement, demo: ChartingDemo, meisterChart: MeisterChartJS) {
    pane.appendChild(createChartStateConfigurationTable(meisterChart))
    pane.appendChild(createDebugOptionsConfigurationTable(meisterChart))
    pane.appendChild(createDebugToolsConfigurationTable(meisterChart))

    val table = createConfigurationTable()
    table.singleColumnRow(document.headline1("Configuration"))
    val htmlDemoConfiguration = DemoConfigurationJS(table)
    demo.declare(htmlDemoConfiguration, meisterChart.layerSupport)

    pane.appendChild(table)
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

  private fun createChartStateConfigurationTable(meisterChart: MeisterChartJS): HTMLTableElement {
    val layerSupport = meisterChart.layerSupport
    val chartSupport = meisterChart.chartSupport

    val textLocale: ObservableObject<Locale> = ObservableObject(chartSupport.i18nSupport.textLocale)
    val formatLocale: ObservableObject<Locale> = ObservableObject(chartSupport.i18nSupport.formatLocale)
    val timeZone: ObservableObject<TimeZone> = ObservableObject(chartSupport.i18nSupport.timeZone)

    textLocale.consume {
      currentMeisterChart.chartSupport.i18nSupport.textLocale = it
      currentMeisterChart.layerSupport.markAsDirty()
    }

    formatLocale.consume {
      currentMeisterChart.chartSupport.i18nSupport.formatLocale = it
      currentMeisterChart.layerSupport.markAsDirty()
    }

    timeZone.consume {
      currentMeisterChart.chartSupport.i18nSupport.timeZone = it
      currentMeisterChart.layerSupport.markAsDirty()
    }


    val corporateDesignConfig: ObservableObject<CorporateDesign> = ObservableObject(corporateDesign).also {
      it.consumeImmediately { newDesign ->
        initCorporateDesign(newDesign)
        currentMeisterChart.layerSupport.markAsDirty()
      }
    }

    val table = createConfigurationTable()

    table.singleColumnRow(document.headline1("Chart state"))

    table.twoColumnsRow(
      document.label("Zoom"),
      document.label(layerSupport.chartSupport.rootChartState.zoomProperty.map { it.format() })
    )

    table.twoColumnsRow(
      document.label("Translation"),
      document.label(layerSupport.chartSupport.rootChartState.windowTranslationProperty.map { it.format() })
    )

    table.twoColumnsRow(
      document.label("Window size"),
      document.label(layerSupport.chartSupport.rootChartState.windowSizeProperty.map { it.format() })
    )

    table.twoColumnsRow(
      document.label("Content area size"),
      document.label(layerSupport.chartSupport.rootChartState.contentAreaSizeProperty.map { it.format() })
    )

    table.twoColumnsRow(
      document.label("Y-Axis orientation"),
      document.comboBox(layerSupport.chartSupport.rootChartState.axisOrientationYProperty, AxisOrientationY.values())
    )

    table.twoColumnsRow(
      document.label("X-Axis orientation"),
      document.comboBox(layerSupport.chartSupport.rootChartState.axisOrientationXProperty, AxisOrientationX.values())
    )

    table.twoColumnsRow(
      document.label("Snap configuration"),
      document.comboBox(layerSupport.chartSupport.pixelSnapSupport.snapConfigurationProperty, enumValues())
    )

    table.twoColumnsRow(
      document.label("Corporate design"),
      document.comboBox(corporateDesignConfig, listOf(NeckarITDesign, SegoeUiDesign, DebugDesign)) { it.id }
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

  private fun createDebugOptionsConfigurationTable(meisterChart: MeisterChartJS): HTMLTableElement {
    val table = createConfigurationTable()

    table.singleColumnRow(document.headline1("Debug options"))

    DebugFeature.values().map { debugFeature ->
      val debugModeEnabled = ObservableBoolean().apply {
        consume {
          currentMeisterChart.layerSupport.apply {
            this.debug.set(debugFeature, it)
            this.markAsDirty()
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

  private fun createDebugToolsConfigurationTable(meisterChart: MeisterChartJS): HTMLTableElement {
    val layerSupport = meisterChart.layerSupport

    val table = createConfigurationTable()

    table.singleColumnRow(document.headline1("Debug tools"))

    // TODO button to open debug pane (see com.meistercharts.fx.debug.ChartingStateDebugPane)
    table.singleColumnRow((document.button("Repaint") { currentMeisterChart.layerSupport.markAsDirty() }))

    val paintPerformanceLayerVisible = ObservableBoolean()
    paintPerformanceLayerVisible.consumeImmediately {
      layerSupport.recordPaintStatistics = it
    }

    paintPerformanceLayerVisible.registerDirtyListener(layerSupport)
    layerSupport.layers.addLayer(PaintPerformanceLayer().visibleIf(paintPerformanceLayerVisible))
    layerSupport.layers.addLayer(FramesPerSecondLayer().visibleIf(paintPerformanceLayerVisible))

    table.singleColumnRow(document.checkBox(paintPerformanceLayerVisible, "Performance layer"))

    val forceRepaint = ObservableBoolean()
    layerSupport.layers.addLayer(MarkAsDirtyLayer().visibleIf(forceRepaint))
    forceRepaint.consume {
      currentMeisterChart.layerSupport.markAsDirty()
    }
    table.singleColumnRow(document.checkBox(forceRepaint, "Always Repaint"))

    val debugModeEnabled = ObservableBoolean().apply {
      consume {
        currentMeisterChart.layerSupport.debug.setAll(it)
        currentMeisterChart.layerSupport.markAsDirty()
      }
    }

    val contentAreaDebugLayerVisible = ObservableBoolean().also {
      it.consume {
        currentMeisterChart.layerSupport.markAsDirty()
      }
    }
    layerSupport.layers.addLayer(ContentAreaDebugLayer().visibleIf(contentAreaDebugLayerVisible))
    table.singleColumnRow(document.checkBox(contentAreaDebugLayerVisible, "Content area debug"))

    val contentViewportDebugLayerVisible = ObservableBoolean().also {
      it.consume {
        currentMeisterChart.layerSupport.markAsDirty()
      }
    }
    layerSupport.layers.addLayer(ContentViewportDebugLayer().visibleIf(contentViewportDebugLayerVisible))
    table.singleColumnRow(document.checkBox(contentViewportDebugLayerVisible, "Content viewport debug"))

    val windowDebugLayerVisible = ObservableBoolean().also {
      it.consume {
        currentMeisterChart.layerSupport.markAsDirty()
      }
    }
    layerSupport.layers.addLayer(WindowDebugLayer().visibleIf(windowDebugLayerVisible))
    table.singleColumnRow(document.checkBox(windowDebugLayerVisible, "Window debug"))

    val paintingVariablesDebugLayerVisible = ObservableBoolean().also {
      it.consume {
        currentMeisterChart.layerSupport.markAsDirty()
      }
    }
    layerSupport.layers.addLayer(DumpPaintingVariablesLayer().visibleIf(paintingVariablesDebugLayerVisible))
    table.singleColumnRow(document.checkBox(paintingVariablesDebugLayerVisible, "Dump PaintingVariables"))


    meisterChart.layerSupport.layers.addLayer(EventsDebugLayer().visibleIf {
      layerSupport.debug[DebugFeature.LogEvents]
    })
    // do not add a check-box here; the feature is activated via the enabledFeaturesProperty

    return table
  }

  private fun initDescriptionPane(div: HTMLElement, descriptor: ChartingDemoDescriptor<*>) {
    div.innerHTML = descriptor.description
  }

  companion object {
    /**
     * Retrieves all demo descriptors
     */
    fun getDemoDescriptors(): List<ChartingDemoDescriptor<*>> {
      val descriptors = mutableListOf<ChartingDemoDescriptor<*>>()
      descriptors.addAll(DemoDescriptors.descriptors)
      descriptors.addAll(DemoDescriptorsJS.descriptors)
      return descriptors
    }

    private val logger = LoggerFactory.getLogger("com.meistercharts.demojs.AbstractChartingDemosJS")
  }

}


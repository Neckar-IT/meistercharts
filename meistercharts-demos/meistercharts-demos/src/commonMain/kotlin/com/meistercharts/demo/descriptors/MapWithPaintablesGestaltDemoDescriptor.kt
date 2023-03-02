package com.meistercharts.demo.descriptors

import com.meistercharts.algorithms.layers.slippymap.OpenStreetMap
import com.meistercharts.algorithms.layers.slippymap.OpenStreetMapBlackAndWhite
import com.meistercharts.algorithms.layers.slippymap.OpenStreetMapDe
import com.meistercharts.algorithms.layers.slippymap.OpenStreetMapGrayscale
import com.meistercharts.algorithms.layers.slippymap.OpenStreetMapHumanitarian
import com.meistercharts.algorithms.layers.slippymap.OpenStreetMapTerrain
import com.meistercharts.algorithms.layers.slippymap.WikimediaMaps
import com.meistercharts.canvas.paintable.ButtonColorProvider
import com.meistercharts.canvas.paintable.ButtonState
import com.meistercharts.charts.MapGestalt
import com.meistercharts.charts.MapWithPaintablesGestalt
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableBoolean
import com.meistercharts.demo.configurableColorPicker
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.configurableFont
import com.meistercharts.demo.configurableList
import com.meistercharts.demo.section

/**
 * A demo descriptor for the [MapGestalt]
 */
class MapWithPaintablesGestaltDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Map With Paintables Gestalt"
  override val description: String = "<h1>Demo of the MapWithPaintablesGestalt</h1>Displayed is the population distributed among 6 age groups for different locations.<br/>Source: <a href=\"https://www.statistik-bw.de/SRDB/?R=GS416025\">www.statistik-bw.de</a>"
  override val category: DemoCategory = DemoCategory.Gestalt


  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {
        val gestalt = MapWithPaintablesGestalt(chartId)

        val colors = object {
          var disabledToolbarButtonBackgroundColor = gestalt.mapGestalt.style.toolbarButtonBackgroundProvider(ButtonState(enabled = false))
          var pressedToolbarButtonBackgroundColor = gestalt.mapGestalt.style.toolbarButtonBackgroundProvider(ButtonState(pressed = true))
          var hoverToolbarButtonBackgroundColor = gestalt.mapGestalt.style.toolbarButtonBackgroundProvider(ButtonState(hover = true))
          var defaultToolbarButtonBackgroundColor = gestalt.mapGestalt.style.toolbarButtonBackgroundProvider(ButtonState())
        }

        val toolbarButtonBackgroundProvider = ButtonColorProvider { state ->
          when {
            state.disabled -> colors.disabledToolbarButtonBackgroundColor
            state.pressed -> colors.pressedToolbarButtonBackgroundColor
            state.hover -> colors.hoverToolbarButtonBackgroundColor
            else -> colors.defaultToolbarButtonBackgroundColor
          }
        }
        gestalt.mapGestalt.style.toolbarButtonBackgroundProvider = toolbarButtonBackgroundProvider

        gestalt.configure(this)

        configure {
          val mapProviders = listOf(
            OpenStreetMap,
            OpenStreetMapDe,
            OpenStreetMapHumanitarian,
            OpenStreetMapGrayscale,
            OpenStreetMapBlackAndWhite,
            OpenStreetMapTerrain,
            WikimediaMaps
          )
          configurableList("Map provider", gestalt.mapGestalt.data.slippyMapProvider, mapProviders) {
            onChange {
              gestalt.mapGestalt.data.slippyMapProvider = it
              this@ChartingDemo.markAsDirty()
            }
          }

          /*
          section("Center")

          configurableDouble("Latitude [90° N - 90° S]") {
            min = -90.0
            max = 90.0
            value = gestalt.mapGestalt.data.slippyMapCenter.latitude
            onChange {
              gestalt.mapGestalt.data.slippyMapCenter = SlippyMapCenter(it, gestalt.mapGestalt.data.slippyMapCenter.longitude)
              chartSupport.resetZoomAndTranslationToDefaults()
            }
          }

          configurableDouble("Longitude [180° W - 180° E]") {
            min = -180.0
            max = 180.0
            value = gestalt.mapGestalt.data.slippyMapCenter.longitude
            onChange {
              gestalt.mapGestalt.data.slippyMapCenter = SlippyMapCenter(gestalt.mapGestalt.data.slippyMapCenter.latitude, it)
              chartSupport.resetZoomAndTranslationToDefaults()
            }
          }
           */

          configurableBoolean("Show legend", gestalt.style::showLegend)

          section("Toolbar")

          configurableBoolean("Show", gestalt.mapGestalt.style::showToolbar)

          configurableBoolean("Enabled") {
            value = gestalt.mapGestalt.zoomInToolbarButton.state.enabled
            onChange {
              gestalt.mapGestalt.zoomInToolbarButton.state = gestalt.mapGestalt.zoomInToolbarButton.state.copy(enabled = it)
              gestalt.mapGestalt.zoomOutToolbarButton.state = gestalt.mapGestalt.zoomOutToolbarButton.state.copy(enabled = it)
              this@ChartingDemo.markAsDirty()
            }
          }

          configurableDouble("Gap", gestalt.mapGestalt.toolbarLayer.configuration::gap) {
            min = 0.0
            max = 100.0
          }

          configurableDouble("Button gap", gestalt.mapGestalt.toolbarLayer.configuration::buttonGap) {
            min = 0.0
            max = 100.0
          }

          configurableColorPicker("Disabled background", colors::disabledToolbarButtonBackgroundColor)
          configurableColorPicker("Pressed background", colors::pressedToolbarButtonBackgroundColor)
          configurableColorPicker("Hover background", colors::hoverToolbarButtonBackgroundColor)
          configurableColorPicker("Default background", colors::defaultToolbarButtonBackgroundColor)

          section("Legal notice")

          configurableBoolean("Show", gestalt.mapGestalt.style::showCopyrightMarker)

          configurableColorPicker("Text color", gestalt.mapGestalt.legalNoticeLayer.style::textColor)

          configurableFont("Font", gestalt.mapGestalt.legalNoticeLayer.style::font)
        }
      }
    }
  }
}


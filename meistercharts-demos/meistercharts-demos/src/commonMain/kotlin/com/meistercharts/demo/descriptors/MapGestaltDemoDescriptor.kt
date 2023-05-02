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
package com.meistercharts.demo.descriptors

import com.meistercharts.algorithms.layers.addTilesDebugLayer
import com.meistercharts.algorithms.layers.slippymap.OpenStreetMap
import com.meistercharts.algorithms.layers.slippymap.OpenStreetMapBlackAndWhite
import com.meistercharts.algorithms.layers.slippymap.OpenStreetMapDe
import com.meistercharts.algorithms.layers.slippymap.OpenStreetMapGrayscale
import com.meistercharts.algorithms.layers.slippymap.OpenStreetMapHumanitarian
import com.meistercharts.algorithms.layers.slippymap.OpenStreetMapTerrain
import com.meistercharts.algorithms.layers.slippymap.SlippyMapCenter
import com.meistercharts.algorithms.layers.slippymap.WikimediaMaps
import com.meistercharts.algorithms.layers.slippymap.withLatitude
import com.meistercharts.algorithms.layers.slippymap.withLongitude
import com.meistercharts.canvas.paintable.ButtonColorProvider
import com.meistercharts.canvas.paintable.ButtonState
import com.meistercharts.canvas.resetZoomAndTranslationToDefaults
import com.meistercharts.charts.MapGestalt
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
class MapGestaltDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Map Gestalt"
  override val description: String = "## Demo of the MapGestalt"
  override val category: DemoCategory = DemoCategory.Gestalt


  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {
        val gestalt = MapGestalt(chartId)

        val colors = object {
          var disabledToolbarButtonBackgroundColor = gestalt.style.toolbarButtonBackgroundProvider(ButtonState(enabled = false))
          var pressedToolbarButtonBackgroundColor = gestalt.style.toolbarButtonBackgroundProvider(ButtonState(pressed = true))
          var hoverToolbarButtonBackgroundColor = gestalt.style.toolbarButtonBackgroundProvider(ButtonState(hover = true))
          var defaultToolbarButtonBackgroundColor = gestalt.style.toolbarButtonBackgroundProvider(ButtonState())
        }

        val toolbarButtonBackgroundProvider = ButtonColorProvider { state ->
          when {
            state.disabled -> colors.disabledToolbarButtonBackgroundColor
            state.pressed -> colors.pressedToolbarButtonBackgroundColor
            state.hover -> colors.hoverToolbarButtonBackgroundColor
            else -> colors.defaultToolbarButtonBackgroundColor
          }
        }
        gestalt.style.toolbarButtonBackgroundProvider = toolbarButtonBackgroundProvider

        gestalt.configure(this)
        configure {
          layers.addTilesDebugLayer(debug)
        }

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
          configurableList("Map provider", gestalt.data.slippyMapProvider, mapProviders) {
            onChange {
              gestalt.data.slippyMapProvider = it
              this@ChartingDemo.markAsDirty()
            }
          }

          section("Center")

          configurableDouble("Latitude [90° N - 90° S]", gestalt.data.slippyMapCenter.latitude.value) {
            min = -90.0
            max = 90.0
            onChange {
              gestalt.data.slippyMapCenter = gestalt.data.slippyMapCenter.withLatitude(it)
              chartSupport.resetZoomAndTranslationToDefaults()
            }
          }

          configurableDouble("Longitude [180° W - 180° E]", gestalt.data.slippyMapCenter.longitude.value) {
            min = -180.0
            max = 180.0
            onChange {
              gestalt.data.slippyMapCenter = gestalt.data.slippyMapCenter.withLongitude(it)
              chartSupport.resetZoomAndTranslationToDefaults()
            }
          }

          declare {
            button("Mössingen") {
              gestalt.data.slippyMapCenter = SlippyMapCenter.neckarItCenter
              chartSupport.resetZoomAndTranslationToDefaults()
            }
            button("Emmendingen") {
              gestalt.data.slippyMapCenter = SlippyMapCenter.emmendingen
              chartSupport.resetZoomAndTranslationToDefaults()
            }
          }

          section("Toolbar")

          configurableBoolean("Show", gestalt.style::showToolbar)

          configurableBoolean("Enabled") {
            value = gestalt.zoomInToolbarButton.state.enabled
            onChange {
              gestalt.zoomInToolbarButton.state = gestalt.zoomInToolbarButton.state.copy(enabled = it)
              gestalt.zoomOutToolbarButton.state = gestalt.zoomOutToolbarButton.state.copy(enabled = it)
              this@ChartingDemo.markAsDirty()
            }
          }

          configurableDouble("Gap", gestalt.toolbarLayer.configuration::gap) {
            min = 0.0
            max = 100.0
          }

          configurableDouble("Button gap", gestalt.toolbarLayer.configuration::buttonGap) {
            min = 0.0
            max = 100.0
          }

          configurableColorPicker("Disabled background", colors::disabledToolbarButtonBackgroundColor)
          configurableColorPicker("Pressed background", colors::pressedToolbarButtonBackgroundColor)
          configurableColorPicker("Hover background", colors::hoverToolbarButtonBackgroundColor)
          configurableColorPicker("Default background", colors::defaultToolbarButtonBackgroundColor)

          section("Legal notice")

          configurableBoolean("Show", gestalt.style::showCopyrightMarker)

          configurableColorPicker("Text color", gestalt.legalNoticeLayer.style::textColor)

          configurableFont("Font", gestalt.legalNoticeLayer.style::font)
        }
      }
    }
  }
}

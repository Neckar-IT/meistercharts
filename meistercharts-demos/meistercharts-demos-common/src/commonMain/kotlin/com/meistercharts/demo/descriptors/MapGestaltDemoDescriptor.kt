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
package com.meistercharts.demo.descriptors

import com.meistercharts.demo.configurable
import kotlin.uuid.Uuid
import com.meistercharts.algorithms.layers.addTilesDebugLayer
import com.meistercharts.algorithms.layers.slippymap.SlippyMapProvider
import com.meistercharts.canvas.paintable.ButtonColorProvider
import com.meistercharts.canvas.paintable.ButtonState
import com.meistercharts.canvas.resetZoomAndTranslationToDefaults
import com.meistercharts.charts.MapGestalt
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.VariabilityType
import com.meistercharts.demo.configurableBoolean
import com.meistercharts.demo.configurableColorPicker
import com.meistercharts.demo.configurableColorPickerProvider
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.configurableFontProvider
import com.meistercharts.demo.configurableList
import com.meistercharts.demo.section
import com.meistercharts.maps.SlippyMapCenter
import com.meistercharts.maps.withLatitude
import com.meistercharts.maps.withLongitude
import com.meistercharts.zoom.UpdateReason

/**
 * A demo descriptor for the [MapGestalt]
 */
class MapGestaltDemoDescriptor : MeisterchartsDemoDescriptor<Nothing> {
  override val uuid: Uuid = Uuid.parse("17c27afd-4934-48f4-8b0c-48dee480e48c")
  override val name: String = "Map Gestalt"
  override val description: String = "Demo of the MapGestalt"
  override val category: DemoCategory = DemoCategory.Gestalt

  override val quality: DemoQuality = DemoQuality.High
  override val variabilityType: VariabilityType = VariabilityType.Unstable

  override fun prepareDemo(configuration: PredefinedConfiguration<Nothing>?): MeisterchartsDemo {
    return MeisterchartsDemo(this) {
      meistercharts {
        val gestalt = MapGestalt(chartId)

        val colors = object {
          var disabledToolbarButtonBackgroundColor = gestalt.configuration.toolbarButtonBackgroundProvider(ButtonState(enabled = false))
          var pressedToolbarButtonBackgroundColor = gestalt.configuration.toolbarButtonBackgroundProvider(ButtonState(pressed = true))
          var hoverToolbarButtonBackgroundColor = gestalt.configuration.toolbarButtonBackgroundProvider(ButtonState(hover = true))
          var defaultToolbarButtonBackgroundColor = gestalt.configuration.toolbarButtonBackgroundProvider(ButtonState())
        }

        val toolbarButtonBackgroundProvider = ButtonColorProvider { state ->
          when {
            state.disabled -> colors.disabledToolbarButtonBackgroundColor
            state.pressed -> colors.pressedToolbarButtonBackgroundColor
            state.hover -> colors.hoverToolbarButtonBackgroundColor
            else -> colors.defaultToolbarButtonBackgroundColor
          }
        }
        gestalt.configuration.toolbarButtonBackgroundProvider = toolbarButtonBackgroundProvider

        gestalt.configure(this)
        configure {
          layers.addTilesDebugLayer(debug)
        }

        configure {
          configurableList("Map provider", gestalt.configuration.slippyMapProvider, SlippyMapProvider.all) {
            onChange {
              gestalt.configuration.slippyMapProvider = it
              this@MeisterchartsDemo.markAsDirty()
            }
          }

          section("Center")

          configurableDouble("Latitude [90° N - 90° S]", gestalt.configuration.slippyMapCenter.latitude.value) {
            min = -90.0
            max = 90.0
            onChange {
              gestalt.configuration.slippyMapCenter = gestalt.configuration.slippyMapCenter.withLatitude(it)
              chartSupport.resetZoomAndTranslationToDefaults(reason = UpdateReason.UserInteraction)
            }
          }

          configurableDouble("Longitude [180° W - 180° E]", gestalt.configuration.slippyMapCenter.longitude.value) {
            min = -180.0
            max = 180.0
            onChange {
              gestalt.configuration.slippyMapCenter = gestalt.configuration.slippyMapCenter.withLongitude(it)
              chartSupport.resetZoomAndTranslationToDefaults(reason = UpdateReason.UserInteraction)
            }
          }

          declare {
            button("Mössingen") {
              gestalt.configuration.slippyMapCenter = SlippyMapCenter.neckarItCenter
              chartSupport.resetZoomAndTranslationToDefaults(reason = UpdateReason.UserInteraction)
            }
            button("Emmendingen") {
              gestalt.configuration.slippyMapCenter = SlippyMapCenter.emmendingen
              chartSupport.resetZoomAndTranslationToDefaults(reason = UpdateReason.UserInteraction)
            }
          }

          section("Toolbar")

          configurable("Show", gestalt.configuration::showToolbar)

          configurableBoolean("Enabled") {
            value = gestalt.zoomInToolbarButton.state.enabled
            onChange {
              gestalt.zoomInToolbarButton.state = gestalt.zoomInToolbarButton.state.copy(enabled = it)
              gestalt.zoomOutToolbarButton.state = gestalt.zoomOutToolbarButton.state.copy(enabled = it)
              this@MeisterchartsDemo.markAsDirty()
            }
          }

          configurable("Gap", gestalt.toolbarLayer.configuration::gap) {
            min = 0.0
            max = 100.0
          }

          configurable("Button gap", gestalt.toolbarLayer.configuration::buttonGap) {
            min = 0.0
            max = 100.0
          }

          configurable("Disabled background", colors::disabledToolbarButtonBackgroundColor)
          configurable("Pressed background", colors::pressedToolbarButtonBackgroundColor)
          configurable("Hover background", colors::hoverToolbarButtonBackgroundColor)
          configurable("Default background", colors::defaultToolbarButtonBackgroundColor)

          section("Legal notice")

          configurable("Show", gestalt.configuration::showCopyrightMarker)

          configurable("Text color", gestalt.legalNoticeLayer.configuration::textColor)

          configurable("Font", gestalt.legalNoticeLayer.configuration::font)
        }
      }
    }
  }
}

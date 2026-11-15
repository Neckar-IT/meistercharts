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
import com.meistercharts.algorithms.layers.slippymap.SlippyMapProvider
import com.meistercharts.canvas.paintable.ButtonColorProvider
import com.meistercharts.canvas.paintable.ButtonState
import com.meistercharts.charts.MapGestalt
import com.meistercharts.charts.MapWithPaintablesGestalt
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

/**
 * A demo descriptor for the [MapGestalt]
 */
class MapWithPaintablesGestaltDemoDescriptor : MeisterchartsDemoDescriptor<Nothing> {
  override val uuid: Uuid = Uuid.parse("6d490680-ffcb-4ecd-b35b-c94f9b99d042")
  override val name: String = "Map With Paintables Gestalt"
  override val description: String = "Population by 6 age groups per location on a map. Source: <a href=\"https://www.statistik-bw.de/SRDB/?R=GS416025\">www.statistik-bw.de</a>"
  override val category: DemoCategory = DemoCategory.Gestalt

  override val quality: DemoQuality = DemoQuality.High
  override val variabilityType: VariabilityType = VariabilityType.Unstable

  override fun prepareDemo(configuration: PredefinedConfiguration<Nothing>?): MeisterchartsDemo {
    return MeisterchartsDemo(this) {
      meistercharts {
        val gestalt = MapWithPaintablesGestalt(chartId)

        val colors = object {
          var disabledToolbarButtonBackgroundColor = gestalt.mapGestalt.configuration.toolbarButtonBackgroundProvider(ButtonState(enabled = false))
          var pressedToolbarButtonBackgroundColor = gestalt.mapGestalt.configuration.toolbarButtonBackgroundProvider(ButtonState(pressed = true))
          var hoverToolbarButtonBackgroundColor = gestalt.mapGestalt.configuration.toolbarButtonBackgroundProvider(ButtonState(hover = true))
          var defaultToolbarButtonBackgroundColor = gestalt.mapGestalt.configuration.toolbarButtonBackgroundProvider(ButtonState())
        }

        val toolbarButtonBackgroundProvider = ButtonColorProvider { state ->
          when {
            state.disabled -> colors.disabledToolbarButtonBackgroundColor
            state.pressed -> colors.pressedToolbarButtonBackgroundColor
            state.hover -> colors.hoverToolbarButtonBackgroundColor
            else -> colors.defaultToolbarButtonBackgroundColor
          }
        }
        gestalt.mapGestalt.configuration.toolbarButtonBackgroundProvider = toolbarButtonBackgroundProvider

        gestalt.configure(this)

        configure {
          configurableList("Map provider", gestalt.mapGestalt.configuration.slippyMapProvider, SlippyMapProvider.all) {
            onChange {
              gestalt.mapGestalt.configuration.slippyMapProvider = it
              this@MeisterchartsDemo.markAsDirty()
            }
          }

          configurable("Show legend", gestalt.configuration::showLegend)

          section("Toolbar")

          configurable("Show", gestalt.mapGestalt.configuration::showToolbar)

          configurableBoolean("Enabled") {
            value = gestalt.mapGestalt.zoomInToolbarButton.state.enabled
            onChange {
              gestalt.mapGestalt.zoomInToolbarButton.state = gestalt.mapGestalt.zoomInToolbarButton.state.copy(enabled = it)
              gestalt.mapGestalt.zoomOutToolbarButton.state = gestalt.mapGestalt.zoomOutToolbarButton.state.copy(enabled = it)
              this@MeisterchartsDemo.markAsDirty()
            }
          }

          configurable("Gap", gestalt.mapGestalt.toolbarLayer.configuration::gap) {
            min = 0.0
            max = 100.0
          }

          configurable("Button gap", gestalt.mapGestalt.toolbarLayer.configuration::buttonGap) {
            min = 0.0
            max = 100.0
          }

          configurable("Disabled background", colors::disabledToolbarButtonBackgroundColor)
          configurable("Pressed background", colors::pressedToolbarButtonBackgroundColor)
          configurable("Hover background", colors::hoverToolbarButtonBackgroundColor)
          configurable("Default background", colors::defaultToolbarButtonBackgroundColor)

          section("Legal notice")

          configurable("Show", gestalt.mapGestalt.configuration::showCopyrightMarker)

          configurable("Text color", gestalt.mapGestalt.legalNoticeLayer.configuration::textColor)

          configurable("Font", gestalt.mapGestalt.legalNoticeLayer.configuration::font)
        }
      }
    }
  }
}


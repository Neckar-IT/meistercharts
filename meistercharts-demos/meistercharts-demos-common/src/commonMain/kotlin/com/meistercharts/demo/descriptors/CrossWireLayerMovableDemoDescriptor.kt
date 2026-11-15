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
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.crosswire.CrossWireLayer
import com.meistercharts.algorithms.layers.crosswire.movable
import com.meistercharts.annotations.Window
import com.meistercharts.annotations.WindowRelative
import com.meistercharts.canvas.mouseCursorSupport
import com.meistercharts.color.Color
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.VariabilityType
import com.meistercharts.demo.configurableDouble
import com.meistercharts.model.Insets
import com.meistercharts.style.BoxStyle
import com.meistercharts.style.Palette
import com.meistercharts.time.TimeRange
import it.neckar.open.formatting.decimalFormat
import it.neckar.open.i18n.I18nConfiguration
import it.neckar.open.i18n.TextService
import it.neckar.open.provider.MultiProvider

/**
 * How a movable cross wire could be set up
 */
class CrossWireLayerMovableDemoDescriptor : CrossWireLayerDemoDescriptor() {
  override val uuid: Uuid = Uuid.parse("68385447-eb49-4ade-916f-75ec52af0970")
  override val name: String = "Cross wire layer - movable"
  override val quality: DemoQuality = DemoQuality.High
  override val variabilityType: VariabilityType = VariabilityType.Stable

  //language=HTML
  override val description: String = "A cross wire layer that can be moved"
  override val category: DemoCategory = DemoCategory.Layers

  override fun configureDemo(meisterchartsDemo: MeisterchartsDemo) {
    super.configureDemo(meisterchartsDemo)

    with(meisterchartsDemo) {
      meistercharts {
        val configuration = object {
          /**
           * the cross wire location
           */
          val crossWireLocationX: @WindowRelative Double = 0.75
        }

        val contentAreaTimeRange: TimeRange = TimeRange.oneHourUntilNow()

        val crossWireValueLabelsProvider = object : CrossWireLayer.ValueLabelsProvider {
          val location: @Window Double = 250.0
          val delta = 15.0

          override fun size(): Int {
            return 10
          }

          override fun layout(wireLocation: Double, paintingContext: LayerPaintingContext) {
            //Do nothing!
          }

          override fun locationAt(index: Int): @Window Double {
            return location + index * delta
          }

          override fun labelAt(index: Int, textService: TextService, i18nConfiguration: I18nConfiguration): String {
            if (index == 2) {
              return ""
            }

            return "Label @ ${decimalFormat.format(locationAt(index))}"
          }
        }

        val crossWireLayer = CrossWireLayer(crossWireValueLabelsProvider) {
          showCurrentLocationLabel = true
          showValueLabels = true

          valueLabelBoxStyle = MultiProvider { index ->
            if (index == 2) {
              BoxStyle(fill = Palette.getChartColor(index), borderColor = Color.red, padding = Insets.of(10.0))
            } else {
              BoxStyle(fill = Palette.getChartColor(index), borderColor = Color.silver)
            }
          }
        }

        configure {
          val movable = crossWireLayer.movable(chartSupport.mouseCursorSupport, chartSupport.mouseEvents)
          layers.addLayer(movable)

          configurable("padding bottom", movable.configuration::paddingBottom) {
            min = 0.0
            max = 50.0
          }
        }
      }
    }
  }

}

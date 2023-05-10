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

import com.meistercharts.algorithms.ValueRange
import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.ValueAxisLayer
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.charts.ContentViewportGestalt
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.button
import com.meistercharts.demo.configurableFont
import com.meistercharts.model.Insets
import it.neckar.open.formatting.NumberFormat
import it.neckar.open.formatting.cached
import it.neckar.open.formatting.format
import it.neckar.open.i18n.I18nConfiguration
import it.neckar.open.kotlin.lang.WhitespaceConfig

class ValueAxisContentViewportCalculationsDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Value Axis - Content Viewport Calculations"

  //language=HTML
  override val category: DemoCategory = DemoCategory.Calculations

  override val description: String =
    """
      Visualizes the preferred content viewport margins - calculated by the axis layer
  """.trimIndent()

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {

        val valueAxis = ValueAxisLayer("Title", ValueRange.default) {
          size = 300.0 //space for larger fonts
          this.ticksFormat = object : NumberFormat {
            override fun format(value: Double, i18nConfiguration: I18nConfiguration, whitespaceConfig: WhitespaceConfig): String {
              return "Äq: " + value.format()
            }
          }.cached()
        }

        val contentViewportGestalt = ContentViewportGestalt(Insets.of(40.0, 40.0, 40.0, 40.0))
        contentViewportGestalt.configure(this)

        configure {
          layers.addClearBackground()
          layers.addLayer(valueAxis)
          layers.addLayer(object : AbstractLayer() {
            override val type: LayerType = LayerType.Notification

            override fun paint(paintingContext: LayerPaintingContext) {
              val gc = paintingContext.gc

              gc.stroke(Color.green)

              valueAxis.style.calculatePreferredViewportMarginTop().let { y ->
                gc.strokeLine(0.0, y, gc.width, y)
              }
              (gc.height - valueAxis.style.calculatePreferredViewportMarginBottom()).let { y ->
                gc.strokeLine(0.0, y, gc.width, y)
              }
            }
          })
        }

        configurableFont("Tick Font", valueAxis.style::tickFont) {
        }

        button("Match CVP margin") {
          contentViewportGestalt.setMarginTop(valueAxis.style.calculatePreferredViewportMarginTop())
          contentViewportGestalt.setMarginBottom(valueAxis.style.calculatePreferredViewportMarginBottom())
        }
      }
    }
  }
}

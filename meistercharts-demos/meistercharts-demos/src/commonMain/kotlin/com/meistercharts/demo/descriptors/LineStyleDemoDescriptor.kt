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

import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.linechart.Dashes
import com.meistercharts.algorithms.layers.linechart.LineStyle
import com.meistercharts.canvas.ArcType
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.configurableList
import com.meistercharts.style.Palette
import kotlin.math.PI

/**
 */
class LineStyleDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Line Styles"
  override val description: String = "Line Styles"
  override val category: DemoCategory = DemoCategory.Primitives

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {
        configure {

          var lineStyle: LineStyle = LineStyle.Continuous
          var lineWidth: Double = 1.0

          layers.addClearBackground()

          layers.addLayer(object : AbstractLayer() {
            override val type: LayerType
              get() = LayerType.Content

            override fun paint(paintingContext: LayerPaintingContext) {
              val gc = paintingContext.gc
              lineStyle.apply(gc)
              gc.lineWidth = lineWidth

              gc.strokeLine(0.0, 0.0, 100.0, 100.0)
              gc.strokeRect(100.0, 100.0, 100.0, 200.0)
              gc.strokeArcCenter(400.0, 200.0, 50.0, 0.0, PI, ArcType.Open)
              gc.strokeArcCenter(400.0, 400.0, 50.0, 0.0, PI * 2, ArcType.Open)
            }
          })

          val availableStyles = listOf(
            LineStyle.Continuous.copy(color = Palette.getChartColor(0)),
            LineStyle.Dotted.copy(color = Palette.getChartColor(1)),
            LineStyle.SmallDashes.copy(color = Palette.getChartColor(2)),
            LineStyle.LargeDashes.copy(color = Palette.getChartColor(3)),
            LineStyle(dashes = Dashes(7.0, 30.0), color = Palette.getChartColor(4))
          )

          configurableList("Style", LineStyle.Continuous, availableStyles) {
            onChange {
              lineStyle = it
              markAsDirty()
            }
          }

          configurableDouble("lineWidth", lineWidth) {
            max = 50.0

            onChange {
              lineWidth = it
              markAsDirty()
            }
          }
        }
      }
    }
  }
}

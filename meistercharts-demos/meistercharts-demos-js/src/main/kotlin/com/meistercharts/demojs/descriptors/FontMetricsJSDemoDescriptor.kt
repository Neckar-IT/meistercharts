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
package com.meistercharts.demojs.descriptors

import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.canvas.CanvasRenderingContext
import com.meistercharts.canvas.FontDescriptorFragment
import com.meistercharts.canvas.FontMetrics
import com.meistercharts.canvas.saved
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableFont
import com.meistercharts.fonts.AbstractCanvasFontMetricsCalculator
import com.meistercharts.js.CanvasFontMetricsCalculatorJS
import com.meistercharts.js.FontMetricsCacheJS
import com.meistercharts.model.Direction

/**
 */
class FontMetricsJSDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "HTML Font Metrics"
  override val category: DemoCategory = DemoCategory.Platform

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {
        configure {
          layers.addClearBackground()


          val myLayer = object : AbstractLayer() {
            var fontFragment = FontDescriptorFragment.L

            override val type: LayerType = LayerType.Content

            override fun paint(paintingContext: LayerPaintingContext) {
              val gc = paintingContext.gc

              gc.font(fontFragment)
              val fontMetricsCalculator: CanvasFontMetricsCalculatorJS = FontMetricsCacheJS.fontMetricsCalculator
              val fontMetrics = fontMetricsCalculator.calculateFontMetrics(fontFragment.withDefaultValues())

              //Top
              gc.translate(140.0, 40.0)
              run {
                gc.stroke(Color.blue)
                gc.strokeLine(-10.0, 0.0, gc.width, 0.0)

                gc.saved {
                  gc.translate(0.0, fontMetrics.accentLine)
                  drawLines(fontMetrics, gc)
                }

                gc.font(fontFragment)
                gc.stroke(Color.black)
                gc.fillText(AbstractCanvasFontMetricsCalculator.lineChars, 0.0, 0.0, Direction.TopLeft)
              }

              //Center
              gc.translate(0.0, gc.height / 4)
              run {
                gc.stroke(Color.blue)
                gc.strokeLine(-10.0, 0.0, gc.width, 0.0)

                gc.saved {
                  gc.translate(0.0, fontMetrics.capitalHLine / 2.0)
                  drawLines(fontMetrics, gc)
                }

                gc.font(fontFragment)
                gc.stroke(Color.black)
                gc.fillText(AbstractCanvasFontMetricsCalculator.lineChars, 0.0, 0.0, Direction.CenterLeft)
              }

              //BaseLine
              gc.translate(0.0, gc.height / 4)
              run {
                gc.stroke(Color.blue)
                gc.strokeLine(-10.0, 0.0, gc.width, 0.0)

                gc.saved {
                  drawLines(fontMetrics, gc)
                }

                gc.font(fontFragment)
                gc.stroke(Color.black)
                gc.fillText(AbstractCanvasFontMetricsCalculator.lineChars, 0.0, 0.0, Direction.BaseLineLeft)
              }

              //Bottom
              gc.translate(0.0, gc.height / 4 + 40.0)
              run {
                gc.stroke(Color.blue)
                gc.strokeLine(-10.0, 0.0, gc.width, 0.0)

                //Ascent
                gc.saved {
                  gc.translate(0.0, -fontMetrics.pLine)
                  drawLines(fontMetrics, gc)
                }

                gc.font(fontFragment)
                gc.stroke(Color.black)
                gc.fillText(AbstractCanvasFontMetricsCalculator.lineChars, 0.0, 0.0, Direction.BottomLeft)
              }

            }
          }

          layers.addLayer(myLayer)
          configurableFont("font", myLayer::fontFragment)
        }
      }
    }
  }

  private fun drawLines(fontMetrics: FontMetrics, gc: CanvasRenderingContext) {
    gc.font(FontDescriptorFragment.XXS)
    gc.stroke(Color.darkgray)
    gc.strokeLine(0.0, 0.0, gc.width, 0.0)
    gc.fillText("Base", 0.0, 0.0, Direction.CenterRight, 10.0, 10.0)

    gc.stroke(Color.silver)
    fontMetrics.xLine.let {
      gc.strokeLine(0.0, -it, gc.width, -it)
      gc.fillText("${AbstractCanvasFontMetricsCalculator.xLineChar}: $it", 0.0, -it, Direction.CenterRight, 10.0, 10.0)
    }
    fontMetrics.capitalHLine.let {
      gc.strokeLine(0.0, -it, gc.width, -it)
      gc.fillText("${AbstractCanvasFontMetricsCalculator.capitalHLineChar}: $it", 0.0, -it, Direction.CenterRight, 10.0, 10.0)
    }
    fontMetrics.accentLine.let {
      gc.strokeLine(0.0, -it, gc.width, -it)
      gc.fillText("${AbstractCanvasFontMetricsCalculator.accentLineChar}: $it", 0.0, -it, Direction.CenterRight, 10.0, 10.0)
    }
    fontMetrics.pLine.let {
      gc.strokeLine(0.0, it, gc.width, it)
      gc.fillText("${AbstractCanvasFontMetricsCalculator.pLineChar}: $it", 0.0, it, Direction.CenterRight, 10.0, 10.0)
    }
  }
}

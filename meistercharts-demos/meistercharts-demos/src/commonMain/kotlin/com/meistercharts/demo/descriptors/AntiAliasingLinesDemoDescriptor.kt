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

import com.meistercharts.algorithms.environment
import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.canvas.CanvasRenderingContext
import com.meistercharts.canvas.saved
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.model.Direction
import it.neckar.open.formatting.format

/**
 */
class AntiAliasingLinesDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Anti Aliasing Demo"

  //language=HTML
  override val description: String =
    """|<h3>Visualizes some AA effects</h3>
       | <h4> Segments:</h4>
       |
       |<ul>
       | <li> 1. Segment: 0.5 Pixel
       | <li> 2. Segment: 1 Pixel
       | <li> 3. Segment: 2 Pixel
       | <li> 4. Segment: 1 / Device Pixel Ratio Pixel
       | </ul>
       | <h4> Triples:</h4>
       |
       |<ul>
       |  <li> 1. Triple: Offset 0.0 pixel
       |  <li> 2. Triple: Offset 0.5 pixel
       |  <li> 3. Triple: Offset 0.25 pixel
       |  <li> 4. Triple: Snap to physical pixel
       |</ul>
       | <h4>Lines:</h4>
       |
       |<ul>
       |  <li> 1. Line: Color: Black
       |  <li> 2. Line: Color: Gray
       |  <li> 3. Line: Color: LightGray
       |</ul>
 """.trimMargin()
  override val category: DemoCategory = DemoCategory.LowLevelTests

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {
        configure {
          layers.addClearBackground()

          layers.addLayer(object : AbstractLayer() {
            override val type: LayerType
              get() = LayerType.Content

            override fun paint(paintingContext: LayerPaintingContext) {
              val gc = paintingContext.gc
              gc.translate(10.0, 0.0)

              gc.saved {
                gc.lineWidth = 0.5
                gc.fillText("Line Width: ${gc.lineWidth.format()}", 0.0, 0.0, Direction.TopLeft)

                strokeLines(gc)

                gc.translate(10.5, 0.0)
                strokeLines(gc)

                gc.translate(10.25, 0.0)
                strokeLines(gc)

                gc.translate(10.0, 0.0)
                strokeLinesSnappedToPhysicalPixel(gc)
              }

              gc.translate(200.0, 0.0)

              gc.saved {
                gc.lineWidth = 1.0
                gc.fillText("Line Width: ${gc.lineWidth.format()}", 0.0, 0.0, Direction.TopLeft)

                strokeLines(gc)

                gc.translate(10.5, 0.0)
                strokeLines(gc)

                gc.translate(10.25, 0.0)
                strokeLines(gc)

                gc.translate(10.0, 0.0)
                strokeLinesSnappedToPhysicalPixel(gc)
              }

              gc.translate(200.0, 0.0)

              gc.saved {
                gc.lineWidth = 2.0
                gc.fillText("Line Width: ${gc.lineWidth.format()}", 0.0, 0.0, Direction.TopLeft)

                strokeLines(gc)

                gc.translate(10.5, 0.0)
                strokeLines(gc)

                gc.translate(10.25, 0.0)
                strokeLines(gc)

                gc.translate(10.0, 0.0)
                strokeLinesSnappedToPhysicalPixel(gc)
              }

              gc.translate(200.0, 0.0)


              gc.saved {
                gc.lineWidth = 1.0 / environment.devicePixelRatio
                gc.fillText("Line Width: ${gc.lineWidth.format()}", 0.0, 0.0, Direction.TopLeft)

                strokeLines(gc)

                gc.translate(10.5, 0.0)
                strokeLines(gc)

                gc.translate(10.25, 0.0)
                strokeLines(gc)

                gc.translate(10.0, 0.0)
                strokeLinesSnappedToPhysicalPixel(gc)
              }

              gc.translate(200.0, 0.0)
            }

            private fun strokeLinesSnappedToPhysicalPixel(gc: CanvasRenderingContext) {
              gc.snapPhysicalTranslation()
              gc.stroke(Color.black)
              gc.strokeLine(0.0, 0.0, 0.0, gc.height)

              gc.translate(5.0, 0.0)
              gc.snapPhysicalTranslation()
              gc.stroke(Color.gray)
              gc.strokeLine(0.0, 0.0, 0.0, gc.height)

              gc.translate(5.0, 0.0)
              gc.snapPhysicalTranslation()
              gc.stroke(Color.lightgray)
              gc.strokeLine(0.0, 0.0, 0.0, gc.height)

            }

            private fun strokeLines(gc: CanvasRenderingContext) {
              gc.stroke(Color.black)
              gc.strokeLine(0.0, 0.0, 0.0, gc.height)

              gc.translate(5.0, 0.0)
              gc.stroke(Color.gray)
              gc.strokeLine(0.0, 0.0, 0.0, gc.height)

              gc.translate(5.0, 0.0)
              gc.stroke(Color.lightgray)
              gc.strokeLine(0.0, 0.0, 0.0, gc.height)
            }
          })
        }
      }
    }
  }
}

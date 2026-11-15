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

import kotlin.uuid.Uuid
import com.meistercharts.environment
import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.color.Color
import com.meistercharts.canvas.CanvasRenderingContext
import com.meistercharts.canvas.saved
import com.meistercharts.canvas.stroke
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.VariabilityType
import it.neckar.geometry.Direction
import it.neckar.open.formatting.format

/**
 */
class AntiAliasingLinesDemoDescriptor : MeisterchartsDemoDescriptor<Nothing> {
  override val uuid: Uuid = Uuid.parse("a69e6cb1-f6e3-4c91-b3df-91d942b706a3")
  override val name: String = "Anti Aliasing Demo"
  override val quality: DemoQuality = DemoQuality.High
  override val variabilityType: VariabilityType = VariabilityType.Stable

  //language=HTML
  override val description: String = "Visualizes anti-aliasing effects for lines at different pixel offsets and snapping"
  override val category: DemoCategory = DemoCategory.LowLevelTests

  override fun prepareDemo(configuration: PredefinedConfiguration<Nothing>?): MeisterchartsDemo {
    return MeisterchartsDemo(this) {
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

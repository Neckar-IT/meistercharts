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
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.canvas.strokeCross
import com.meistercharts.canvas.strokeCross45Degrees
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableDouble

/**
 *
 */
class DrawingPrimitivesArcPathDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Drawing Primitives: Arc Path"

  //language=HTML
  override val description: String = "## shows how to add arcs to a path"
  override val category: DemoCategory = DemoCategory.Primitives

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {

    return ChartingDemo {
      meistercharts {
        configure {
          layers.addClearBackground()

          var x1 = 30.0
          var y1 = 30.0
          var x2 = 220.0
          var y2 = 140.0

          var controlX1 = 120.0
          var controlY1 = 160.0

          var radius = 10.0


          layers.addLayer(object : AbstractLayer() {
            override val type: LayerType
              get() = LayerType.Content

            override fun paint(paintingContext: LayerPaintingContext) {
              val gc = paintingContext.gc
              gc.stroke(Color.gray)
              gc.strokeLine(x1, y1, controlX1, controlY1)
              gc.strokeLine(controlX1, controlY1, x2, y2)

              gc.beginPath()

              gc.moveTo(x1, y1)
              gc.arcTo(controlX1, controlY1, x2, y2, radius)
              gc.lineTo(x2, y2)

              gc.stroke(Color.orangered)
              gc.stroke()

              gc.lineWidth = 1.0
              gc.strokeCross(x1, y1, 5.0)
              gc.strokeCross(x2, y2, 5.0)
              gc.strokeCross45Degrees(controlX1, controlY1, 5.0)
            }
          }
          )

          configurableDouble("x1", x1) {
            max = 800.0
            onChange {
              x1 = it
              markAsDirty()
            }
          }
          configurableDouble("y1", y1) {
            max = 800.0
            onChange {
              y1 = it
              markAsDirty()
            }
          }

          configurableDouble("controlX1", controlX1) {
            max = 800.0
            onChange {
              controlX1 = it
              markAsDirty()
            }
          }
          configurableDouble("controlY1", controlY1) {
            max = 800.0
            onChange {
              controlY1 = it
              markAsDirty()
            }
          }

          configurableDouble("x2", x2) {
            max = 800.0
            onChange {
              x2 = it
              markAsDirty()
            }
          }
          configurableDouble("y2", y2) {
            max = 800.0
            onChange {
              y2 = it
              markAsDirty()
            }
          }

          configurableDouble("radius", radius) {
            max = 800.0
            onChange {
              radius = it
              markAsDirty()
            }
          }
        }
      }
    }
  }
}

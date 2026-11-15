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
import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.canvas.stroke
import com.meistercharts.color.Color
import com.meistercharts.canvas.strokeCross
import com.meistercharts.canvas.strokeCross45Degrees
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.VariabilityType
import com.meistercharts.demo.configurableDouble

/**
 *
 */
class DrawingPrimitivesBezierCurveDemoDescriptor : MeisterchartsDemoDescriptor<Nothing> {
  override val uuid: Uuid = Uuid.parse("62201f23-7beb-4f8c-af93-c72081e27958")
  override val name: String = "Drawing Primitives: Bezier Curves"

  //language=HTML
  override val description: String = "Shows how to draw bezier curves"
  override val category: DemoCategory = DemoCategory.Primitives
  override val quality: DemoQuality = DemoQuality.High
  override val variabilityType: VariabilityType = VariabilityType.Stable

  override fun prepareDemo(configuration: PredefinedConfiguration<Nothing>?): MeisterchartsDemo {

    return MeisterchartsDemo(this) {
      meistercharts {
        configure {
          layers.addClearBackground()

          var x1 = 30.0
          var y1 = 30.0
          var x2 = 220.0
          var y2 = 140.0

          var controlX1 = 120.0
          var controlY1 = 160.0
          var controlX2 = 180.0
          var controlY2 = 10.0


          layers.addLayer(object : AbstractLayer() {
            override val type: LayerType
              get() = LayerType.Content

            override fun paint(paintingContext: LayerPaintingContext) {
              val gc = paintingContext.gc
              gc.beginPath()

              gc.moveTo(x1, y1)
              gc.bezierCurveTo(controlX1, controlY1, controlX2, controlY2, x2, y2)

              gc.stroke(Color.orangered)
              gc.stroke()

              gc.lineWidth = 1.0
              gc.strokeCross(x1, y1, 5.0)
              gc.strokeCross(x2, y2, 5.0)
              gc.strokeCross45Degrees(controlX1, controlY1, 5.0)
              gc.strokeCross45Degrees(controlX2, controlY2, 5.0)

              gc.stroke(Color.gray)
              gc.strokeLine(x1, y1, controlX1, controlY1)
              gc.strokeLine(x2, y2, controlX2, controlY2)
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

          configurableDouble("controlX2", controlX2) {
            max = 800.0
            onChange {
              controlX2 = it
              markAsDirty()
            }
          }
          configurableDouble("controlY2", controlY2) {
            max = 800.0
            onChange {
              controlY2 = it
              markAsDirty()
            }
          }


        }
      }
    }
  }
}

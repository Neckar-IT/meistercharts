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
import com.meistercharts.canvas.paintMark
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.configurableRad
import com.meistercharts.model.Coordinates
import com.meistercharts.model.PolarCoordinates
import com.meistercharts.model.Rectangle
import it.neckar.open.unit.si.rad
import kotlin.math.PI

/**
 *
 */
class AngleDirectionDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Direction with angle Demo"

  //language=HTML
  override val description: String = "# How to find the bounding box using an angle"
  override val category: DemoCategory = DemoCategory.Calculations

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {
        configure {
          layers.addClearBackground()
          val layer = AngleDirectionDemoDescriptorLayer()
          layers.addLayer(layer)


          configurableDouble("X", layer::x) {
            min = -400.0
            max = 400.0
            onChange {
              layer.x = it
              markAsDirty()
            }
          }
          configurableDouble("y", layer::y) {
            min = -400.0
            max = 400.0
            onChange {
              layer.y = it
              markAsDirty()
            }
          }
          configurableDouble("width", layer::width) {
            min = -400.0
            max = 400.0
            onChange {
              layer.width = it
              markAsDirty()
            }
          }
          configurableDouble("height", layer::height) {
            min = -400.0
            max = 400.0
            onChange {
              layer.height = it
              markAsDirty()
            }
          }

          configurableRad("Angle in Shape", layer::angleInShape) {
          }
        }
      }
    }
  }
}

private class AngleDirectionDemoDescriptorLayer : AbstractLayer() {
  var angleInShape: @rad Double = 0.0

  var x = -75.0
  var y = -50.0
  var width = 100.0
  var height = 150.0

  override val type: LayerType
    get() = LayerType.Content

  override fun paint(paintingContext: LayerPaintingContext) {
    val gc = paintingContext.gc

    gc.translateToCenter()
    gc.stroke(Color.silver)
    gc.strokeLine(-gc.width, 0.0, gc.width, 0.0)
    gc.strokeLine(0.0, -gc.height, 0.0, gc.height)

    val rect = Rectangle(x, y, width, height)

    gc.stroke(Color.orange)
    gc.strokeRect(rect)

    //Paint the origin of the rectangle
    gc.strokeOvalCenter(x, y, 15.0, 15.0)

    //paint the sizes *from origin*
    gc.stroke(Color.blue)
    gc.strokeLine(0.0, 0.0, rect.left, 0.0)
    gc.stroke(Color.deeppink)
    gc.strokeLine(0.0, 0.0, rect.right, 0.0)
    gc.stroke(Color.hotpink)
    gc.strokeLine(0.0, 0.0, 0.0, rect.top)
    gc.stroke(Color.blueviolet)
    gc.strokeLine(0.0, 0.0, 0.0, rect.bottom)


    val anchorX = rect.xFromRadRelative(angleInShape % (2 * PI))
    val anchorY = rect.yFromRadRelative(angleInShape % (2 * PI))
    gc.stroke(Color.red)
    gc.paintMark(anchorX, anchorY)

    //The direction
    gc.stroke(Color.silver)
    gc.strokeLine(0.0, 0.0, anchorX, anchorY)

    //The angle as calculated
    gc.stroke(Color.orange)

    gc.strokeLine(Coordinates.origin, PolarCoordinates(100.0, angleInShape).toCartesian())
  }
}

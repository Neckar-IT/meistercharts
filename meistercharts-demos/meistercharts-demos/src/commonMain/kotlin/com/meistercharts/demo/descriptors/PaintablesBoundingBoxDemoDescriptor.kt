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
import com.meistercharts.algorithms.paintable.ObjectFit
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.canvas.FontDescriptorFragment
import com.meistercharts.canvas.paintable.DebugPaintable
import com.meistercharts.canvas.saved
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.configurableEnum
import com.meistercharts.demo.section
import com.meistercharts.model.Coordinates
import com.meistercharts.model.Direction
import com.meistercharts.model.Size

/**
 *
 */
class PaintablesBoundingBoxDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Paintable - paintInBoundingBox"
  override val category: DemoCategory = DemoCategory.Paintables

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {
        configure {
          layers.addClearBackground()


          val layer = object : AbstractLayer() {
            override val type: LayerType = LayerType.Content

            val paintable = DebugPaintable()

            var x: Double = 10.0
            var y: Double = 10.0

            var width: Double = 180.0
            var height: Double = 50.0
            var objectFit = ObjectFit.ContainNoGrow

            override fun paint(paintingContext: LayerPaintingContext) {
              val gc = paintingContext.gc
              gc.translateToCenter()

              val location = Coordinates.of(x, y)

              gc.saved {
                paintable.paintInBoundingBox(paintingContext, location, Direction.TopLeft, Size(width, height), objectFit = objectFit)
              }

              gc.stroke(Color.red)
              gc.fill(Color.red)
              gc.strokeRect(location, Size(width, height))
              gc.font(FontDescriptorFragment.XS)
              gc.fillText("Forced.BoundingBox", location, Direction.BottomLeft)

              gc.stroke(Color.blue)
              gc.fill(Color.blue)
              gc.strokeRect(location, paintable.boundingBox(paintingContext).size)
              gc.font(FontDescriptorFragment.XS)
              //Move to bottom right of the bounding box
              gc.fillText("Paintable.Size", location.plus(paintable.width, paintable.height), Direction.TopRight)
            }
          }
          layers.addLayer(layer)

          configurableEnum("Object Fit", layer::objectFit, enumValues())

          section("Paintable")
          configurableDouble("Width", layer.paintable::width) {
            max = 500.0
          }
          configurableDouble("Height", layer.paintable::height) {
            max = 500.0
          }

          configurableDouble("Alignment Point X", layer.paintable::alignmentPointX) {
            min = -100.0
            max = 100.0
          }
          configurableDouble("Alignment Point Y", layer.paintable::alignmentPointY) {
            min = -100.0
            max = 100.0
          }

          section("Bounding Box")

          configurableDouble("Width", layer::width) {
            max = 500.0
          }
          configurableDouble("Height", layer::height) {
            max = 500.0
          }

          configurableDouble("X", layer::x) {
            min = -100.0
            max = 100.0
          }
          configurableDouble("Y", layer::y) {
            min = -100.0
            max = 100.0
          }
        }
      }
    }
  }
}

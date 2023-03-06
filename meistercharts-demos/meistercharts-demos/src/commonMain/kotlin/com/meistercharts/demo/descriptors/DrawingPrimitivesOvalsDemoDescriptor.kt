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
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableDouble

/**
 *
 */
class DrawingPrimitivesOvalsDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Drawing Primitives: Ovals"

  //language=HTML
  override val description: String = "## shows how to add arcs to a path"
  override val category: DemoCategory = DemoCategory.Primitives

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {

    return ChartingDemo {
      meistercharts {
        configure {
          layers.addClearBackground()


          val layer = object : AbstractLayer() {
            override val type: LayerType
              get() = LayerType.Content

            var width = 100.0
            var height = 140.0

            override fun paint(paintingContext: LayerPaintingContext) {
              val gc = paintingContext.gc

              val x = gc.center.x
              val y = gc.center.y

              gc.fill(Color.lightgray)
              gc.fillOvalCenter(x, y, width, height)
              gc.stroke(Color.orange)
              gc.strokeOvalCenter(x, y, width, height)

              gc.strokeCross(x, y, 5.0)
            }
          }
          layers.addLayer(
            layer
          )

          configurableDouble("width", layer::width) {
            max = 800.0
          }

          configurableDouble("height", layer::height) {
            max = 800.0
          }
        }
      }
    }
  }
}

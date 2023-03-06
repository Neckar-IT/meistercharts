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
import com.meistercharts.canvas.Canvas
import com.meistercharts.canvas.CanvasFactory
import com.meistercharts.canvas.CanvasType
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableDouble
import com.meistercharts.model.Size
import kotlin.math.PI

/**
 *
 */
class DrawingHatchesDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Drawing Hatches"

  override val category: DemoCategory = DemoCategory.Primitives

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {
        configure {
          layers.addClearBackground()

          val config = object {
            var width = 200.0
            var height = 200.0
            var scale = 1.0
          }

          val layer = object : AbstractLayer() {
            override val type: LayerType = LayerType.Content

            lateinit var patternCanvas: Canvas

            override fun initialize(paintingContext: LayerPaintingContext) {
              super.initialize(paintingContext)

              val canvasFactory: CanvasFactory = CanvasFactory.get()
              patternCanvas = canvasFactory.createCanvas(CanvasType.OffScreen, Size.PX_30)
              patternCanvas.gc.also { gc ->
                gc.fill(Color.lightseagreen)
                gc.fillRect(0.0, 0.0, gc.width, gc.height)

                gc.stroke(Color.blue)
                gc.arcCenter(0.0, 0.0, 30.0, 0.0, 0.5 * PI);
                gc.stroke()
              }
            }

            override fun paint(paintingContext: LayerPaintingContext) {
              val gc = paintingContext.gc
              gc.pattern(patternCanvas)

              gc.scale(config.scale, config.scale)
              gc.fillRect(40.0, 40.0, config.width, config.height)
            }
          }

          configurableDouble("width", config::width) {
            max = 500.0
          }
          configurableDouble("height", config::height) {
            max = 500.0
          }

          configurableDouble("Scale", config::scale) {
            max = 2.0
          }

          layers.addLayer(
            layer
          )
        }
      }
    }
  }
}

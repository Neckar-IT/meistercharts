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
import com.meistercharts.canvas.saved
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableColor
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.configurableEnum
import com.meistercharts.demo.configurableList
import com.meistercharts.model.Direction
import com.meistercharts.model.Size
import com.meistercharts.resources.svg.SvgPaintableProviders

/**
 *
 */
class SvgPaintablesDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "SVG Paintables"
  override val description: String = "SVG Paintables - using a fill hint"
  override val category: DemoCategory = DemoCategory.Paintables

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {
        configure {
          layers.addClearBackground()

          var paintableProvider = SvgPaintableProviders.autoScale

          var width = 25.0
          var height = 25.0

          var fill: Color = Color.darkgray
          var stroke: Color = Color.red
          var anchorDirection = Direction.TopLeft

          layers.addLayer(object : AbstractLayer() {
            override val type: LayerType
              get() = LayerType.Content

            override fun paint(paintingContext: LayerPaintingContext) {
              val gc = paintingContext.gc
              gc.stroke(Color.silver)
              gc.strokeLine(0.0, gc.height / 2.0, gc.width, gc.height / 2.0)
              gc.strokeLine(gc.width / 2.0, 0.0, gc.width / 2.0, gc.height)

              gc.translate(gc.width / 2.0, gc.height / 2.0)

              val size = Size(width, height)
              val paintable = paintableProvider.get(size, fill, stroke, anchorDirection)
              gc.saved {
                paintable.paint(paintingContext, 0.0, 0.0)
              }

              //draw red rect
              gc.stroke(Color.orangered)
              gc.strokeRect(0.0, 0.0, size.width, size.height)
            }
          })

          configurableColor("Fill", fill) {
            onChange {
              fill = it
              markAsDirty()
            }
          }

          configurableColor("Stroke", fill) {
            onChange {
              stroke = it
              markAsDirty()
            }
          }

          configurableDouble("Image Width", width) {
            max = 200.0

            onChange {
              width = it
              markAsDirty()
            }
          }

          configurableDouble("Image Height", height) {
            max = 200.0

            onChange {
              height = it
              markAsDirty()
            }
          }

          configurableEnum("Anchor Direction", anchorDirection, Direction.entries) {
            onChange {
              anchorDirection = it
              markAsDirty()
            }
          }

          configurableList("Paintable", paintableProvider, SvgPaintableProviders.all()) {
            converter = { paintable ->
              paintable.toString()
            }

            onChange { it ->
              paintableProvider = it
              markAsDirty()
            }
          }
        }
      }

    }
  }
}

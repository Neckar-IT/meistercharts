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
import com.meistercharts.canvas.paintable.CombinedPaintable
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableColor
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.configurableEnum
import com.meistercharts.model.Direction
import com.meistercharts.model.Distance
import com.meistercharts.model.Size
import com.meistercharts.resources.svg.SvgPaintableProviders
import it.neckar.open.kotlin.lang.enumEntries

/**
 *
 */
class CombinedPaintablesDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Combined Paintables"

  //language=HTML
  override val description: String = "Combined Paintables"
  override val category: DemoCategory = DemoCategory.Paintables

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {
        configure {
          layers.addClearBackground()

          val mapMarker = SvgPaintableProviders.mapMarker
          val warning = SvgPaintableProviders.warning

          var primarySize = Size.PX_90
          var fill: Color = Color.darkgray
          var anchorDirection = Direction.TopLeft

          var secondarySizeFactor = 0.43
          var offsetPercentageX = 0.0
          var offsetPercentageY = -0.20


          layers.addLayer(object : AbstractLayer() {
            override val type: LayerType
              get() = LayerType.Content

            override fun paint(paintingContext: LayerPaintingContext) {
              val gc = paintingContext.gc
              gc.stroke(Color.silver)
              gc.strokeLine(0.0, gc.height / 2.0, gc.width, gc.height / 2.0)
              gc.strokeLine(gc.width / 2.0, 0.0, gc.width / 2.0, gc.height)

              gc.translate(gc.width / 2.0, gc.height / 2.0)

              val secondarySize = primarySize.times(secondarySizeFactor)

              val combined = CombinedPaintable.relative(
                mapMarker.get(primarySize, fill),
                warning.get(secondarySize, fill),
                Distance(offsetPercentageX, offsetPercentageY),
                paintingContext
              )

              combined.paintInBoundingBox(paintingContext, 0.0, 0.0, anchorDirection)
            }
          })

          configurableColor("Fill", fill) {
            onChange {
              fill = it
              markAsDirty()
            }
          }

          configurableDouble("Image Size", primarySize.width) {
            max = 200.0

            onChange {
              primarySize = Size.both(it)
              markAsDirty()
            }
          }
          configurableDouble("Secondary Size Factor", secondarySizeFactor) {
            max = 2.0

            onChange {
              secondarySizeFactor = it
              markAsDirty()
            }
          }
          configurableDouble("Offset Percentage X", offsetPercentageX) {
            max = 2.0
            min = -2.0

            onChange {
              offsetPercentageX = it
              markAsDirty()
            }
          }
          configurableDouble("Offset Percentage Y", offsetPercentageY) {
            max = 2.0
            min = -2.0

            onChange {
              offsetPercentageY = it
              markAsDirty()
            }
          }

          configurableEnum("Anchor Direction", anchorDirection, enumEntries()) {
            onChange {
              anchorDirection = it
              markAsDirty()
            }
          }
        }
      }

    }
  }
}

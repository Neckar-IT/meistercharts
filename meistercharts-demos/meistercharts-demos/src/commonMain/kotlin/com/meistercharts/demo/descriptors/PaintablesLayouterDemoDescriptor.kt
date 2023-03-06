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
import com.meistercharts.algorithms.layout.PaintablesLayouter
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.canvas.paintMark
import com.meistercharts.canvas.paintable.Paintable
import com.meistercharts.canvas.paintable.RectanglePaintable
import com.meistercharts.canvas.paintable.SymbolAndTextKeyPaintable
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
import it.neckar.open.kotlin.lang.fastFor
import it.neckar.open.provider.SizedProvider
import it.neckar.open.provider.asMultiProvider
import it.neckar.open.i18n.TextKey
import com.meistercharts.style.Palette.getChartColor

/**
 * Demos that visualizes the functionality of a multi line text
 */
class PaintablesLayouterDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Paintables Layouter"
  override val description: String = ""
  override val category: DemoCategory = DemoCategory.Calculations

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {

        configure {
          layers.addClearBackground()

          val layer = object : AbstractLayer() {
            override val type: LayerType
              get() = LayerType.Content

            /**
             * Where the paintables are painted
             */
            var paintingAnchor: Direction = Direction.TopLeft
            var horizontalGap: Double = 0.0
            var verticalGap: Double = 0.0


            val paintables = SizedProvider.forList(buildList<Paintable> {
              5.fastFor {
                add(RectanglePaintable(Size(20.0 + it * 5.0, 20.0 + it * 5.0), getChartColor(it)))
              }

              add(Paintable.NoOp)

              add(SymbolAndTextKeyPaintable(RectanglePaintable(24.0, 24.0, Color.pink), TextKey.simple("Hello World!")))
            })

            val layouter = PaintablesLayouter()

            override fun layout(paintingContext: LayerPaintingContext) {
              super.layout(paintingContext)

              layouter.calculate(paintingContext, paintables, paintingAnchor, horizontalGap, verticalGap)
            }

            override fun paint(paintingContext: LayerPaintingContext) {
              val gc = paintingContext.gc

              gc.translateToCenter()
              gc.paintMark()

              //Paint using the layout manager directly
              layouter.paintAllPaintables(paintingContext, paintables.asMultiProvider())

              //Paint
              gc.stroke(Color.orangered)
              gc.strokeRect(Coordinates.origin, layouter.totalSize())
            }
          }
          layers.addLayer(layer)

          configurableDouble("Gap", layer.layouter.configuration::gap) {
            min = 0.0
            max = 100.0
          }

          configurableEnum("Layout Orientation", layer.layouter.configuration::layoutOrientation)
          configurableEnum("Vertical Alignment", layer.layouter.configuration::verticalAlignment)
          configurableEnum("Horizontal Alignment", layer.layouter.configuration::horizontalAlignment)

          section("Painting Config")

          configurableEnum("Painting Anchor", layer::paintingAnchor)
          configurableDouble("Horizontal Gap", layer::horizontalGap) {
            max = 30.0
          }
          configurableDouble("Vertical Gap", layer::verticalGap) {
            max = 30.0
          }
        }
      }
    }
  }
}

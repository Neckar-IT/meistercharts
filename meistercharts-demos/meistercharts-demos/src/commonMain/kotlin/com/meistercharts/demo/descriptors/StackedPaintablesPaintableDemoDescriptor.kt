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
import com.meistercharts.algorithms.layers.legend.StackedPaintablesPaintable
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.canvas.DebugFeature
import com.meistercharts.canvas.paintMark
import com.meistercharts.canvas.paintable.RectanglePaintable
import com.meistercharts.canvas.saved
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.configurableEnum
import com.meistercharts.model.Orientation
import it.neckar.open.provider.SizedProvider

/**
 *
 */
class StackedPaintablesPaintableDemoDescriptor : ChartingDemoDescriptor<Orientation> {
  override val name: String = "Stacked Paintables Paintable"
  override val category: DemoCategory = DemoCategory.Paintables

  override val predefinedConfigurations: List<PredefinedConfiguration<Orientation>> = listOf(
    PredefinedConfiguration(Orientation.Vertical),
    PredefinedConfiguration(Orientation.Horizontal),
  )

  override fun createDemo(configuration: PredefinedConfiguration<Orientation>?): ChartingDemo {
    val payload = requireNotNull(configuration).payload

    return ChartingDemo {
      meistercharts {
        configure {
          layers.addClearBackground()


          val paintable = StackedPaintablesPaintable(
            SizedProvider.forValues(
              RectanglePaintable(10.0, 10.0, Color.red),
              RectanglePaintable(30.0, 20.0, Color.blue),
              RectanglePaintable(25.0, 30.0, Color.green),
            )
          ) {
            this.layoutOrientation = payload
          }

          val layer = object : AbstractLayer() {
            override val type: LayerType = LayerType.Content

            var x: Double = 100.0
            var y: Double = 100.0

            override fun layout(paintingContext: LayerPaintingContext) {
              super.layout(paintingContext)
              paintable.layout(paintingContext)
            }

            override fun paint(paintingContext: LayerPaintingContext) {
              val gc = paintingContext.gc

              gc.paintMark(x, y)

              gc.saved {
                paintable.paint(paintingContext, x, y)
              }

              if (gc.debug[DebugFeature.ShowBounds]) {
                val boundingBox = paintable.boundingBox(paintingContext)
                gc.stroke(Color.orange)
                gc.translate(x, y)
                gc.strokeRect(boundingBox)
              }
            }
          }
          layers.addLayer(layer)


          configurableDouble("x", layer::x) {
            max = 400.0
          }
          configurableDouble("y", layer::y) {
            max = 400.0
          }

          configurableEnum("Orientation", paintable.configuration.layoutOrientation)
          configurableEnum("Orientation", paintable.configuration.layoutOrientation)
        }
      }
    }
  }
}

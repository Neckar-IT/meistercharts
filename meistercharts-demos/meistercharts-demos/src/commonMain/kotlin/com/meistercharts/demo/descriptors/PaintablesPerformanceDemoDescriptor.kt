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
import com.meistercharts.canvas.paintable.Paintable
import com.meistercharts.canvas.saved
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableInt
import it.neckar.open.kotlin.lang.fastFor

/**
 *
 */
class PaintablesPerformanceDemoDescriptor : ChartingDemoDescriptor<() -> Paintable> {
  override val name: String = "Paintables Performance"

  override val category: DemoCategory = DemoCategory.Paintables

  override val predefinedConfigurations: List<PredefinedConfiguration<() -> Paintable>> = PaintableDemoDescriptor.createPaintableConfigurations()

  override fun createDemo(configuration: PredefinedConfiguration<() -> Paintable>?): ChartingDemo {
    require(configuration != null) { "Configuration required" }

    return ChartingDemo {
      val paintable = configuration.payload()

      meistercharts {
        configure {
          layers.addClearBackground()

          val layer = object : AbstractLayer() {
            var paintablesCount = 100

            override val type: LayerType = LayerType.Content

            override fun paint(paintingContext: LayerPaintingContext) {
              val gc = paintingContext.gc
              gc.translateToCenter()

              paintablesCount.fastFor { index ->
                gc.saved {
                  gc.translate(index.toDouble(), index.toDouble())
                  paintable.paint(paintingContext)
                }
              }
            }
          }
          layers.addLayer(layer)

          configurableInt("Paintables Count", layer::paintablesCount) {
            max = 1000
          }
        }
      }
    }
  }
}

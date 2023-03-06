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
import com.meistercharts.canvas.saved
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.model.Direction
import it.neckar.open.collections.fastForEach
import com.meistercharts.style.Palette

/**
 *
 */
class ColorTintDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Color Tint / Shade"
  override val category: DemoCategory = DemoCategory.Calculations

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {
        configure {
          layers.addClearBackground()

          layers.addLayer(object : AbstractLayer() {
            override val type: LayerType = LayerType.Content

            val size = 40.0
            val gap = 5.0

            val factors = listOf(0.1, 0.2, 0.4, 0.6, 0.8, 1.0)


            override fun paint(paintingContext: LayerPaintingContext) {
              val gc = paintingContext.gc

              gc.translate(20.0, 20.0)

              //To the center
              gc.translate((size + gap) * factors.size, 0.0)

              gc.saved {
                gc.fillText("Base", size / 2.0, 0.0, Direction.BottomCenter)

                gc.saved {
                  factors.fastForEach { factor ->
                    gc.translate(-size - gap, 0.0)
                    gc.fillText("-$factor", size / 2.0, 0.0, Direction.BottomCenter)
                  }
                }

                factors.fastForEach { factor ->
                  gc.translate(size + gap, 0.0)
                  gc.fillText("+$factor", size / 2.0, 0.0, Direction.BottomCenter)
                }
              }

              Palette.chartColors.fastForEach { baseColor ->
                gc.saved {
                  gc.fill(baseColor)
                  gc.fillRect(0.0, 0.0, size, size)
                  gc.strokeRect(0.0, 0.0, size, size)

                  //Shade
                  gc.saved {
                    factors.fastForEach { factor ->
                      gc.translate(-size - gap, 0.0)
                      gc.fill(baseColor.darker(factor))
                      gc.fillRect(0.0, 0.0, size, size)
                      gc.strokeRect(0.0, 0.0, size, size)
                    }
                  }

                  //Tint
                  factors.fastForEach { factor ->
                    gc.translate(size + gap, 0.0)
                    gc.fill(baseColor.lighter(factor))
                    gc.fillRect(0.0, 0.0, size, size)
                    gc.strokeRect(0.0, 0.0, size, size)
                  }
                }

                gc.translate(0.0, size + gap)
              }
            }
          })
        }
      }
    }
  }
}

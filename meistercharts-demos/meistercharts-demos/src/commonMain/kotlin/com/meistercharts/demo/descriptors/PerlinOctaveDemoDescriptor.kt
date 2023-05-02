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
import com.meistercharts.annotations.Zoomed
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.configurableInt
import it.neckar.open.kotlin.lang.fastFor
import it.neckar.open.random.Perlin

/**
 * A simple hello world demo.
 *
 * Can be used as template to create new demos
 */
class PerlinOctaveDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Perlin Random Octaves Generator"

  override val category: DemoCategory = DemoCategory.Calculations

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {
        configure {
          var perlinNoise = Perlin(123499.0)
          var octaves = 7
          var persistence = 0.5

          layers.addClearBackground()
          layers.addLayer(object : AbstractLayer() {
            override val type: LayerType = LayerType.Content

            val yScale = 300.0



            override fun paint(paintingContext: LayerPaintingContext) {
              val gc = paintingContext.gc
              gc.translate(20.0, 20.0)

              //horizontal lines representing 0.0 and 1.0
              gc.strokeLine(0.0, 0.0, gc.width, 0.0)
              gc.strokeLine(0.0, yScale, gc.width, yScale)

              gc.stroke(Color.darkred)

              gc.beginPath()
              400.fastFor { index ->
                val x = index * 5.0
                @Zoomed val y = perlinNoise.noiseOctave(index.toDouble(), octaves, persistence) * yScale

                if (index == 0) {
                  gc.moveTo(x,y)
                }else{
                  gc.lineTo(x,y)
                }
              }

              gc.stroke()
            }
          })

          configurableDouble("Frequency", perlinNoise.frequency){
            max = 10.0

            onChange {
              perlinNoise = perlinNoise.copy(frequency =  it)
              markAsDirty()
            }
          }

          configurableDouble("Amplitude", perlinNoise.amplitude){
            max = 1.5

            onChange {
              perlinNoise = perlinNoise.copy(amplitude =  it)
              markAsDirty()
            }
          }

          configurableInt("Size", perlinNoise.size){
            max = 200

            onChange {
              perlinNoise = perlinNoise.copy(size =  it)
              markAsDirty()
            }
          }

          configurableInt("Number of octaves", octaves){
            min = 1
            max = 15

            onChange {
              octaves = it
              markAsDirty()
            }
          }

          configurableDouble("Persistence of octaves", persistence){
            max = 1.0

            onChange {
              persistence = it
              markAsDirty()
            }
          }


        }
      }
    }
  }
}

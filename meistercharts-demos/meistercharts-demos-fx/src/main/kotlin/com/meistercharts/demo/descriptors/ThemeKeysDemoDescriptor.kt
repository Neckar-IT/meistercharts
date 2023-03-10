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
import com.meistercharts.canvas.FontDescriptorFragment
import com.meistercharts.canvas.saved
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.design.Theme
import com.meistercharts.design.ThemeKey
import com.meistercharts.model.Direction
import it.neckar.open.collections.fastForEachIndexed
import kotlin.reflect.full.memberProperties

/**
 *
 */
class ThemeKeysDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Theme Keys"
  override val description: String = "Shows all theme keys. Is platform dependent because it uses reflection"
  override val category: DemoCategory = DemoCategory.Platform

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {
        configure {

          val keys = Theme::class.memberProperties.map {
            (it.get(Theme) as ThemeKey<*>)
          }

          layers.addClearBackground()
          layers.addLayer(object : AbstractLayer() {
            override val type: LayerType = LayerType.Content

            override fun paint(paintingContext: LayerPaintingContext) {
              val gc = paintingContext.gc
              gc.translate(10.0, 10.0)

              keys.fastForEachIndexed { index, key ->
                val value = key()
                val id = key.id


                gc.saved {
                  gc.translate(0.0, index * 20.0)

                  if (value is Color) {
                    gc.fill(value)
                  }
                  if (value is FontDescriptorFragment) {
                    gc.font(value)
                  }

                  gc.fillText("$id: $value", 0.0, 0.0, Direction.TopLeft)
                }
              }
            }
          })
        }
      }
    }
  }
}

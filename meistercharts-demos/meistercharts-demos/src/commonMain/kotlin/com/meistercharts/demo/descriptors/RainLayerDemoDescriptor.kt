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

import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.custom.rainsensor.RainLayer
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableDouble

/**
 *
 */
class RainLayerDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Rain Layer"
  override val category: DemoCategory = DemoCategory.Layers

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {

    return ChartingDemo {
      meistercharts {

        configure {
          layers.addClearBackground()
          val layer = RainLayer(RainLayer.Data(3))
          layers.addLayer(layer)

          configurableDouble("Space horizontal", layer.style::spaceHorizontalPerDrop) {
            min = 1.0
            max = 200.0
          }
          configurableDouble("Space vertical", layer.style::spaceVerticalPerDrop) {
            min = 1.0
            max = 200.0
          }
        }

      }
    }
  }
}

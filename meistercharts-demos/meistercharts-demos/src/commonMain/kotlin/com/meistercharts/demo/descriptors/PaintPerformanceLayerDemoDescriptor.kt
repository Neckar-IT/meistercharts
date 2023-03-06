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
import com.meistercharts.algorithms.layers.debug.SlowLayer
import com.meistercharts.algorithms.layers.debug.addPerformanceLayer
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration

/**
 * Demos that visualizes the functionality of the PaintPerformanceLayerDemo layer
 */
class PaintPerformanceLayerDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Paint Performance"
  override val description: String = "## Paint performance"
  override val category: DemoCategory = DemoCategory.Layers

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {
        configure {
          layers.addClearBackground()
          layers.addLayer(SlowLayer(4.0, 4.0))
          layers.addLayer(SlowLayer(4.0, 4.0))
          layers.addPerformanceLayer()
        }
      }
    }
  }
}

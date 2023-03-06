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
import com.meistercharts.algorithms.layers.ClearBackgroundLayer
import com.meistercharts.algorithms.layers.FillBackgroundLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.LayerVisibilityAdapter
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableBoolean
import it.neckar.open.observable.ObservableBoolean

/**
 * Very simple demo that shows how to clear the background
 */
class ClearBackgroundLayerDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Clear background layer"
  override val description: String = "Clears the background"
  override val category: DemoCategory = DemoCategory.Layers

  private val clearBackground = ObservableBoolean(true)

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {


      meistercharts {
        configure {
          layers.addLayer(FillBackgroundLayer {
            background = Color.orangered
          })
          layers.addLayer(object : AbstractLayer() {
            override val type: LayerType = LayerType.Background

            override fun paint(paintingContext: LayerPaintingContext) {
              val gc = paintingContext.gc
              gc.fill(Color.blue)
              gc.fillRect(-10_000.0, -10_000.0, 100_000.0, 100_000.0)
            }
          })

          layers.addLayer(LayerVisibilityAdapter(ClearBackgroundLayer(), { clearBackground.value }))

          configurableBoolean("Clear", clearBackground)
        }
      }
    }
  }
}


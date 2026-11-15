/*
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


import kotlin.uuid.Uuid
import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.ClearBackgroundLayer
import com.meistercharts.algorithms.layers.FillBackgroundLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.LayerVisibilityAdapter
import com.meistercharts.canvas.fill
import com.meistercharts.color.Color
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.VariabilityType
import com.meistercharts.demo.configurableBoolean
import it.neckar.open.observable.ObservableBoolean

/**
 * Very simple demo that shows how to clear the background
 */
class ClearBackgroundLayerDemoDescriptor : MeisterchartsDemoDescriptor<Nothing> {
  override val uuid: Uuid = Uuid.parse("de19e948-57d3-4716-ae96-170531edbeff")
  override val name: String = "Clear background layer"
  override val description: String = "Clears the background"
  override val category: DemoCategory = DemoCategory.Layers
  override val quality: DemoQuality = DemoQuality.High
  override val variabilityType: VariabilityType = VariabilityType.Stable

  private val clearBackground = ObservableBoolean(true)

  override fun prepareDemo(configuration: PredefinedConfiguration<Nothing>?): MeisterchartsDemo {
    return MeisterchartsDemo(this) {


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


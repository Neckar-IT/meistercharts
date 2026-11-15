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
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.canvas.StyleDsl
import com.meistercharts.canvas.fill
import com.meistercharts.color.ColorProvider
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.VariabilityType
import com.meistercharts.demo.configurableList
import com.meistercharts.style.Palette
import com.meistercharts.style.Palette.chartColors

/**
 * Demos that visualizes the functionality of the FPS layer
 */
class PaletteDemoDescriptor : MeisterchartsDemoDescriptor<Nothing> {
  override val uuid: Uuid = Uuid.parse("280ac32d-12e1-49aa-be7f-aae40b04bac3")
  override val name: String = "Palette"
  override val description: String = "The chart color palette"
  override val category: DemoCategory = DemoCategory.Primitives
  override val quality: DemoQuality = DemoQuality.High
  override val variabilityType: VariabilityType = VariabilityType.Stable

  override fun prepareDemo(configuration: PredefinedConfiguration<Nothing>?): MeisterchartsDemo {
    return MeisterchartsDemo(this) {
      meistercharts {
        configure {
          layers.addClearBackground()
          val paletteLayer = PaletteLayer()
          layers.addLayer(paletteLayer)

          configurableList("Palettes", paletteLayer.style.colors, Palette.all) {
            converter = {
              "${it.size}"
            }

            onChange {
              paletteLayer.style.colors = it
              markAsDirty()
            }
          }
        }
      }
    }
  }
}


private class PaletteLayer() : AbstractLayer() {
  val style: Style = Style()

  override val type: LayerType
    get() = LayerType.Content

  override fun paint(paintingContext: LayerPaintingContext) {
    val gc = paintingContext.gc
    val widthPerLane = gc.width / style.colors.size

    style.colors.forEach {
      gc.fill(it)
      gc.fillRect(0.0, 0.0, widthPerLane, gc.height)
      gc.translate(widthPerLane, 0.0)
    }
  }

  @StyleDsl
  class Style {
    var colors: List<ColorProvider> = chartColors
  }
}

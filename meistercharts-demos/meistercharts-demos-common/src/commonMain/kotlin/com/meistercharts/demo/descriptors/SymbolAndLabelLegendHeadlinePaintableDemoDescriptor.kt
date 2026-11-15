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

import com.meistercharts.demo.configurable
import kotlin.uuid.Uuid
import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.legend.SymbolAndLabelLegendPaintable
import com.meistercharts.algorithms.layers.legend.withHeadline
import com.meistercharts.canvas.ChartSupport
import com.meistercharts.canvas.paintMark
import com.meistercharts.canvas.saved
import com.meistercharts.canvas.stroke
import com.meistercharts.color.Color
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.VariabilityType
import com.meistercharts.demo.configurableColor
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.configurableFontProvider
import com.meistercharts.demo.configurableList
import com.meistercharts.demo.configurableSize
import com.meistercharts.design.Theme
import com.meistercharts.style.Palette
import it.neckar.geometry.Size
import it.neckar.open.provider.MultiProvider
import it.neckar.open.provider.SizedProvider1

/**
 * A simple hello world demo.
 *
 * Can be used as template to create new demos
 */
class SymbolAndLabelLegendHeadlinePaintableDemoDescriptor : MeisterchartsDemoDescriptor<SymbolAndLabelLegendPaintable> {
  override val uuid: Uuid = Uuid.parse("de16ce84-250b-4dc9-91b7-3bdc02a46b0a")
  override val name: String = "Symbol/Label + Headline Legend"
  override val category: DemoCategory = DemoCategory.Paintables
  override val quality: DemoQuality = DemoQuality.High
  override val variabilityType: VariabilityType = VariabilityType.Stable

  override val predefinedConfigurations: List<PredefinedConfiguration<SymbolAndLabelLegendPaintable>> = listOf(
    PredefinedConfiguration(
      Uuid.parse("558db6fb-5120-4fff-a18a-3dac6ef5e1d8"),
      SymbolAndLabelLegendPaintable.rectangles(
        labels = object : SizedProvider1<String, ChartSupport> {
          override fun valueAt(index: Int, param1: ChartSupport): String {
            return "Text @ $index"
          }

          override fun size(param1: ChartSupport): Int {
            return 8
          }
        }, symbolColors = Theme.chartColors.resolve()
      ), "8 elements"
    ),
    PredefinedConfiguration(
      Uuid.parse("1da9c079-b45a-49f7-b862-bb806e312393"),
      SymbolAndLabelLegendPaintable.rectangles(
        labels = SizedProvider1.empty(), symbolColors = Theme.chartColors.resolve()
      ), "Empty"
    ),
  )

  override fun prepareDemo(configuration: PredefinedConfiguration<SymbolAndLabelLegendPaintable>?): MeisterchartsDemo {
    return MeisterchartsDemo(this) {
      meistercharts {
        configure {
          layers.addClearBackground()

          val symbolAndLAbelsPaintable: SymbolAndLabelLegendPaintable = requireNotNull(configuration).payload

          val paintable = symbolAndLAbelsPaintable.withHeadline { _, _ -> "Headline" }

          layers.addLayer(object : AbstractLayer() {
            override val type: LayerType = LayerType.Content

            override fun paint(paintingContext: LayerPaintingContext) {
              val gc = paintingContext.gc

              gc.translateToCenter()
              gc.paintMark()

              gc.stroke(Color.orange)
              gc.strokeRect(paintable.boundingBox(paintingContext))

              gc.saved {
                paintable.paint(paintingContext)
              }
            }
          })


          configurable("Symbol Label Gap", paintable.delegate.configuration::symbolLabelGap) {
            max = 30.0
          }
          configurable("Font", paintable.delegate.configuration::textFont)
          configurable("Entries Gap", paintable.delegate.configuration::entriesGap) {
            max = 50.0
          }

          configurableColor("Label Color", Palette.defaultGray()) {
            onChange {
              paintable.delegate.configuration.labelColors = MultiProvider.always(it)
              markAsDirty()
            }
          }
          configurableSize("Symbol Size", Size.PX_16) {
            onChange {
              paintable.delegate.configuration.symbols = SymbolAndLabelLegendPaintable.defaultSymbols(it, Theme.chartColors.resolve())
              markAsDirty()
            }
          }

          configurableList("Headline Text", "Headline", listOf("Headline", null, "#")) {
            onChange {
              paintable.headlinePaintable.style.label = { _, _ -> it }
              markAsDirty()
            }
          }
        }
      }
    }
  }
}

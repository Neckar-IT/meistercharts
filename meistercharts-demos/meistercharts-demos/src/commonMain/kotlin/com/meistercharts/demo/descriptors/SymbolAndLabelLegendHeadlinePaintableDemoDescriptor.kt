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
import com.meistercharts.algorithms.layers.legend.SymbolAndLabelLegendPaintable
import com.meistercharts.algorithms.layers.legend.withHeadline
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.canvas.ChartSupport
import com.meistercharts.canvas.paintMark
import com.meistercharts.canvas.saved
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableColor
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.configurableFontProvider
import com.meistercharts.demo.configurableList
import com.meistercharts.demo.configurableSize
import com.meistercharts.design.Theme
import com.meistercharts.model.Size
import it.neckar.open.provider.MultiProvider
import it.neckar.open.provider.SizedProvider1
import com.meistercharts.style.Palette

/**
 * A simple hello world demo.
 *
 * Can be used as template to create new demos
 */
class SymbolAndLabelLegendHeadlinePaintableDemoDescriptor : ChartingDemoDescriptor<SymbolAndLabelLegendPaintable> {
  override val name: String = "Symbol/Label + Headline Legend"
  override val category: DemoCategory = DemoCategory.Paintables

  override val predefinedConfigurations: List<PredefinedConfiguration<SymbolAndLabelLegendPaintable>> = listOf(
    PredefinedConfiguration(
      SymbolAndLabelLegendPaintable.rectangles(
        labels = object : SizedProvider1<String, ChartSupport> {
          override fun valueAt(index: Int, param1: ChartSupport): String {
            return "Text @ $index"
          }

          override fun size(param1: ChartSupport): Int {
            return 8
          }
        }, symbolColors = Theme.chartColors.invoke()
      ), "8 elements"
    ),
    PredefinedConfiguration(
      SymbolAndLabelLegendPaintable.rectangles(
        labels = SizedProvider1.empty(), symbolColors = Theme.chartColors.invoke()
      ), "Empty"
    ),
  )

  override fun createDemo(configuration: PredefinedConfiguration<SymbolAndLabelLegendPaintable>?): ChartingDemo {
    return ChartingDemo {
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


          configurableDouble("Symbol Label Gap", paintable.delegate.configuration::symbolLabelGap) {
            max = 30.0
          }
          configurableFontProvider("Font", paintable.delegate.configuration::textFont)
          configurableDouble("Entries Gap", paintable.delegate.configuration::entriesGap) {
            max = 50.0
          }

          configurableColor("Label Color", Palette.defaultGray) {
            onChange {
              paintable.delegate.configuration.labelColors = MultiProvider.always(it)
              markAsDirty()
            }
          }
          configurableSize("Symbol Size", Size.PX_16) {
            onChange {
              paintable.delegate.configuration.symbols = SymbolAndLabelLegendPaintable.defaultSymbols(it, Theme.chartColors.invoke())
              markAsDirty()
            }
          }

          configurableList("Headline Text", "Headline", listOf("Headline", null, "#")) {
            onChange {
              paintable.headlinePaintable.configuration.label = { _, _ -> it }
              markAsDirty()
            }
          }
        }
      }
    }
  }
}

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
class SymbolAndLabelLegendPaintableDemoDescriptor : MeisterchartsDemoDescriptor<Nothing> {
  override val uuid: Uuid = Uuid.parse("529f3640-45d5-4cde-b34d-66f50dd992c5")
  override val name: String = "Symbol and Label Legend Paintable"
  override val category: DemoCategory = DemoCategory.Paintables
  override val quality: DemoQuality = DemoQuality.High
  override val variabilityType: VariabilityType = VariabilityType.Stable

  override fun prepareDemo(configuration: PredefinedConfiguration<Nothing>?): MeisterchartsDemo {
    return MeisterchartsDemo(this) {
      meistercharts {
        configure {
          layers.addClearBackground()

          val paintable = SymbolAndLabelLegendPaintable.rectangles(
            labels = object : SizedProvider1<String, ChartSupport> {
              override fun valueAt(index: Int, param1: ChartSupport): String {
                return "Text @ $index"
              }

              override fun size(param1: ChartSupport): Int {
                return 8
              }
            },
            symbolColors = Theme.chartColors.resolve()
          )

          layers.addLayer(object : AbstractLayer() {
            override val type: LayerType = LayerType.Content

            override fun paint(paintingContext: LayerPaintingContext) {
              val gc = paintingContext.gc

              gc.translateToCenter()
              gc.paintMark()

              gc.saved {
                paintable.paint(paintingContext)
              }

              gc.stroke(Color.orange)
              gc.strokeRect(paintable.boundingBox(paintingContext))
            }
          })


          configurable("Symbol Label Gap", paintable.configuration::symbolLabelGap) {
            max = 30.0
          }
          configurable("Font", paintable.configuration::textFont)
          configurable("Entries Gap", paintable.configuration::entriesGap) {
            max = 50.0
          }

          configurableColor("Label Color", Palette.defaultGray()) {
            onChange {
              paintable.configuration.labelColors = MultiProvider.always(it)
              markAsDirty()
            }
          }
          configurableSize("Symbol Size", Size.PX_16) {
            onChange {
              paintable.configuration.symbols = SymbolAndLabelLegendPaintable.defaultSymbols(it, Theme.chartColors.resolve())
              markAsDirty()
            }
          }

        }
      }
    }
  }
}

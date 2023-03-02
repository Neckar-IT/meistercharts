package com.meistercharts.demo.descriptors

import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.canvas.ChartSupport
import com.meistercharts.canvas.paintMark
import com.meistercharts.algorithms.layers.legend.SymbolAndLabelLegendPaintable
import com.meistercharts.canvas.saved
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableColor
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.configurableFontProvider
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
class SymbolAndLabelLegendPaintableDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Symbol and Label Legend Paintable"
  override val category: DemoCategory = DemoCategory.Paintables

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
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
            symbolColors = Theme.chartColors.invoke()
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


          configurableDouble("Symbol Label Gap", paintable.configuration::symbolLabelGap) {
            max = 30.0
          }
          configurableFontProvider("Font", paintable.configuration::textFont)
          configurableDouble("Entries Gap", paintable.configuration::entriesGap) {
            max = 50.0
          }

          configurableColor("Label Color", Palette.defaultGray) {
            onChange {
              paintable.configuration.labelColors = MultiProvider.always(it)
              markAsDirty()
            }
          }
          configurableSize("Symbol Size", Size.PX_16) {
            onChange {
              paintable.configuration.symbols = SymbolAndLabelLegendPaintable.defaultSymbols(it, Theme.chartColors.invoke())
              markAsDirty()
            }
          }

        }
      }
    }
  }
}

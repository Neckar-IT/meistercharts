package com.meistercharts.demo.descriptors

import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.algorithms.painter.UrlPaintable
import com.meistercharts.canvas.FontDescriptorFragment
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableDouble
import com.meistercharts.model.Direction
import com.meistercharts.model.Size

/**
 *
 */
class GlobalAlphaDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Global Alpha"
  override val category: DemoCategory = DemoCategory.LowLevelTests

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {
        configure {
          layers.addClearBackground()

          val paintable = UrlPaintable.fixedSize("https://a.tile.openstreetmap.org/12/2138/1420.png", Size(256.0, 256.0))

          val layer = object : AbstractLayer() {
            var alpha = 0.6

            override val type: LayerType = LayerType.Content

            override fun paint(paintingContext: LayerPaintingContext) {
              val gc = paintingContext.gc
              gc.globalAlpha = alpha

              gc.fill(Color.red)
              gc.font(FontDescriptorFragment.XL)
              gc.fillText("Hello World", 10.0, 10.0, Direction.TopLeft)

              gc.fillRect(10.0, 100.0, 300.0, 150.0)

              paintable.paint(paintingContext, 10.0, 250.0)
            }
          }
          layers.addLayer(layer)

          configurableDouble("Global Alpha", layer::alpha)
        }
      }
    }
  }
}

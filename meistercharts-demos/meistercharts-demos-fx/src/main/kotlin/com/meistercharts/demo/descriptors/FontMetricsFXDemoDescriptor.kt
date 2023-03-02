package com.meistercharts.demo.descriptors

import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.canvas.FontDescriptorFragment
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableFont
import com.meistercharts.fx.native

/**
 *
 */
class FontMetricsFXDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Fx Font Metrics"
  override val category: DemoCategory = DemoCategory.Platform

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {
        configure {
          layers.addClearBackground()

          val myLayer = object : AbstractLayer() {
            var fontFragment = FontDescriptorFragment.L

            override val type: LayerType = LayerType.Content

            override fun paint(paintingContext: LayerPaintingContext) {
              val gcCommon = paintingContext.gc
              gcCommon.translateToCenter()

              val gcFx = gcCommon.native()
              gcFx.font(fontFragment)

              val fxFontMetrics = gcFx.getFxFontMetrics()

              gcFx.stroke(Color.silver)
              gcFx.strokeLine(-10.0, 0.0, 100.0, 0.0)

              //Ascent
              fxFontMetrics.ascent.toDouble().let {
                gcFx.strokeLine(-10.0, -it, 120.0, -it)
              }
              fxFontMetrics.maxAscent.toDouble().let {
                gcFx.strokeLine(-10.0, -it, 120.0, -it)
              }

              fxFontMetrics.descent.toDouble().let {
                gcFx.strokeLine(-10.0, it, 120.0, it)
              }
              fxFontMetrics.maxDescent.toDouble().let {
                gcFx.strokeLine(-10.0, it, 120.0, it)
              }

              gcFx.stroke(Color.black)
              gcFx.context.fillText("xHÁp", 0.0, 0.0)
            }
          }
          layers.addLayer(myLayer)

          configurableFont("font", myLayer::fontFragment)
        }
      }
    }
  }
}

package com.meistercharts.demo.descriptors

import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.canvas.Image
import com.meistercharts.canvas.devicePixelRatio
import com.meistercharts.canvas.loadImage
import com.meistercharts.canvas.paintLocation
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableBoolean
import com.meistercharts.demo.configurableDouble

/**
 *
 */
class PaintPixelPerfectDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Paint Pixel Perfect"
  override val description: String = "Paints a 360x360 pixel sized image using paintImagePixelPerfect.<br/>You may change that behavior by toggling the corresponding check box in the configuration pane."
  override val category: DemoCategory = DemoCategory.LowLevelTests

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {
        configure {
          layers.addClearBackground()

          val layer = object : AbstractLayer() {
            override val type: LayerType = LayerType.Content

            var x = 10.0
            var y = 10.0
            var pixelPerfect = true

            var image: Image? = null

            init {
              loadImage("https://neckar.it/logo/social-media/nit-logo-n-neg_360x360_facebook_insta.png") {
                image = it
                markAsDirty()
              }
            }


            override fun paint(paintingContext: LayerPaintingContext) {
              val gc = paintingContext.gc

              val devicePixelRatio = paintingContext.chartSupport.devicePixelRatio

              gc.paintLocation(x, y)

              image?.let {
                if (pixelPerfect) {
                  gc.paintImagePixelPerfect(it, x, y)
                } else {
                  gc.paintImage(it, x, y, it.size.width / devicePixelRatio, it.size.height / devicePixelRatio)
                }
              }
            }
          }
          layers.addLayer(layer)

          configurableDouble("X", layer::x) {
            step = 0.1
            max = 1000.0
          }
          configurableDouble("Y", layer::y) {
            step = 0.1
            max = 1000.0
          }
          configurableBoolean("Pixel Perfect", layer::pixelPerfect)
        }
      }
    }
  }
}

package com.meistercharts.demo.descriptors

import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.model.Direction
import it.neckar.open.kotlin.lang.fromBase64
import it.neckar.open.kotlin.lang.toBase64

/**
 * A simple hello world demo
 */
class Base64EncodingDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Base64"
  override val description: String = "Shows Base64 Encoding/Decoding"
  override val category: DemoCategory = DemoCategory.Platform

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {
        configure {
          layers.addClearBackground()
          layers.addLayer(object : AbstractLayer() {
            override val type: LayerType = LayerType.Content

            override fun paint(paintingContext: LayerPaintingContext) {
              val base64 = "Hello World".toBase64()
              val reverted = base64.fromBase64()

              val gc = paintingContext.gc
              gc.translateToCenter()

              gc.fillText(base64, 0.0, 0.0, Direction.BottomLeft)
              gc.fillText(reverted.contentToString(), 0.0, 0.0, Direction.TopLeft)
            }
          })
        }
      }
    }
  }
}

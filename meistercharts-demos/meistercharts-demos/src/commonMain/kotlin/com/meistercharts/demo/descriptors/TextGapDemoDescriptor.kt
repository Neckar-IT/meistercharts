package com.meistercharts.demo.descriptors

import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.canvas.FontDescriptor
import com.meistercharts.canvas.FontDescriptorFragment
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.configurableEnum
import com.meistercharts.demo.configurableFont
import com.meistercharts.model.Direction

class TextGapDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Text Gap"
  override val description: String = "## Gap between anchor and text"
  override val category: DemoCategory = DemoCategory.Text

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {
        configure {
          layers.addClearBackground()
          val layer = MyTextLayer()
          layers.addLayer(layer)

          configurableEnum("Anchor Direction", layer.anchorDirection, Direction.values()) {
            onChange {
              layer.anchorDirection = it
              markAsDirty()
            }
          }

          configurableDouble("Gap Horizontal", layer::gapHorizontal) {
            max = 50.0
          }
          configurableDouble("Gap Vertical", layer::gapVertical) {
            max = 50.0
          }

          configurableFont("Font", layer::font) {
          }
        }
      }
    }
  }
}

private class MyTextLayer : AbstractLayer() {
  override val type: LayerType
    get() = LayerType.Content

  var anchorDirection = Direction.Center
  var font: FontDescriptorFragment = FontDescriptor.XL
  var gapHorizontal: Double = 0.0
  var gapVertical: Double = 0.0

  override fun paint(paintingContext: LayerPaintingContext) {
    val gc = paintingContext.gc

    val y = gc.height / 2.0
    val x = gc.width / 2.0

    gc.strokeLine(0.0, y, gc.width, y)
    gc.strokeLine(x, 0.0, x, gc.height)

    //translate to the center
    // gc.translate(x, y)

    gc.font(font)
    gc.fillText("Hello World Gap", x, y, anchorDirection, gapHorizontal, gapVertical)
  }
}

package com.meistercharts.demo.descriptors

import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.canvas.FontDescriptorFragment
import com.meistercharts.canvas.paintTextBox
import com.meistercharts.canvas.saved
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.configurableEnum
import com.meistercharts.demo.configurableFont
import com.meistercharts.demo.configurableSize
import com.meistercharts.model.Direction
import com.meistercharts.model.Insets
import com.meistercharts.model.Size
import com.meistercharts.resources.Icons
import com.meistercharts.style.BoxStyle

/**
 *
 */
class AnchorDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Anchor Demo"

  //language=HTML
  override val description: String = "How to configure the anchor"
  override val category: DemoCategory = DemoCategory.Calculations

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {
        configure {
          layers.addClearBackground()
          val anchorLayer = AnchorLayer()
          layers.addLayer(anchorLayer)


          configurableEnum("Anchor Direction", anchorLayer::anchorDirection, enumValues()) {
          }

          configurableDouble("Anchor Gap Horizontal", anchorLayer::anchorGapHorizontal) {
            max = 60.0
          }
          configurableDouble("Anchor Gap Vertical", anchorLayer::anchorGapVertical) {
            max = 60.0
          }

          configurableSize("Image size", anchorLayer::imageSize) {
          }

          configurableDouble("Box Insets", anchorLayer.boxStyle.padding.top) {
            max = 30.0

            onChange {
              anchorLayer.boxStyle.padding = Insets.of(it)
              markAsDirty()
            }
          }

          configurableFont("Font", property = anchorLayer::font) {
          }
        }
      }
    }
  }
}

private class AnchorLayer : AbstractLayer() {
  var anchorDirection: Direction = Direction.Center
  var anchorGapHorizontal: Double = 0.0
  var anchorGapVertical: Double = 0.0
  var font: FontDescriptorFragment = FontDescriptorFragment.XL
  var imageSize = Size.PX_50
  val boxStyle = BoxStyle.gray

  override val type: LayerType
    get() = LayerType.Content

  override fun paint(paintingContext: LayerPaintingContext) {
    val gc = paintingContext.gc
    gc.stroke(Color.silver)

    //Center with text box
    gc.saved {
      val y = gc.height / 2.0
      val x = gc.width / 2.0

      gc.strokeLine(0.0, y, gc.width, y)
      gc.strokeLine(x, 0.0, x, gc.height)

      //translate to the center
      gc.translate(x, y)

      //Paint the text
      gc.font(font)
      gc.paintTextBox("This is the painted text", anchorDirection, anchorGapHorizontal, anchorGapVertical, boxStyle, Color.blueviolet)
    }

    //FILL TEXT
    gc.saved {
      //Stroke the cross
      val y = gc.height / 4.0
      val x = gc.width / 4.0

      gc.strokeLine(0.0, y, gc.width, y)
      gc.strokeLine(x, 0.0, x, gc.height)

      //translate to the center
      gc.translate(x, y)

      //Paint the text
      gc.font(font)
      gc.fillText("This is the painted text", 0.0, 0.0, anchorDirection, anchorGapHorizontal, anchorGapVertical)
    }

    //STROKE TEXT
    gc.saved {
      //Stroke the cross
      val y = gc.height / 4.0
      val x = gc.width / 4.0 * 3

      //only one line necessary
      gc.strokeLine(x, 0.0, x, gc.height)

      //translate to the center
      gc.translate(x, y)

      //Paint the text
      gc.font(font)
      gc.strokeText("This is the painted text", 0.0, 0.0, anchorDirection, anchorGapHorizontal, anchorGapVertical)
    }

    //image
    gc.saved {
      //Stroke the cross
      val y = gc.height / 4.0 * 3
      val x = gc.width / 4.0

      //only one line necessary
      gc.strokeLine(0.0, y, gc.width, y)

      //translate to the center
      gc.translate(x, y)

      //Paint the text
      gc.font(font)

      Icons.home(imageSize).paintInBoundingBox(paintingContext, 0.0, 0.0, anchorDirection, anchorGapHorizontal, anchorGapVertical)
    }

    //image + text
    gc.saved {
      //Stroke the cross
      val y = gc.height / 4.0 * 3
      val x = gc.width / 4.0 * 3

      //no line necessary

      //translate to the center
      gc.translate(x, y)

      //Paint the text
      gc.font(font)

      Icons.home(imageSize)
        .paintInBoundingBox(paintingContext, 0.0, 0.0, anchorDirection, anchorGapHorizontal, anchorGapVertical)
      gc.fillText("This is the painted text", 0.0, 0.0, anchorDirection, anchorGapHorizontal, anchorGapVertical)
    }
  }
}

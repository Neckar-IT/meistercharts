package com.meistercharts.demo.descriptors

import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.canvas.FontDescriptorFragment
import com.meistercharts.canvas.saved
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableFont
import com.meistercharts.model.Direction

/**
 *
 */
class TextTickAlignmentDemoDescriptor : ChartingDemoDescriptor<Any> {
  override val name: String = "Tick/Text Alignment"
  override val category: DemoCategory = DemoCategory.Text

  //language=HTML
  override val description: String = """
    Visualizes the tick alignments.

    <h3>Left side: Potential UTF-8 Signs</h3>
    <h3>Right side: Alignment to a tick (CenterLeft)</h3>
  """.trimIndent()

  override fun createDemo(configuration: PredefinedConfiguration<Any>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {
        configure {
          layers.addClearBackground()

          val myLayer = object : AbstractLayer() {
            override val type: LayerType = LayerType.Content

            val possibleChars = listOf(
              "\u2022",
              "\u00b7",
              "\u002d",
              "\u1806",
              "\u2010",
              "\u2011",
              "\u2027",
              "\u2043",
              "\ufe63",
              "\uff0d"
            )

            var fontDescriptor: FontDescriptorFragment = FontDescriptorFragment.empty

            override fun paint(paintingContext: LayerPaintingContext) {
              val gc = paintingContext.gc
              gc.font(fontDescriptor)

              val translationForNextLine = fontDescriptor.size?.size ?: 10.0 * 1.2
              gc.translate(40.0, 20.0)

              gc.saved {
                possibleChars.forEach {
                  gc.translate(0.0, 10.0 + translationForNextLine)
                  gc.fillText("${it}3.0", 0.0, 0.0, Direction.CenterLeft)
                }
              }

              //Left side with alignment center to tick
              gc.translate(150.0, 10 + translationForNextLine)
              gc.strokeLine(-10.0, 0.0, 0.0, 0.0)
              gc.fillText("3.0", 0.0, 0.0, Direction.CenterLeft)

              gc.translate(0.0, 10.0 + translationForNextLine)
              gc.strokeLine(-10.0, 0.0, 0.0, 0.0)
              gc.fillText("Hello", 0.0, 0.0, Direction.CenterLeft)

              gc.translate(0.0, 10.0 + translationForNextLine)
              gc.strokeLine(-10.0, 0.0, 0.0, 0.0)
              gc.fillText("Á", 0.0, 0.0, Direction.CenterLeft)
            }
          }
          layers.addLayer(myLayer)

          configurableFont("Font", myLayer::fontDescriptor)
        }
      }
    }
  }
}

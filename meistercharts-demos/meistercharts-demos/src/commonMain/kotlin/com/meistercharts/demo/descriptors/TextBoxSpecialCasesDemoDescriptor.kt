package com.meistercharts.demo.descriptors

import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.canvas.paintTextBox
import com.meistercharts.canvas.saved
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.model.Direction
import com.meistercharts.style.BoxStyle

/**
 */
class TextBoxSpecialCasesDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Text Box - Special cases"
  override val description: String = "## Text Boxes"
  override val category: DemoCategory = DemoCategory.Text

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {
        configure {
          layers.addClearBackground()
          val layer = object : AbstractLayer() {
            override val type: LayerType = LayerType.Content


            val leftLabelAnchorDirection = Direction.TopRight
            val textBoxAnchorDirection = Direction.TopLeft


            override fun paint(paintingContext: LayerPaintingContext) {
              val gc = paintingContext.gc
              gc.translateToCenterX()
              gc.translate(0.0, 20.0)
              gc.fillText("Empty string:", 0.0, 0.0, leftLabelAnchorDirection, 10.0)
              gc.saved {
                gc.paintTextBox("", textBoxAnchorDirection, boxStyle = BoxStyle.gray)
              }

              gc.translate(0.0, 40.0)
              gc.fillText("Blank string:", 0.0, 0.0, leftLabelAnchorDirection, 10.0)
              gc.saved {
                gc.paintTextBox(" ", textBoxAnchorDirection, boxStyle = BoxStyle.gray)
              }

              gc.translate(0.0, 40.0)
              gc.fillText("3 lines, empty in the middle:", 0.0, 0.0, leftLabelAnchorDirection, 10.0)
              gc.saved {
                gc.paintTextBox(listOf("first line", "", "third line"), textBoxAnchorDirection, boxStyle = BoxStyle.gray)
              }

              gc.translate(0.0, 120.0)
              gc.fillText("3 lines, 2 blanks:", 0.0, 0.0, leftLabelAnchorDirection, 10.0)
              gc.saved {
                gc.paintTextBox(listOf("first line", "", ""), textBoxAnchorDirection, boxStyle = BoxStyle.gray)
              }

              gc.translate(0.0, 120.0)
              gc.fillText("3 lines, blank in the middle:", 0.0, 0.0, leftLabelAnchorDirection, 10.0)
              gc.saved {
                gc.paintTextBox(listOf("first line", "  ", "third line"), textBoxAnchorDirection, boxStyle = BoxStyle.gray)
              }

              gc.translate(0.0, 120.0)
              gc.fillText("3 lines:", 0.0, 0.0, leftLabelAnchorDirection, 10.0)
              gc.saved {
                gc.paintTextBox(listOf("first line", "second line", "third line"), textBoxAnchorDirection, boxStyle = BoxStyle.gray)
              }

              gc.translate(0.0, 120.0)
              gc.fillText("3 blank lines:", 0.0, 0.0, leftLabelAnchorDirection, 10.0)
              gc.saved {
                gc.paintTextBox(listOf(" ", " ", " "), textBoxAnchorDirection, boxStyle = BoxStyle.gray)
              }

              gc.translate(0.0, 120.0)
              gc.fillText("3 empty lines:", 0.0, 0.0, leftLabelAnchorDirection, 10.0)
              gc.saved {
                gc.paintTextBox(listOf("", "", ""), textBoxAnchorDirection, boxStyle = BoxStyle.gray)
              }

              gc.translate(0.0, 120.0)
              gc.fillText("empty lines list:", 0.0, 0.0, leftLabelAnchorDirection, 10.0)
              gc.saved {
                gc.paintTextBox(listOf(), textBoxAnchorDirection, boxStyle = BoxStyle.gray)
              }
            }
          }
          layers.addLayer(layer)
        }
      }
    }
  }
}

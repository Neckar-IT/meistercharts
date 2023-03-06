/**
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

import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.canvas.CanvasRenderingContext
import com.meistercharts.canvas.FontDescriptor
import com.meistercharts.canvas.FontDescriptorFragment
import com.meistercharts.canvas.FontMetrics
import com.meistercharts.canvas.paintTextBox
import com.meistercharts.canvas.saved
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableBoolean
import com.meistercharts.demo.configurableFont
import com.meistercharts.demo.configurableList
import com.meistercharts.fonts.FontCenterAlignmentStrategy
import com.meistercharts.model.Coordinates
import com.meistercharts.model.Direction
import com.meistercharts.model.Rectangle
import com.meistercharts.style.BoxStyle
import it.neckar.open.unit.other.px

/**
 * Demos that visualizes the functionality of a multi line message
 */
class TextRenderDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Text Render"

  //language=HTML
  override val description: String = """
    |<h3>Text / font rendering</h3>
    |
    |  Shows how the texts are rendered and calculated
    |
    |  <p>Uses all four alignments</p>
    |  <ul>
    |  <li>Baseline</li>
    |  <li>Center</li>
    |  <li>Top</li>
    |  <li>Bottom</li>
    |  </ul>
    |
    | <h3>Legend</h3>
    | <ul>
    | <li>Blue box: Text size as calculated by canvas</li>
    | <li>Orange lines: Text *height* as calculated by font metrics</li>
    | <li>Red line: The line the text is placed relative to</li>
    | <li>Gray lines: The "x" and "H" lines</li>
    | </ul>
    |
    |
    """.trimMargin()
  override val category: DemoCategory = DemoCategory.Text

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {
        configure {
          layers.addClearBackground()
          val myLayer = MyLayer()
          layers.addLayer(myLayer)

          // text samples taken from http://generator.lorem-ipsum.info/
          configurableList(
            "Sample text", "xHpÁÄgqÅÁqÜgÖfÄPß", listOf(
              "ÄMgFwqMÖBpÅÁqÜgÖfÄPq",
              "與朋友交而不信乎", // Chinese
              "載ほうトラ中保ぱ残摯芸級業へ", // Japanese
              "Лорем ипсум долор", // Cyrillic
              "Λορεμ ιπσθμ δολορ σιτ", // Greek
              "լոռեմ իպսում դոլոռ", // Armenian
              "ლორემ იფსუმ", // Georgian
              "जिसकी पढाए करेसाथ", // Hindi
              "복수정당제는 보장된다", // Korean
              "بـ. شيء قدما بتطويق العالم بل, أي ", // Arabic
              "מונחונים מאמרשיחהצפה על כתב, או לוח" // Hebrew
            )
          ) {
            onChange {
              myLayer.text = it
              markAsDirty()
            }
          }

          configurableBoolean("Paint Canvas Text Size Box", myLayer::paintCanvasTextSizeBox)

          configurableFont("Font", myLayer::font) {
          }
        }
      }
    }
  }
}


class MyLayer : AbstractLayer() {
  override val type: LayerType
    get() = LayerType.Content


  var text: String = "configureMe"

  var font: FontDescriptorFragment = FontDescriptor.Default

  private val insetsLeft = 100.0

  var paintCanvasTextSizeBox = true


  override fun paint(paintingContext: LayerPaintingContext) {
    val gc = paintingContext.gc
    gc.font(font)

    val fontMetrics = gc.getFontMetrics()
    val alignmentCorrectionInformation = fontMetrics.alignmentCorrectionInformation

    gc.saved {
      gc.font = FontDescriptor.Default

      gc.paintTextBox(
        listOf(
          "Alignment correction:",
          "Top: ${alignmentCorrectionInformation.top}",
          "Center: ${alignmentCorrectionInformation.center}",
          "Bottom: ${alignmentCorrectionInformation.bottom}",
        ), Direction.TopLeft, boxStyle = BoxStyle.none
      )
    }

    gc.translate(insetsLeft, 0.0)
    gc.translate(0.0, gc.height / 5.0)

    //Paint using base line
    gc.paintDebugTextPanel("Baseline$text", Direction.BaseLineLeft, fontMetrics, 0.0)
    gc.translate(0.0, gc.height / 5.0)

    gc.paintDebugTextPanel("Center$text", Direction.CenterLeft, fontMetrics, fontMetrics.capitalHLine / 2.0 - FontCenterAlignmentStrategy.calculateCenterOffset(fontMetrics.capitalHLine))
    gc.translate(0.0, gc.height / 5.0)

    gc.paintDebugTextPanel("Bottom$text", Direction.BottomLeft, fontMetrics, -fontMetrics.pLine)
    gc.translate(0.0, gc.height / 5.0)

    gc.paintDebugTextPanel("Top$text", Direction.TopLeft, fontMetrics, fontMetrics.accentLine)
    gc.translate(0.0, gc.height / 5.0)
  }

  /**
   * Paints the text debug line
   */
  private fun CanvasRenderingContext.paintDebugTextPanel(text: String, textAnchorDirection: Direction, fontMetrics: FontMetrics, yAnchorForHelpLines: @px Double) {
    lineWidth = 0.5

    val textSize = calculateTextSize(text)

    //The text size as calculated by the canvas
    if (paintCanvasTextSizeBox) {
      stroke(Color.blue)
      strokeRect(Rectangle(Coordinates(0.0, -fontMetrics.accentLine + yAnchorForHelpLines), textSize))
    }

    //The text size as calculated by the font metrics
    stroke(Color.orange)
    strokeLine(0.0, -fontMetrics.accentLine + yAnchorForHelpLines, textSize.width, -fontMetrics.accentLine + yAnchorForHelpLines)
    strokeLine(0.0, -fontMetrics.accentLine + yAnchorForHelpLines + fontMetrics.totalHeight, textSize.width, -fontMetrics.accentLine + yAnchorForHelpLines + fontMetrics.totalHeight)

    //the H Line
    fontMetrics.capitalHLine.let {
      stroke(Color.silver)
      strokeLine(0.0, -it + yAnchorForHelpLines, textSize.width, -it + yAnchorForHelpLines)
    }

    //the x Line
    fontMetrics.xLine.let {
      stroke(Color.silver)
      strokeLine(0.0, -it + yAnchorForHelpLines, textSize.width, -it + yAnchorForHelpLines)
    }

    //The alignment line
    stroke(Color.red)
    strokeLine(-insetsLeft, 0.0, width, 0.0)


    //The text itself
    fill(Color.black)
    fillText(text, 0.0, 0.0, textAnchorDirection)
  }
}

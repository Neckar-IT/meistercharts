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
import com.meistercharts.canvas.BorderRadius
import com.meistercharts.canvas.FontDescriptor
import com.meistercharts.canvas.FontDescriptorFragment
import com.meistercharts.canvas.paintTextBox
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableBoolean
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.configurableEnum
import com.meistercharts.demo.configurableFont
import com.meistercharts.demo.configurableList
import com.meistercharts.model.Direction
import com.meistercharts.model.Insets
import com.meistercharts.style.BoxStyle

/**
 */
class TextBoxDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Text Box"
  override val description: String = "## Text Boxes"
  override val category: DemoCategory = DemoCategory.Text

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {
        configure {
          layers.addClearBackground()
          val layer = MyTextBoxLayer()
          layers.addLayer(layer)

          // text samples taken from http://generator.lorem-ipsum.info/
          configurableList(
            "Sample text", "ÄMgFwqMÖBpÅÁqÜgÖfÄPq", listOf(
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
              layer.text = it
              markAsDirty()
            }
          }

          configurableBoolean("Prevent overflow", layer::preventViewPortOverflow)

          configurableDouble("Max string width", layer::maxStringWidth) {
            min = 1.0
            max = 1000.0
          }

          configurableDouble("Translate x", layer::translateX) {
            min = -1000.0
            max = +1000.0
          }

          configurableDouble("Translate y", layer::translateY) {
            min = -1000.0
            max = +1000.0
          }

          configurableEnum("Anchor Direction", layer.anchorDirection, Direction.entries) {
            onChange {
              layer.anchorDirection = it
              markAsDirty()
            }
          }

          configurableDouble("Anchor gap Horizontal", layer::anchorGapHorizontal) {
            min = 0.0
            max = 100.0
          }
          configurableDouble("Anchor gap Vertical", layer::anchorGapVertical) {
            min = 0.0
            max = 100.0
          }

          configurableDouble("Box padding", layer.boxStyle.padding.top) {
            max = 30.0

            onChange {
              layer.boxStyle.padding = Insets.of(it)
              markAsDirty()
            }
          }

          configurableDouble("Border width", layer.boxStyle::borderWidth) {
            min = 0.0
            max = 20.0
          }

          configurableDouble("Rounded", layer.boxStyle.radii?.topLeft ?: 0.0) {
            max = 30.0

            onChange {
              if (it == 0.0) {
                layer.boxStyle.radii = null
              } else {
                layer.boxStyle.radii = BorderRadius.of(it)
              }

              markAsDirty()
            }
          }

          configurableFont("Font", layer::font) {
          }
        }
      }
    }
  }
}

private class MyTextBoxLayer : AbstractLayer() {
  override val type: LayerType
    get() = LayerType.Content

  var preventViewPortOverflow = true
  var translateX = 0.0
  var translateY = 0.0
  var anchorDirection = Direction.Center
  var anchorGapHorizontal = 0.0
  var anchorGapVertical = 0.0
  var font: FontDescriptorFragment = FontDescriptor.XL
  var maxStringWidth = 1000.0

  val boxStyle = BoxStyle(fill = Color.silver, borderColor = Color.red, borderWidth = 2.0)

  var text = "Hello World"


  override fun paint(paintingContext: LayerPaintingContext) {
    val gc = paintingContext.gc

    val y = gc.height / 2.0 + translateY
    val x = gc.width / 2.0 + translateX

    gc.strokeLine(0.0, y, gc.width, y)
    gc.strokeLine(x, 0.0, x, gc.height)

    //translate to the center
    gc.translate(x, y)

    gc.font(font)
    gc.paintTextBox(text, anchorDirection, anchorGapHorizontal, anchorGapVertical, boxStyle, Color.azure, maxStringWidth)
  }
}

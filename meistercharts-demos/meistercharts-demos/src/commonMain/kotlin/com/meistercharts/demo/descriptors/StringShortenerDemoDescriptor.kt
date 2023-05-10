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
import com.meistercharts.canvas.FontDescriptor
import com.meistercharts.canvas.FontDescriptorFragment
import com.meistercharts.canvas.StringShortener
import com.meistercharts.canvas.paintTextBox
import com.meistercharts.canvas.strokeRect
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableFont
import com.meistercharts.demo.configurableInt
import com.meistercharts.demo.configurableListWithProperty
import com.meistercharts.model.Direction

class StringShortenerDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "String shortener"
  override val category: DemoCategory = DemoCategory.Text

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {
        configure {
          layers.addClearBackground()
          val layer = MyStringShortenerLayer()
          layers.addLayer(layer)

          configurableListWithProperty(
            "String shortener", layer::stringShortener, listOf(
              StringShortener.NoOp,
              StringShortener.TruncateToLength,
              StringShortener.TruncateCenterToLength
            )
          ) {
            converter {
              return@converter when {
                it == StringShortener.NoOp -> "None"
                it == StringShortener.TruncateToLength -> "Truncate"
                it == StringShortener.TruncateCenterToLength -> "Truncate center"
                else -> it.toString()
              }
            }
          }

          configurableInt("Max Chars", layer::charactersCount) {
            max = 40
          }

          configurableFont("Font", layer::font) {}
        }
      }
    }
  }
}

private class MyStringShortenerLayer : AbstractLayer() {
  override val type: LayerType
    get() = LayerType.Content

  var charactersCount = 7

  var anchorDirection = Direction.CenterLeft
  var font: FontDescriptorFragment = FontDescriptor.XL
  var gapHorizontal: Double = 0.0
  var gapVertical: Double = 0.0


  var stringShortener: StringShortener = StringShortener.TruncateToLength

  override fun paint(paintingContext: LayerPaintingContext) {
    val gc = paintingContext.gc

    val y = gc.height / 2.0
    val x = gc.width / 2.0

    gc.stroke(Color.silver)
    gc.strokeLine(0.0, y, gc.width, y)
    gc.strokeLine(x, 0.0, x, gc.height)


    gc.font(font)
    val text = stringShortener.shorten("Hello MeisterCharts by Neckar IT", charactersCount)

    if (text == null) {
      gc.fillText("text could not be shortened!", x, y, anchorDirection, gapHorizontal, gapHorizontal)
      return
    }

    gc.fillText(text, x, y, anchorDirection, gapHorizontal, gapVertical)

    val textWidth = gc.calculateTextWidth(text)
    val actualTextSize = gc.calculateTextSize(text)

    gc.font(FontDescriptorFragment.DefaultSize)
    gc.paintTextBox(
      listOf(
        "Text Width: $textWidth",
        "Actual text size: $actualTextSize"
      ), Direction.TopLeft
    )

    gc.stroke(Color.orange)
    gc.strokeRect(x, y, actualTextSize.width, actualTextSize.height, anchorDirection, gapHorizontal)
  }
}

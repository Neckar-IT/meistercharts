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
import com.meistercharts.canvas.CanvasStringShortener
import com.meistercharts.canvas.DebugFeature
import com.meistercharts.canvas.FontDescriptor
import com.meistercharts.canvas.FontDescriptorFragment
import com.meistercharts.canvas.StringShortener
import com.meistercharts.canvas.paintTextBox
import com.meistercharts.canvas.strokeRect
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.configurableEnum
import com.meistercharts.demo.configurableFont
import com.meistercharts.demo.configurableListWithProperty
import com.meistercharts.model.Direction
import it.neckar.open.unit.other.px

class TextMaxLengthDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Text - with max length"
  override val category: DemoCategory = DemoCategory.Text

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {
        configure {
          debug.enable(DebugFeature.ShowAnchors)
          debug.enable(DebugFeature.ShowMaxTextWidth)

          layers.addClearBackground()
          val layer = MyMaxLengthTextLayer()
          layers.addLayer(layer)

          configurableListWithProperty(
            "String shortener", layer::canvasStringShortener, listOf(
              CanvasStringShortener.NoOp,
              CanvasStringShortener.AllOrNothing,
              CanvasStringShortener.ExactButSlow(StringShortener.TruncateToLength),
              CanvasStringShortener.ExactButSlow(StringShortener.TruncateCenterToLength),
              CanvasStringShortener.ExactButVerySlow(StringShortener.TruncateToLength),
              CanvasStringShortener.ExactButVerySlow(StringShortener.TruncateCenterToLength),
            )
          ) {
            converter {
              return@converter when (it) {
                CanvasStringShortener.NoOp                -> "None"
                is CanvasStringShortener.ExactButVerySlow -> "Exact but very slow (${it.stringShortener}"
                is CanvasStringShortener.ExactButSlow     -> "Exact but slow (${it.stringShortener}"
                is CanvasStringShortener.AllOrNothing     -> "All or nothing"
                else                                      -> it.toString()
              }
            }
          }


          configurableDouble("Max text width (px)", layer::canvasMaxWidth) {
            step = 0.1
            max = 500.0
          }

          configurableDouble("Max text height (px)", layer::canvasMaxHeight) {
            step = 0.1
            max = 500.0
          }

          configurableEnum("Anchor Direction", layer.anchorDirection, Direction.values()) {
            onChange {
              layer.anchorDirection = it
              markAsDirty()
            }
          }

          configurableDouble("Gap Horizontal", layer::gapHorizontal) {
            max = 50.0
          }

          configurableFont("Font", layer::font) {
          }

        }
      }
    }
  }
}

private class MyMaxLengthTextLayer : AbstractLayer() {
  override val type: LayerType
    get() = LayerType.Content

  var anchorDirection = Direction.CenterLeft
  var font: FontDescriptorFragment = FontDescriptor.XL
  var gapHorizontal: Double = 0.0
  var gapVertical: Double = 0.0

  var canvasMaxWidth: @px Double = 500.0
  var canvasMaxHeight: @px Double = 35.0

  var canvasStringShortener: CanvasStringShortener = CanvasStringShortener.ExactButSlow(StringShortener.TruncateToLength)

  override fun paint(paintingContext: LayerPaintingContext) {
    val gc = paintingContext.gc

    val y = gc.height / 2.0
    val x = gc.width / 2.0

    gc.stroke(Color.silver)
    gc.strokeLine(0.0, y, gc.width, y)
    gc.strokeLine(x, 0.0, x, gc.height)

    gc.font(font)

    val text = "Hello MeisterCharts by Neckar IT"
    gc.fillText(text, x, y, anchorDirection, gapHorizontal, gapVertical, canvasMaxWidth, canvasMaxHeight, canvasStringShortener)

    val textWidth = gc.calculateTextWidth(text)
    val actualTextSize = gc.calculateTextSize(text)
    val totalFontHeight = gc.getFontMetrics().totalHeight

    gc.font(FontDescriptorFragment.DefaultSize)

    gc.paintTextBox(
      listOf(
        "Text Width: $textWidth",
        "Actual text size: $actualTextSize",
        "Total font height: $totalFontHeight",
        "Delta height: ${canvasMaxHeight - totalFontHeight}"
      ), Direction.TopLeft
    )

    gc.stroke(Color.orange)
    gc.strokeRect(x, y, actualTextSize.width, actualTextSize.height, anchorDirection, gapHorizontal, gapVertical)
    gc.stroke(Color.blue)
    gc.strokeRect(x, y, canvasMaxWidth, canvasMaxHeight, anchorDirection, gapHorizontal, gapVertical)
  }
}

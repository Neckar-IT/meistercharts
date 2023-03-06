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

class TextWithinBoxDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Text - enforced within box"
  override val category: DemoCategory = DemoCategory.Text

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {
        configure {
          layers.addClearBackground()
          val layer = MyTextWithinArealayer()
          layers.addLayer(layer)


          declare {
            section("Enforced Area")
          }

          configurableDouble("X", layer::enforcedAreaX) {
            max = 1000.0
          }
          configurableDouble("Y", layer::enforcedAreaY) {
            max = 1000.0
          }
          configurableDouble("Width", layer::enforcedAreaWidth) {
            max = 1000.0
          }
          configurableDouble("Height", layer::enforcedAreaHeight) {
            max = 1000.0
          }


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
                CanvasStringShortener.NoOp -> "None"
                is CanvasStringShortener.ExactButVerySlow -> "Exact but very slow (${it.stringShortener}"
                is CanvasStringShortener.ExactButSlow -> "Exact but slow (${it.stringShortener}"
                is CanvasStringShortener.AllOrNothing -> "All or nothing"
                else -> it.toString()
              }
            }
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

          configurableFont("Font", layer::font)
        }
      }
    }
  }
}

class MyTextWithinArealayer : AbstractLayer() {
  override val type: LayerType = LayerType.Content

  var enforcedAreaX: Double = 400.0
  var enforcedAreaY: Double = 400.0
  var enforcedAreaWidth: Double = 300.0
  var enforcedAreaHeight: Double = 500.0

  var anchorDirection = Direction.CenterLeft
  var font: FontDescriptorFragment = FontDescriptor.XL
  var gapHorizontal: Double = 0.0
  var gapVertical: Double = 0.0

  var canvasStringShortener: CanvasStringShortener = CanvasStringShortener.ExactButSlow(StringShortener.TruncateToLength)

  override fun paint(paintingContext: LayerPaintingContext) {
    val gc = paintingContext.gc

    val centerY = gc.height / 2.0
    val centerX = gc.width / 2.0

    gc.stroke(Color.silver)
    gc.strokeLine(0.0, centerY, gc.width, centerY)
    gc.strokeLine(centerX, 0.0, centerX, gc.height)

    gc.font(font)

    val text = "Hello MeisterCharts by Neckar IT"
    gc.fill(Color.black)
    gc.fillTextWithin(text, centerX, centerY, anchorDirection, gapHorizontal, gapVertical, enforcedAreaX, enforcedAreaY, enforcedAreaWidth, enforcedAreaHeight, canvasStringShortener)

    val textWidth = gc.calculateTextWidth(text)
    val actualTextSize = gc.calculateTextSize(text)
    val totalFontHeight = gc.getFontMetrics().totalHeight

    gc.font(FontDescriptorFragment.DefaultSize)

    gc.paintTextBox(
      listOf(
        "Text Width: $textWidth",
        "Actual text size: $actualTextSize",
        "Total font height: $totalFontHeight",
      ), Direction.TopLeft
    )

    gc.stroke(Color.orange)
    gc.strokeRect(centerX, centerY, actualTextSize.width, actualTextSize.height, anchorDirection, gapHorizontal, gapVertical)
    gc.stroke(Color.blue)
    //gc.strokeRect(centerX, centerY, canvasMaxWidth, canvasMaxHeight, anchorDirection, gapHorizontal, gapVertical)

    //Paint the enforced area
    gc.stroke(Color.silver)
    gc.strokeRect(enforcedAreaX, enforcedAreaY, enforcedAreaWidth, enforcedAreaHeight)
  }
}

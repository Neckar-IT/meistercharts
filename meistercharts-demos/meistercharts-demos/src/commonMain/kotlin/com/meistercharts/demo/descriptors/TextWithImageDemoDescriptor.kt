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
import com.meistercharts.canvas.PaintableLocation
import com.meistercharts.canvas.paintTextWithPaintable
import com.meistercharts.canvas.paintable.RectanglePaintable
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
import com.meistercharts.model.Size

/**
 */
class TextWithImageDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Text + Image"
  override val description: String = "## Text Box with image"
  override val category: DemoCategory = DemoCategory.Text

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {
        configure {
          layers.addClearBackground()
          val layer = MyTextWithImageLayer()
          layers.addLayer(layer)

          configurableSize("Image size", layer.imageSize) {
            onChange {
              layer.imageSize = it
              markAsDirty()
            }
          }

          configurableEnum("Image Location", layer.paintableLocation, PaintableLocation.entries) {
            onChange {
              layer.paintableLocation = it
              markAsDirty()
            }
          }

          configurableDouble("Gap", layer::gap) {
            max = 30.0
          }

          configurableFont("Font", layer::font) {
          }
        }
      }
    }
  }
}

private class MyTextWithImageLayer() : AbstractLayer() {
  override val type: LayerType
    get() = LayerType.Content

  var font: FontDescriptorFragment = FontDescriptor.XL
  var imageSize = Size.PX_50
  var gap = 5.0
  var paintableLocation = PaintableLocation.PaintableOutside


  override fun paint(paintingContext: LayerPaintingContext) {
    val gc = paintingContext.gc
    gc.font(font)
    val image = RectanglePaintable(imageSize, Color.blue)

    paint(paintingContext, 1 / 4.0, 1 / 5.0, image, Direction.TopLeft)
    paint(paintingContext, 2 / 4.0, 1 / 5.0, image, Direction.TopCenter)
    paint(paintingContext, 3 / 4.0, 1 / 5.0, image, Direction.TopRight)

    paint(paintingContext, 1 / 4.0, 2 / 5.0, image, Direction.BaseLineLeft)
    paint(paintingContext, 2 / 4.0, 2 / 5.0, image, Direction.BaseLineCenter)
    paint(paintingContext, 3 / 4.0, 2 / 5.0, image, Direction.BaseLineRight)

    paint(paintingContext, 1 / 4.0, 3 / 5.0, image, Direction.CenterLeft)
    paint(paintingContext, 2 / 4.0, 3 / 5.0, image, Direction.Center)
    paint(paintingContext, 3 / 4.0, 3 / 5.0, image, Direction.CenterRight)

    paint(paintingContext, 1 / 4.0, 4 / 5.0, image, Direction.BottomLeft)
    paint(paintingContext, 2 / 4.0, 4 / 5.0, image, Direction.BottomCenter)
    paint(paintingContext, 3 / 4.0, 4 / 5.0, image, Direction.BottomRight)
  }

  private fun paint(paintingContext: LayerPaintingContext, factorX: Double, factorY: Double, image: RectanglePaintable, anchorDirection: Direction) {
    val gc = paintingContext.gc

    gc.saved {
      val x = gc.width * factorX
      val y = gc.height * factorY

      gc.stroke(Color.lightgray)
      gc.strokeLine(0.0, y, gc.width, y)
      gc.strokeLine(x, 0.0, x, gc.height)

      //translate to the center
      gc.translate(x, y)

      paintingContext.paintTextWithPaintable(anchorDirection.name, image, paintableLocation, anchorDirection, gap)
    }
  }
}

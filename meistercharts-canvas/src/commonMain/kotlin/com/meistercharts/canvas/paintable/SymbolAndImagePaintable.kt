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
package com.meistercharts.canvas.paintable

import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.color.Color
import com.meistercharts.annotations.Window
import com.meistercharts.canvas.ConfigurationDsl
import com.meistercharts.canvas.saved
import it.neckar.geometry.Direction
import it.neckar.geometry.Rectangle
import it.neckar.geometry.Size
import com.meistercharts.style.Palette
import it.neckar.open.unit.other.px
import kotlin.math.max

/**
 * A [Paintable] that displays a symbol and an image.
 *
 * So far the symbol is always to the left of the image.
 *
 * The logical center lies between the symbol and the image.
 */
class SymbolAndImagePaintable(
  val symbol: Paintable,
  val image: Paintable,
  styleConfiguration: Style.() -> Unit = {}
) : Paintable {

  val style: Style = Style().also(styleConfiguration)

  override fun boundingBox(paintingContext: LayerPaintingContext): Rectangle {
    val chartSupport = paintingContext.chartSupport

    val symbolBoundingBox = symbol.boundingBox(paintingContext)
    val size = Size(symbolBoundingBox.getWidth() + style.gapHorizontal + style.imageSize.width, max(symbolBoundingBox.getHeight(), style.imageSize.height))
    return Rectangle(-symbolBoundingBox.getWidth() - style.gapHorizontal / 2.0, -size.height / 2.0, size.width, size.height)
  }


  override fun paint(
    paintingContext: LayerPaintingContext,
    x: @Window Double,
    y: @Window Double,
  ) {
    val boundingBox = boundingBox(paintingContext)

    paintingContext.gc.saved {
      symbol.paintInBoundingBox(paintingContext, x, y, Direction.CenterRight, style.gapHorizontal / 2.0, style.gapVertical / 2.0, boundingBox.getWidth(), boundingBox.getHeight())
    }
    image.paintInBoundingBox(paintingContext, x, y, Direction.CenterLeft, style.gapHorizontal / 2.0, style.gapVertical / 2.0, boundingBox.getWidth(), boundingBox.getHeight())
  }

  @ConfigurationDsl
  open class Style {
    /**
     * The size of the image
     */
    var imageSize: Size = Size.PX_24

    /**
     * The color the image should be painted in
     */
    var imageColor: Color = Palette.defaultGray

    /**
     * The gap between the symbol and the image
     */
    var gapHorizontal: @px Double = 5.0
    var gapVertical: @px Double = 5.0
  }
}

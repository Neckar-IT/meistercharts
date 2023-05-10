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
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.canvas.ChartSupport
import com.meistercharts.model.Coordinates
import com.meistercharts.model.Rectangle
import com.meistercharts.model.Size
import it.neckar.open.kotlin.lang.asProvider
import it.neckar.open.unit.other.px

/**
 * Paints a solid rectangle
 */
class RectanglePaintable(
  val width: @px Double,
  val height: @px Double,
  var color: () -> Color,
) : Paintable {

  constructor(size: Size, color: () -> Color) : this(size.width, size.height, color)
  constructor(size: Size, color: Color) : this(size.width, size.height, color.asProvider())

  val boundingBox: Rectangle = Rectangle(Coordinates.origin, Size(width, height))

  override fun boundingBox(paintingContext: LayerPaintingContext): Rectangle {
    return boundingBox
  }

  override fun paint(paintingContext: LayerPaintingContext, x: Double, y: Double) {
    val gc = paintingContext.gc
    gc.fill(color())
    gc.fillRect(x, y, width, height)
  }

  companion object {
    operator fun invoke(
      width: @px Double,
      height: @px Double,
      color: Color,
    ): RectanglePaintable {
      return RectanglePaintable(width, height, color.asProvider())
    }
  }
}

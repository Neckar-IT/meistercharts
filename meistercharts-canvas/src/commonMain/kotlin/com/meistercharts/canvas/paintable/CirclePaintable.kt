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
import com.meistercharts.canvas.fill
import com.meistercharts.color.Color
import com.meistercharts.color.ColorProvider
import it.neckar.geometry.Coordinates
import it.neckar.geometry.Rectangle
import it.neckar.geometry.Size
import it.neckar.open.unit.other.px

/**
 * Paints a solid circle with the given diameter
 */
class CirclePaintable(
  val color: ColorProvider,
  val diameter: @px Double,
) : Paintable {

  val boundingBox: Rectangle = Rectangle(Coordinates.origin, Size(diameter, diameter))

  override fun boundingBox(paintingContext: LayerPaintingContext): Rectangle {
    return boundingBox
  }

  override fun paint(paintingContext: LayerPaintingContext, x: Double, y: Double) {
    val gc = paintingContext.gc
    gc.fill(color)
    gc.fillOvalOrigin(x, y, diameter, diameter)
  }
}

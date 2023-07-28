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

import it.neckar.geometry.AxisSelection
import com.meistercharts.algorithms.layers.LayerPaintingContext
import it.neckar.geometry.Rectangle

/**
 * Paints a paintable mirrored
 * Can invert over both or only one axis
 */
class MirrorPaintable(
  val delegate: Paintable,
  val flippedAxisProvider: () -> AxisSelection
) : Paintable {
  override fun boundingBox(paintingContext: LayerPaintingContext): Rectangle {
    return delegate.boundingBox(paintingContext)
  }

  //TODO implement layout! (if necessary)

  override fun paint(paintingContext: LayerPaintingContext, x: Double, y: Double) {
    val flippedAxis = flippedAxisProvider()
    if (flippedAxis == AxisSelection.None) {
      delegate.paint(paintingContext, x, y)
      return
    }

    val gc = paintingContext.gc
    gc.translate(x, y)

    if (flippedAxis.containsX) {
      gc.scale(-1.0, 1.0)
    }
    if (flippedAxis.containsY) {
      gc.scale(1.0, -1.0)
    }

    delegate.paint(paintingContext, 0.0, 0.0)
  }
}

/**
 * Returns a paintable that paints this paintable flipped on the selected axis
 */
fun Paintable.mirror(flipX: Boolean, flipY: Boolean): Paintable {
  return mirror(AxisSelection.get(flipX, flipY))
}

/**
 * Mirrors along the x-axis
 *
 * M -> W
 */
fun Paintable.mirrorOnX(): Paintable {
  return mirror(AxisSelection.Y)
}

/**
 * Mirrors along the y-axis
 *
 * 3 -> E
 */
fun Paintable.mirrorOnY(): Paintable {
  return mirror(AxisSelection.X)
}

/**
 * Mirrors the given axis
 * @see mirrorOnX
 * @see mirrorOnY
 */
fun Paintable.mirror(flippedAxis: AxisSelection): Paintable {
  if (flippedAxis == AxisSelection.None) {
    return this
  }

  return MirrorPaintable(this) { flippedAxis }
}

/**
 * Flips the paintable
 */
fun Paintable.mirror(flippedAxisProvider: () -> AxisSelection): MirrorPaintable {
  return MirrorPaintable(this, flippedAxisProvider)
}

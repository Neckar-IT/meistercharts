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
package com.meistercharts.algorithms.painter

import com.meistercharts.canvas.CanvasRenderingContext
import com.meistercharts.painter.LinePainter
import it.neckar.open.collections.DoubleArrayList
import it.neckar.open.collections.fastForEachIndexed

/**
 * A class for drawing a single line on a canvas.
 */
class DirectLinePainter(
  snapXValues: Boolean,
  snapYValues: Boolean,
) : AbstractPainter(snapXValues, snapYValues), LinePainter {

  private val xLocations = DoubleArrayList(10)
  private val yLocations = DoubleArrayList(10)

  /**
   * Clears the existing line coordinates.
   */
  override fun begin(gc: CanvasRenderingContext) {
    xLocations.clear()
    yLocations.clear()
  }

  /**
   * Adds coordinates to the line.
   *
   * @param gc The canvas rendering context used for drawing.
   * @param x The x coordinate of the point to be added.
   * @param y The y coordinate of the point to be added.
   */
  override fun addCoordinates(gc: CanvasRenderingContext, x: Double, y: Double) {
    require(x.isFinite()) { "x must be a finite number but was $x" }
    require(y.isFinite()) { "y must be a finite number but was $y" }

    xLocations.add(x)
    yLocations.add(y)
  }

  /**
   * Draws the line on the canvas using the given [CanvasRenderingContext].
   *
   * @param gc The canvas rendering context used for drawing.
   */
  override fun paint(gc: CanvasRenderingContext) {
    if (xLocations.size < 2 || yLocations.size < 2) return

    gc.beginPath()
    gc.moveTo(xLocations[0], yLocations[0])

    xLocations.fastForEachIndexed { index, x ->
      if (index > 0) {
        gc.lineTo(x, yLocations[index])
      }
    }

    gc.stroke()
  }

  override fun toString(): String {
    return "SimpleLinePainter"
  }
}

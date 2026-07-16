/*
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
import com.meistercharts.canvas.layout.buffer.CoordinatesArrayList
import com.meistercharts.painter.LinePainter
import it.neckar.open.annotations.Hot

/**
 * A class for drawing a single line on a canvas.
 */
class DirectLinePainter(
  snapXValues: Boolean,
  snapYValues: Boolean,
) : AbstractPainter(snapXValues, snapYValues), LinePainter {

  private val locations = CoordinatesArrayList(10)

  /**
   * Clears the existing line coordinates.
   */
  @Hot
  override fun begin(gc: CanvasRenderingContext) {
    locations.clear()
  }

  /**
   * Adds coordinates to the line.
   *
   * @param gc The canvas rendering context used for drawing.
   * @param x The x coordinate of the point to be added.
   * @param y The y coordinate of the point to be added.
   */
  @Hot
  override fun addCoordinates(gc: CanvasRenderingContext, x: Double, y: Double) {
    require(x.isFinite()) { "x must be a finite number but was $x" }
    require(y.isFinite()) { "y must be a finite number but was $y" }

    locations.add(x, y)
  }

  /**
   * Draws the line on the canvas using the given [CanvasRenderingContext].
   *
   * @param gc The canvas rendering context used for drawing.
   */
  @Hot
  override fun paint(gc: CanvasRenderingContext) {
    if (locations.size < 2) return

    gc.beginPath()
    gc.moveTo(locations.xAt(0), locations.yAt(0))

    locations.fastForEachIndexed { index, x, y ->
      if (index > 0) {
        gc.lineTo(x, y)
      }
    }

    gc.stroke()
  }

  override fun toString(): String {
    return "DirectLinePainter"
  }
}

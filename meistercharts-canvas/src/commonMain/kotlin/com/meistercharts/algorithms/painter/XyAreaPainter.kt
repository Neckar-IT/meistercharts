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

import com.meistercharts.annotations.Window
import com.meistercharts.canvas.CanvasRenderingContext
import com.meistercharts.color.Color
import it.neckar.geometry.Coordinates
import it.neckar.open.unit.other.px


/**
 * Paints a xy area
 *
 */
@Deprecated("No longer required(?)")
class XyAreaPainter(
  /**
   * The baseline height
   */
  @px @Window
  private val baseLineY: Double, snapXValues: Boolean, snapYValues: Boolean
) : FancyXyLinePainter(snapXValues, snapYValues) {

  private var fill: Color? = null

  /**
   * Stores the first x values to be able to finish the area
   */
  @px
  private var firstX: Double = 0.toDouble()

  override fun addCoordinate(gc: CanvasRenderingContext, @px @Window x: Double, @px @Window y: Double) {
    if (path.isEmpty()) {
      firstX = x
    }

    super.addCoordinate(gc, x, y)
  }

  override fun finish(gc: CanvasRenderingContext) {
    if (path.size < 2) {
      return
    }

    //Fill the area
    val toFill = path.toMutableList()

    val currentPoint = toFill.last()
    toFill.add(Coordinates(currentPoint.x, baseLineY))
    toFill.add(Coordinates(firstX, baseLineY))

    gc.beginPath()
    toFill.forEach {
      gc.lineTo(it)
    }

    fill?.let {
      gc.fill(it)
    }

    gc.fill()

    super.finish(gc)
  }
}

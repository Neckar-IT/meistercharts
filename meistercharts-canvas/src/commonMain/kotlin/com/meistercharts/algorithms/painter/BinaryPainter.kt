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

import com.meistercharts.calc.ChartingUtils
import com.meistercharts.annotations.Window
import com.meistercharts.canvas.CanvasRenderingContext
import com.meistercharts.color.Color
import it.neckar.open.unit.other.px

/**
 * Paints a binary chart (with only two possible values: true/false)
 *
 */
class BinaryPainter(
  snapXValuesToPixel: Boolean,
  snapYValuesToPixel: Boolean,
  /**
   * Snapped baseline y
   */
  @px @Window
  val baseLineY: Double,
  /**
   * The maximum width for the lines.
   * The lines are fitted *within* this width
   */
  @px
  val maxWidth: Double,
  /**
   * The maximum height for the lines.
   * The lines are fitted *within* this height
   */
  @px
  val maxHeight: Double,
) : AbstractXyLinePainter(snapXValuesToPixel, snapYValuesToPixel) {

  var shadowOffsetX: Double = 4.0
  var shadowOffsetY: Double = 4.0

  private val path = Path()

  /**
   * The x of the first point
   */
  private var firstX: Double = 0.0

  /**
   * The optional fill for the area
   */
  var areaFill: Color? = null

  fun reset() {
    firstX = 0.0
    path.beginPath()
  }

  override fun addCoordinate(gc: CanvasRenderingContext, @px @Window x: Double, @px @Window y: Double) {
    val currentPoint = path.currentPointOrNull

    //Ensure the line is always visible
    @Window val snappedX = ChartingUtils.lineWithin(snapXPosition(x), 0.0, maxWidth, lineWidth)

    //Verify that y is always visible
    @Window val snappedY = ChartingUtils.lineWithin(snapYPosition(y), 0.0, maxHeight, lineWidth)

    if (currentPoint == null) {
      //The first point. Start at bottom
      path.moveTo(snappedX, ChartingUtils.lineWithin(snapYPosition(baseLineY), 0.0, maxHeight, lineWidth))

      //Line to first point
      path.lineTo(snappedX, snappedY)

      //Remember the first x point to be able to close the path
      firstX = snappedX

    } else {
      @px @Window val lastY = currentPoint.y

      if (lastY != snappedY) {
        //The y value has changed. continue line on last y value
        path.lineTo(snappedX, lastY)
      }
      path.lineTo(snappedX, snappedY)
    }
  }

  override fun finish(gc: CanvasRenderingContext) {
    if (path.isEmpty()) {
      return
    }

    //Draw the line
    gc.lineWidth = lineWidth

    //Fill the area
    areaFill?.let {
      gc.fillStyle(it)

      val fillPath = path.copy()

      val x = fillPath.currentPoint.x
      val y = fillPath.currentPoint.y

      //Complete the path to be able to fill the area

      //move down
      fillPath.lineTo(x, baseLineY)

      //Complete path to be able to fill
      fillPath.lineTo(firstX, baseLineY)

      //close the path
      fillPath.closePath()

      gc.fill(fillPath)
    }

    shadow?.let {
      gc.strokeStyle(it)
      gc.translate(shadowOffsetX, shadowOffsetY)
      gc.stroke(path)
      gc.translate(-shadowOffsetX, -shadowOffsetY)
    }

    gc.strokeStyle(stroke)
    gc.stroke(path)
  }

  /**
   * Returns true if the current path is empty
   */
  fun isPathEmpty(): Boolean {
    return path.isEmpty()
  }
}

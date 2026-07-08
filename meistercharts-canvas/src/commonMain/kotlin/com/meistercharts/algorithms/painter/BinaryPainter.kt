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

import com.meistercharts.annotations.Window
import com.meistercharts.calc.ChartingUtils
import com.meistercharts.canvas.CanvasRenderingContext
import com.meistercharts.color.ColorProviderNullable
import com.meistercharts.color.get
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
  baseLineY: Double,
  /**
   * The maximum width for the lines.
   * The lines are fitted *within* this width
   */
  @px
  maxWidth: Double,
  /**
   * The maximum height for the lines.
   * The lines are fitted *within* this height
   */
  @px
  maxHeight: Double,
) : AbstractXyLinePainter(snapXValuesToPixel, snapYValuesToPixel) {

  /**
   * Snapped baseline y.
   * Mutable to allow reuse of the painter instance ([reset]).
   */
  @px @Window
  var baseLineY: Double = baseLineY
    private set

  /**
   * The maximum width for the lines.
   * The lines are fitted *within* this width
   */
  @px
  var maxWidth: Double = maxWidth
    private set

  /**
   * The maximum height for the lines.
   * The lines are fitted *within* this height
   */
  @px
  var maxHeight: Double = maxHeight
    private set

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
  var areaFill: ColorProviderNullable = { null }

  fun reset() {
    firstX = 0.0
    path.beginPath()
  }

  /**
   * Resets the painter for reuse with updated bounds.
   * Allows keeping a single instance instead of instantiating a new painter per layout pass.
   */
  fun reset(@px @Window baseLineY: Double, @px maxWidth: Double, @px maxHeight: Double) {
    this.baseLineY = baseLineY
    this.maxWidth = maxWidth
    this.maxHeight = maxHeight
    reset()
  }

  override fun addCoordinate(gc: CanvasRenderingContext, @px @Window x: Double, @px @Window y: Double) {
    //Ensure the line is always visible
    @Window val snappedX = ChartingUtils.lineWithin(snapXPosition(x), 0.0, maxWidth, lineWidth)

    //Verify that y is always visible
    @Window val snappedY = ChartingUtils.lineWithin(snapYPosition(y), 0.0, maxHeight, lineWidth)

    if (path.isEmpty()) {
      //The first point. Start at bottom
      path.moveTo(snappedX, ChartingUtils.lineWithin(snapYPosition(baseLineY), 0.0, maxHeight, lineWidth))

      //Line to first point
      path.lineTo(snappedX, snappedY)

      //Remember the first x point to be able to close the path
      firstX = snappedX

    } else {
      @px @Window val lastY = path.currentPointYOrNaN()

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
    areaFill?.invoke()?.let {
      gc.fillStyle(it)

      @px @Window val x = path.currentPointXOrNaN()
      //The first action is the initial moveTo (at the snapped baseline) - required to close the path
      @px @Window val firstPointX = path.firstPointXOrNaN()
      @px @Window val firstPointY = path.firstPointYOrNaN()

      //Complete the path (in place - avoids copying the path) to be able to fill the area

      //move down
      path.lineTo(x, baseLineY)

      //Complete path to be able to fill
      path.lineTo(firstX, baseLineY)

      //close the path (back to the first point - same as closePath())
      path.lineTo(firstPointX, firstPointY)

      gc.fill(path)

      //Remove the three actions added above - the strokes below must only paint the line itself
      path.removeLastActions(3)
    }

    shadow.get()?.let {
      gc.strokeStyle(it)
      gc.translate(shadowOffsetX, shadowOffsetY)
      gc.stroke(path)
      gc.translate(-shadowOffsetX, -shadowOffsetY)
    }

    if (stroke.get() != null) {
      gc.strokeStyle((stroke.get() ?: return).toRgba())
    }

    gc.stroke(path)
  }

  /**
   * Returns true if the current path is empty
   */
  fun isPathEmpty(): Boolean {
    return path.isEmpty()
  }
}

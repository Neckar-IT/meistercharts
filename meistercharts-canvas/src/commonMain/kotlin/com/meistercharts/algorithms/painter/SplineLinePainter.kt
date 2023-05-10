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

import com.meistercharts.annotations.Zoomed
import com.meistercharts.canvas.CanvasRenderingContext
import com.meistercharts.canvas.DebugConfiguration
import com.meistercharts.canvas.DebugFeature
import com.meistercharts.painter.AbstractLinePainter
import it.neckar.open.collections.DoubleArrayList
import it.neckar.open.collections.first
import it.neckar.open.collections.last
import it.neckar.open.kotlin.lang.sqrt

/**
 * Paints splines
 */
class SplineLinePainter(
  snapXValues: Boolean,
  snapYValues: Boolean
) : AbstractLinePainter(snapXValues, snapYValues) {

  /**
   * The smoothing factor. The heigher the smoother the curve.
   * If too high, the spline will contain loops.
   */
  var smoothingFactor: Double = 0.5

  /**
   * Contains the x values for the points itself
   */
  private val pointsX: DoubleArrayList = DoubleArrayList(10)

  /**
   * Contains the Y values for the points itself
   */
  private val pointsY: DoubleArrayList = DoubleArrayList(10)

  override fun begin(gc: CanvasRenderingContext) {
    pointsX.clear()
    pointsY.clear()
  }

  override fun addCoordinates(gc: CanvasRenderingContext, x: @Zoomed Double, y: @Zoomed Double) {
    pointsX.add(x)
    pointsY.add(y)
  }

  /**
   * Contains the coords for the control points
   */
  private val controlPointsX: DoubleArrayList = DoubleArrayList(10)
  private val controlPointsY: DoubleArrayList = DoubleArrayList(10)

  /**
   * Calculates all control points in this method
   */
  override fun paint(gc: CanvasRenderingContext) {
    if (pointsX.size < 2) {
      //Less than 2 points - do nothing
      return
    }

    if (pointsX.size == 2) {
      //Only 2 points, just connect them directly
      gc.strokeLine(pointsX[0], pointsY[0], pointsX[1], pointsY[1])
      return
    }

    //We have at least 3 points, calculate the control points first
    controlPointsX.clear()
    controlPointsY.clear()


    //Add the artificial control point for the first one
    controlPointsX.add(pointsX[0])
    controlPointsY.add(pointsY[0])

    //First and last point are not visited
    for (i in 1 until pointsX.size - 1) {
      val startPointX = pointsX[i - 1]
      val startPointY = pointsY[i - 1]

      val midPointX = pointsX[i]
      val midPointY = pointsY[i]

      val endPointX = pointsX[i + 1]
      val endPointY = pointsY[i + 1]


      /**
       * Calculate deltas
       */
      @Zoomed val deltaStartEndX = endPointX - startPointX
      @Zoomed val deltaStartEndY = endPointY - startPointY

      val deltaX2start = midPointX - startPointX
      val deltaX2end = midPointX - endPointX

      val deltaY2start = midPointY - startPointY
      val deltaY2end = midPointY - endPointY


      //Distance 2 start / end
      val distance2Start = (deltaX2start * deltaX2start + deltaY2start * deltaY2start).sqrt()
      val distance2End = (deltaX2end * deltaX2end + deltaY2end * deltaY2end).sqrt()

      //Calculate the factor for the "small" triangles towards the control points
      val scale2Start = smoothingFactor * distance2Start / (distance2Start + distance2End)
      val scale2End = smoothingFactor * distance2End / (distance2Start + distance2End)

      val controlStartX = midPointX - scale2Start * deltaStartEndX
      val controlStartY = midPointY - scale2Start * deltaStartEndY

      val controlEndX = midPointX + scale2End * deltaStartEndX
      val controlEndY = midPointY + scale2End * deltaStartEndY

      controlPointsX.add(controlStartX)
      controlPointsY.add(controlStartY)

      controlPointsX.add(controlEndX)
      controlPointsY.add(controlEndY)
    }

    //Add the artificial control point for the last one
    controlPointsX.add(pointsX.last())
    controlPointsY.add(pointsY.last())


    //Check we have the right amount of control points
    val expectedControlPointsCount = pointsX.size * 2 - 2
    require(controlPointsX.size == expectedControlPointsCount) {
      "Expected $expectedControlPointsCount control points but was <${controlPointsX.size}>"
    }

    //Now paint the path
    gc.beginPath()

    //Start at the first coordinate
    gc.moveTo(pointsX[0], pointsY[0])

    /**
     * Iterate over all points but the first
     */
    for (i in 1 until pointsX.size) {
      val controlX1 = controlPointsX[(i - 1) * 2]
      val controlY1 = controlPointsY[(i - 1) * 2]

      val controlX2 = controlPointsX[(i - 1) * 2 + 1]
      val controlY2 = controlPointsY[(i - 1) * 2 + 1]

      val x = pointsX[i]
      val y = pointsY[i]

      gc.bezierCurveTo(controlX1, controlY1, controlX2, controlY2, x, y)
    }
    gc.stroke()


    /**
     * Debugging code
     */

    if (DebugFeature.BezierControlPoints.enabled(DebugConfiguration())) {
      //Paint the control points
      for (i in 0 until controlPointsX.size) {
        val controlX1 = controlPointsX[i]
        val controlY1 = controlPointsY[i]

        gc.strokeOvalCenter(controlX1, controlY1, 5.0, 5.0)
      }

      //Paint the connected control points
      gc.beginPath()
      gc.moveTo(pointsX.first(), pointsY.first())

      for (i in 1 until pointsX.size) {
        val controlX1 = controlPointsX[(i - 1) * 2]
        val controlY1 = controlPointsY[(i - 1) * 2]

        gc.lineTo(controlX1, controlY1)

        val controlX2 = controlPointsX[(i - 1) * 2 + 1]
        val controlY2 = controlPointsY[(i - 1) * 2 + 1]

        gc.lineTo(controlX2, controlY2)
        gc.strokeLine(controlX1, controlY1, controlX2, controlY2)
      }

      gc.stroke()
    }
  }
}

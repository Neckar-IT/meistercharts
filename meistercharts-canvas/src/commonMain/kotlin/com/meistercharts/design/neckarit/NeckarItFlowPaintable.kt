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
package com.meistercharts.design.neckarit

import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.annotations.DomainRelative
import com.meistercharts.annotations.Window
import com.meistercharts.canvas.CanvasRenderingContext
import com.meistercharts.canvas.geometry.BezierCurveRect
import com.meistercharts.canvas.paintable.Paintable
import com.meistercharts.canvas.path
import com.meistercharts.canvas.stroke
import it.neckar.geometry.Coordinates
import it.neckar.geometry.Rectangle
import it.neckar.geometry.Size
import it.neckar.open.unit.other.px

/**
 * A paintable that paints the NECKAR.IT flow
 */
class NeckarItFlowPaintable(
  /**
   * The size of the flow
   */
  val size: @px Size
) : Paintable {
  val boundingBox: Rectangle = Rectangle(Coordinates.none, size)

  override fun boundingBox(paintingContext: LayerPaintingContext): Rectangle {
    return boundingBox
  }

  override fun paint(paintingContext: LayerPaintingContext, x: Double, y: Double) {
    val gc = paintingContext.gc
    gc.translate(x, y)
    val chartCalculator = paintingContext.chartCalculator

    gc.stroke(NeckarItFlow.colorShape0)
    gc.fill(NeckarItFlow.colorShape0)
    @DomainRelative val segment0 = NeckarItFlow.shape0(paintingContext.frameTimestamp)
    gc.paintBezierCurveRect(segment0)

    gc.stroke(NeckarItFlow.colorShape1)
    gc.fill(NeckarItFlow.colorShape1)
    @DomainRelative val segment1 = NeckarItFlow.shape1(paintingContext.frameTimestamp)
    gc.paintBezierCurveRect(segment1)

    gc.stroke(NeckarItFlow.colorShape2)
    gc.fill(NeckarItFlow.colorShape2)
    @DomainRelative val segment2 = NeckarItFlow.shape2(paintingContext.frameTimestamp)
    gc.paintBezierCurveRect(segment2)

    gc.stroke(NeckarItFlow.colorShape3)
    gc.fill(NeckarItFlow.colorShape3)
    @DomainRelative val segment3 = NeckarItFlow.shape3(paintingContext.frameTimestamp)
    gc.paintBezierCurveRect(segment3)

    //if (showControlPoints) {
    //  debugSegment(gc, chartCalculator, segment0, Color.pink, "0")
    //  debugSegment(gc, chartCalculator, segment1, Color.blue, "1")
    //  debugSegment(gc, chartCalculator, segment2, Color.green, "2")
    //  debugSegment(gc, chartCalculator, segment3, Color.red, "3")
    //}
  }

  /**
   * Paints the bezier curve rect
   */
  private fun CanvasRenderingContext.paintBezierCurveRect(bezierCurveRect: @DomainRelative BezierCurveRect) {
    @Window val scaled = bezierCurveRect.scale(size.width, size.height)

    beginPath()
    path(scaled)

    fill()
    stroke()
  }

  companion object {
    /**
     * Ratio between width and height
     */
    const val ratio: Double = 0.21

    /**
     * Returns the optimal size for a given width
     */
    fun calculateSizeForWidth(width: Double): Size {
      return Size(width, optimalHeight(width))
    }

    /**
     * Returns the optimal height for a given width
     */
    fun optimalHeight(width: Double): Double = width * ratio

    /**
     * Returns the paintable for a given width
     */
    fun forWidth(width: Double): NeckarItFlowPaintable {
      return NeckarItFlowPaintable(calculateSizeForWidth(width))
    }
  }
}


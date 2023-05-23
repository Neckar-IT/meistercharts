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
package com.meistercharts.painter

import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.linechart.PointStyle
import com.meistercharts.algorithms.painter.AbstractPainter
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.annotations.Window
import com.meistercharts.annotations.Zoomed
import com.meistercharts.canvas.CanvasRenderingContext
import com.meistercharts.canvas.ChartSupport
import com.meistercharts.canvas.paintable.Paintable
import com.meistercharts.canvas.strokeCross
import com.meistercharts.canvas.strokeCross45Degrees
import com.meistercharts.model.Rectangle
import kotlin.math.max

/**
 * Paints *point* (not lines!)
 */
abstract class AbstractPointPainter(
  snapXValues: Boolean,
  snapYValues: Boolean
) : AbstractPainter(snapXValues, snapYValues), PointPainter {

  var pointSize: @Zoomed Double = 10.0
}

/**
 * Paints the points based upon the point style
 */
class PointStylePainter(
  val pointStyle: PointStyle = PointStyle.Dot,
  var lineWidth: @Zoomed Double = 1.0,
  snapXValues: Boolean,
  snapYValues: Boolean
) : AbstractPointPainter(snapXValues, snapYValues) {
  /**
   * The color to be used for the point - null implies to use the current color
   */
  var color: Color? = null

  init {
    pointSize = calculatePointSize(pointStyle, lineWidth)
  }

  override fun paintPoint(gc: CanvasRenderingContext, x: @Window Double, y: @Window Double) {
    if (pointStyle == PointStyle.None) {
      return
    }
    gc.lineWidth = lineWidth
    color?.let {
      gc.strokeStyle(it)
      gc.fillStyle(it)
    }

    when (pointStyle) {
      PointStyle.None           -> Unit // nothing to do
      PointStyle.Cross          -> gc.strokeCross(x, y, pointSize)
      PointStyle.Cross45Degrees -> gc.strokeCross45Degrees(x, y, pointSize)
      PointStyle.Dot            -> gc.fillOvalCenter(x, y, pointSize, pointSize)
    }
  }

  companion object {
    /**
     * Calculates a useful size for a point depending on the style and line width
     */
    fun calculatePointSize(pointStyle: PointStyle, lineWidth: @Zoomed Double): @Zoomed Double {
      return when (pointStyle) {
        PointStyle.None           -> 0.0
        PointStyle.Cross          -> max(5.0, lineWidth * 3.0)
        PointStyle.Cross45Degrees -> max(5.0, lineWidth * 3.0)
        PointStyle.Dot            -> max(5.0, lineWidth * 2.0)
      }
    }
  }
}


/**
 * Paints a rectangle
 */
class RectanglePointPainter(
  snapXValues: Boolean,
  snapYValues: Boolean
) : AbstractPointPainter(snapXValues, snapYValues) {
  var stroke: Color = Color.black

  var lineWidth: @Zoomed Double = 1.0

  override fun paintPoint(gc: CanvasRenderingContext, x: @Window Double, y: @Window Double) {
    val x1 = snapXPosition(x - pointSize / 2.0)
    val y1 = snapYPosition(y - pointSize / 2.0)

    gc.lineWidth = lineWidth
    gc.stroke(stroke)
    gc.strokeRect(x1, y1, snapWidth(pointSize), snapHeight(pointSize))
  }
}

/**
 * Paints a circle
 */
class CirclePointPainter(
  snapXValues: Boolean = false,
  snapYValues: Boolean = false,
) : AbstractPointPainter(snapXValues, snapYValues), Paintable {
  var fill: Color = Color.white
  var stroke: Color = Color.web("#ffc83e")

  var lineWidth: @Zoomed Double = 1.0

  override fun paintPoint(gc: CanvasRenderingContext, x: @Window Double, y: @Window Double) {
    val centerX = snapXPosition(x)
    val centerY = snapYPosition(y)

    //Fill first
    gc.fill(fill)
    gc.fillOvalCenter(centerX, centerY, pointSize, pointSize)

    gc.lineWidth = lineWidth
    gc.stroke(stroke)
    gc.strokeOvalCenter(centerX, centerY, pointSize, pointSize)
  }

  //Below are the properties required for the [Paintable] interface
  override fun boundingBox(paintingContext: LayerPaintingContext): Rectangle = Rectangle.centered(pointSize, pointSize)

  override fun paint(paintingContext: LayerPaintingContext, x: Double, y: Double) {
    paintPoint(paintingContext.gc, x, y)
  }
}

/**
 * Paints a fancy point consisting of three points
 */
class FancyPointPainter(
  snapXValues: Boolean,
  snapYValues: Boolean
) : AbstractPointPainter(snapXValues, snapYValues) {

  init {
    pointSize = 16.0
  }

  var innerFill: Color = Color.black
  var fill: Color = Color.web("#ffc83e")
  var outerFill: Color = Color.black
  var outerWidth: Double = 4.0
  var fillSize: Double = 6.0


  override fun paintPoint(gc: CanvasRenderingContext, x: @Window Double, y: @Window Double) {
    val centerX = snapXPosition(x)
    val centerY = snapYPosition(y)

    //The outer oval
    run {
      val pointSize = this.pointSize + outerWidth

      gc.fill(outerFill)
      gc.fillOvalCenter(centerX, centerY, pointSize, pointSize)
    }

    //The yellow "main" oval
    run {
      gc.fill(fill)
      gc.fillOvalCenter(centerX, centerY, pointSize, pointSize)
    }

    //The inner oval
    run {
      val pointSize = this.pointSize - fillSize

      gc.fill(innerFill)
      gc.fillOvalCenter(centerX, centerY, pointSize, pointSize)
    }

  }
}


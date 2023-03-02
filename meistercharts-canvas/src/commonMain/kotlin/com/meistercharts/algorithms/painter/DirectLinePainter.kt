package com.meistercharts.algorithms.painter

import com.meistercharts.annotations.Window
import com.meistercharts.canvas.CanvasRenderingContext
import com.meistercharts.painter.AbstractLinePainter

/**
 * Paints a line using a direct path.
 *
 * * see [com.meistercharts.painter.SegmentedLinePainter] for a painter that support segments
 *
 */
open class DirectLinePainter(
  snapXValues: Boolean,
  snapYValues: Boolean
) : AbstractLinePainter(snapXValues, snapYValues) {

  private fun firstPoint(gc: CanvasRenderingContext, x: @Window Double, y: @Window Double) {
    gc.beginPath()
    gc.moveTo(x, y)
  }

  /**
   * The number of points added
   */
  private var pointCount = 0

  private var lastX: @Window Double = 0.0
  private var lastY: @Window Double = 0.0

  /**
   * True, if no points have been added yet
   */
  private val empty: Boolean
    get() = pointCount < 1

  override fun begin(gc: CanvasRenderingContext) {
    pointCount = 0
  }

  override fun addCoordinate(gc: CanvasRenderingContext, x: @Window Double, y: @Window Double) {
    require(x.isFinite()) {
      "x must be a real number required but was $x"
    }
    require(y.isFinite()) {
      "y must be a real number required but was $y"
    }

    if (empty) {
      firstPoint(gc, x, y)
    } else {
      gc.lineTo(x, y)
    }
    lastX = x
    lastY = y
    ++pointCount
  }

  override fun finish(gc: CanvasRenderingContext) {
    if (empty) {
      //No points have been added - just return
      return
    }

    if (pointCount == 1) {
      //The line consists of a single point.
      //In order to make that point visible we prolong the line by the current lineWidth.
      gc.lineTo(lastX + gc.lineWidth, lastY)
    }

    gc.stroke()
  }

  override fun toString(): String {
    return "DirectLinePainter"
  }
}


package com.meistercharts.painter

import com.meistercharts.canvas.CanvasRenderingContext

/**
 * A [PointPainter] that paints nothing
 */
object NoPointPainter : PointPainter {
  override fun paintPoint(gc: CanvasRenderingContext, x: Double, y: Double) {
    // do nothing
  }
}

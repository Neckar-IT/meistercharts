package com.meistercharts.painter

import com.meistercharts.annotations.Zoomed
import com.meistercharts.canvas.CanvasRenderingContext

/**
 * A [LinePainter] that paints nothing
 */
object NoLinePainter : LinePainter {
  override fun begin(gc: CanvasRenderingContext) {
    // do nothing
  }

  override fun addCoordinate(gc: CanvasRenderingContext, x: @Zoomed Double, y: @Zoomed Double) {
    // do nothing
  }

  override fun finish(gc: CanvasRenderingContext) {
    // do nothing
  }
}

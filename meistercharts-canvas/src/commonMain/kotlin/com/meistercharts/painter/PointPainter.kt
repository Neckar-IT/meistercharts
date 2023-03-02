package com.meistercharts.painter

import com.meistercharts.annotations.Window
import com.meistercharts.canvas.CanvasRenderingContext

/**
 * Represents painters that paint a single point
 */
fun interface PointPainter {
  /**
   * Paints a point at the given location
   */
  fun paintPoint(gc: CanvasRenderingContext, x: @Window Double, y: @Window Double)
}

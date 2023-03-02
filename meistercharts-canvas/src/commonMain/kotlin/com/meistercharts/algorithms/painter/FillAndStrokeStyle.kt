package com.meistercharts.algorithms.painter

import com.meistercharts.canvas.CanvasRenderingContext

/**
 * Combines a fill and a stroke
 */
data class FillAndStrokeStyle(
  /**
   * The fill
   */
  val fill: CanvasPaint,
  /**
   * The stroke
   */
  val stroke: CanvasPaint,
) {
  /**
   * Apply the fill and stroke to the given gc
   */
  fun apply(gc: CanvasRenderingContext) {
    gc.fillAndStroke(this)
  }
}

/**
 * Sets the fill and stroke
 */
fun CanvasRenderingContext.fillAndStroke(style: FillAndStrokeStyle) {
  fillAndStroke(style.fill, style.stroke)
}

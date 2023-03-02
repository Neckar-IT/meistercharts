package com.meistercharts.algorithms.painter

import it.neckar.open.unit.other.px

/**
 * A linear gradient that can be used in styles
 */
data class LinearGradient(
  val color0: Color,
  val color1: Color
) : CanvasPaintProvider {
  override fun toCanvasPaint(x0: @px Double, y0: @px Double, x1: @px Double, y1: @px Double): CanvasPaint {
    return CanvasLinearGradient(x0, y0, x1, y1, color0, color1)
  }
}

package com.meistercharts.algorithms.painter

import it.neckar.open.kotlin.lang.isPositiveOrZero
import it.neckar.open.unit.other.px
import kotlin.math.sqrt

/**
 * A radial gradient that can be used in styles
 */
data class RadialGradient(
  val color0: Color,
  val color1: Color
) : CanvasPaintProvider {
  override fun toCanvasPaint(x0: @px Double, y0: @px Double, x1: @px Double, y1: @px Double): CanvasPaint {
    val dx = x1 - x0
    val dy = y1 - y0

    val radius = sqrt(dx * dx + dy * dy)

    require(radius.isPositiveOrZero()) { "radius must not be negative but was <${radius}>" }

    return CanvasRadialGradient(x0, y0, radius = radius, color0 = color0, color1 = color1)
  }
}

package com.meistercharts.algorithms.painter

import it.neckar.open.unit.other.px

/**
 * Can be used in styles - represents flat colors or gradients.
 *
 * Can be a [Color] or a [LinearGradient]
 */
fun interface CanvasPaintProvider {
  /**
   * Converts this [CanvasPaintProvider] to a [CanvasPaint].
   *
   * The given values are relevant when creating a gradient. They are ignored for flat colors
   */
  fun toCanvasPaint(x0: @px Double, y0: @px Double, x1: @px Double, y1: @px Double): CanvasPaint

}


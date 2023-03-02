package com.meistercharts.algorithms

import it.neckar.open.unit.other.px

/**
 * Contains several helper classes
 *
 */
object ChartingUtils {
  /**
   * Ensure that a line lies fully within the given min / max.
   * This method should be used when a line is placed at the edge of the window. It ensures that the complete
   * line is visible.
   *
   * If placing a line at the edge of the canvas only half of the line width is visible by default.
   */
  @px
  fun lineWithin(@px lineCenter: Double, @px min: Double, @px max: Double, @px lineWidth: Double): Double {
    require(max >= min) { "max must not be less than min" }

    return if (max - min < lineWidth) lineCenter else lineCenter.coerceIn(min + lineWidth / 2.0, max - lineWidth / 2.0)
  }
}

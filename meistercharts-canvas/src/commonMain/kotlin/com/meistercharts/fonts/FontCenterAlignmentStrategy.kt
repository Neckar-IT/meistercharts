package com.meistercharts.fonts

/**
 * Special implementation that optimizes the center alignment to be visually pleasing.
 *
 * This is especially important if aligning (number) values to ticks
 */
object FontCenterAlignmentStrategy {

  /**
   * Calculates the offset from the center for the given height of the capital "h"
   */
  fun calculateCenterOffset(capitalHHeight: Double): Double {
    return -capitalHHeight * 0.02 //hard coded value to improve the position of the center line
  }
}

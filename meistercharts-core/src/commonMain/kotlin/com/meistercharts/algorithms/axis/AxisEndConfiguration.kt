package com.meistercharts.algorithms.axis

/**
 * Configuration for the display for the start and end of the axis
 */
enum class AxisEndConfiguration {
  /**
   * Just show the best ticks.
   */
  Default,

  /**
   * Ensure the exact values are shown at the axis ends (both sides)
   */
  Exact
}

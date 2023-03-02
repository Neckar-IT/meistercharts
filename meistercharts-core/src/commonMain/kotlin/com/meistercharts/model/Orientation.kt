package com.meistercharts.model

/**
 * The orientation within the chart.
 */
enum class Orientation {
  /**
   * The values of the chart are visible on the vertical (y) axis.
   * e.g. BarChart: Bars are painted bottom to top
   */
  Vertical,

  /**
   * The values of the chart are visible on the horizontal (x) axis.
   * e.g. BarChart: Bars are painted left to right
   */
  Horizontal;

  /**
   * Returns the opposite orientation
   */
  fun opposite(): Orientation {
    return when (this) {
      Vertical -> Horizontal
      Horizontal -> Vertical
    }
  }

  fun isHorizontal(): Boolean {
    return this == Horizontal
  }

  fun isVertical(): Boolean {
    return this == Vertical
  }
}

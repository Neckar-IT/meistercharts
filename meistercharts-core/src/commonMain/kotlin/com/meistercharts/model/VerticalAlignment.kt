package com.meistercharts.model

/**
 * The vertical alignment
 */
enum class VerticalAlignment {
  Top,
  Center,
  Baseline,
  Bottom;

  /**
   * Returns the opposite direction
   */
  fun opposite(): VerticalAlignment {
    return when (this) {
      Top      -> Bottom
      Center   -> Center
      Baseline -> Baseline
      Bottom   -> Top
    }
  }
}

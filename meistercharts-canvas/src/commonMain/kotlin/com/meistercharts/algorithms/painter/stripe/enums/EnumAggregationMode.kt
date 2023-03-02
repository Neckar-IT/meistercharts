package com.meistercharts.algorithms.painter.stripe.enums

/**
 * How to visualize aggregated enum values
 */
enum class EnumAggregationMode {
  /**
   * The enum-value with the lowest ordinal during a sampling period will be taken.
   */
  ByOrdinal,

  /**
   * The enum-value that is sampled the most during a sampling period will be taken.
   */
  MostTime,
}

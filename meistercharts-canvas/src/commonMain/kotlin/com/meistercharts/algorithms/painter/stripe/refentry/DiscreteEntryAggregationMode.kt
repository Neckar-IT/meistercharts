package com.meistercharts.algorithms.painter.stripe.refentry

/**
 * How to visualize aggregated reference entries
 */
enum class DiscreteEntryAggregationMode {
  /**
   * Visualizes the most of the time reference entry
   */
  MostOfTheTimeLabel,

  /**
   * Visualizes the most important status.
   *
   * Does *not* show the most-of-the-time-label to avoid confusion.
   * It is possible that the most-of-the-time-label does not have the status enum with the lowest ordinal.
   */
  MostImportantStatus,
}

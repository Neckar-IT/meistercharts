package com.meistercharts.api.discrete

import it.neckar.open.unit.other.Exclusive
import it.neckar.open.unit.other.Inclusive
import it.neckar.open.unit.other.Sorted
import it.neckar.open.unit.si.ms


/**
 * Contains the data for the discrete timeline chart
 */
expect interface DiscreteTimelineChartData {
  /**
   * Index corresponds to the data series index.
   * Contains one entry for each data series.
   *
   *
   * The entries for the data series are *not* aligned.
   */
  val series: Array<DiscreteDataEntriesForDataSeries>
}

/**
 * Contains the entries for a single discrete data series
 */
expect interface DiscreteDataEntriesForDataSeries {
  /**
   * Contains all entries for this data series.
   * Must not overlap!
   */
  val entries: Array<@Sorted(by = "start") DiscreteDataEntry>
}

/**
 *
 * Interface representing a discrete data entry.
 */
expect interface DiscreteDataEntry {
  /**
   * The start time of the data entry in milliseconds, inclusive
   */
  val start: @ms @Inclusive Double

  /**
   * The end time of the data entry in milliseconds, exclusive
   */
  val end: @ms @Exclusive Double

  /**
   * A string representing the label of the data entry
   */
  val label: String

  /**
   * Representing the status of the data entry.
   */
  val status: Double //must be double since JS does not support Int
}

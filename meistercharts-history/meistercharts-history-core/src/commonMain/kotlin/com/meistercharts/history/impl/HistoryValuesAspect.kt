package com.meistercharts.history.impl

/**
 * Implementations contain one aspect of history values.
 */
interface HistoryValuesAspect {
  /**
   * The amount of data series within
   */
  val dataSeriesCount: Int

  /**
   * The number of timestamps
   */
  val timeStampsCount: Int

  /**
   * Returns true if this aspect is empty.
   * Does either not contain any data series and/or timestamps
   */
  val isEmpty: Boolean

  /**
   * Returns the recording type for this aspect.
   */
  val recordingType: RecordingType
}

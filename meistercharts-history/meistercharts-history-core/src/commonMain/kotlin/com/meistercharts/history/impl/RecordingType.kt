package com.meistercharts.history.impl

/**
 * The recording type of history bucket.
 */
enum class RecordingType {
  /**
   * The values have been measured.
   * Does not contain min/max values
   */
  Measured,

  /**
   * The values have been calculated - also contains min/max
   */
  Calculated
}

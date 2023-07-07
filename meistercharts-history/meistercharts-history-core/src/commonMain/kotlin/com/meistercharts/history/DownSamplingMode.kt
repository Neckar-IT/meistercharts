package com.meistercharts.history

/**
 * Represents the down sampling mode.
 */
enum class DownSamplingMode {
  /**
   * Down sampling is executed automatically
   */
  Automatic,

  /**
   * Down sampling is not scheduled. It is probably necessary to calculate it manually.
   */
  None,
}

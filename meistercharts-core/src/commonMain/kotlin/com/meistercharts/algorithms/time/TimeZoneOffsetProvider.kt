package com.meistercharts.algorithms.time

import it.neckar.open.time.TimeZone
import it.neckar.open.unit.si.ms
import com.meistercharts.algorithms.time.DefaultTimeZoneOffsetProvider

/**
 * The time-zone-offset provider that has been configured.
 * The design is configured from the MeisterChartsPlatform
 */
var timeZoneOffsetProvider: CachedTimeZoneOffsetProvider = DefaultTimeZoneOffsetProvider().cached()

/**
 * Provider for time zone offsets for a given timestamp and time zone
 */
fun interface TimeZoneOffsetProvider {
  /**
   * Provides the offset for the given time-zone at the given timestamp
   * @param timestamp represents milliseconds since 1 January 1970 UTC
   * @param timeZone the time-zone for which the offset should be provided
   */
  fun timeZoneOffset(timestamp: @ms Double, timeZone: TimeZone): @ms Double
}

/**
 * Always returns 0 as a time-zone offset
 */
object Always0TimeZoneOffsetProvider : TimeZoneOffsetProvider {
  override fun timeZoneOffset(timestamp: @ms Double, timeZone: TimeZone): @ms Double {
    return 0.0
  }

}


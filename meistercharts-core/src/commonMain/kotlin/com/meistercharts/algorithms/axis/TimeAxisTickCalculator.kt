package com.meistercharts.algorithms.axis

import it.neckar.open.formatting.formatUtc
import it.neckar.open.time.TimeZone
import it.neckar.open.unit.other.Inclusive
import it.neckar.open.unit.si.ms

/**
 * Calculates ticks for a time axis
 */
object TimeAxisTickCalculator {

  /**
   * Calculates the timestamps for the ticks between [startTimestamp] and [endTimestamp].
   *
   * The distance between two consecutive ticks must be at least [minTickDistance] milliseconds.
   */
  fun calculateTickValues(
    /**
     * The start of the time range to compute the ticks for
     */
    startTimestamp: @ms @Inclusive Double,
    /**
     * The end of the time range to compute the ticks for
     */
    endTimestamp: @ms @Inclusive Double,
    /**
     * The min distance between two ticks (in milli seconds)
     */
    minTickDistance: @ms Double,
    /**
     * The time zone
     */
    timeZone: TimeZone = TimeZone.UTC
  ): @ms DoubleArray {
    require(startTimestamp >= klockSmallestSupportedTimestamp) { "start timestamp must be greater than or equal to ${klockSmallestSupportedTimestamp.formatUtc()} but was ${startTimestamp.formatUtc()}" }
    require(startTimestamp <= klockGreatestSupportedTimestamp) { "start timestamp must be less than or equal to ${klockGreatestSupportedTimestamp.formatUtc()} but was ${startTimestamp.formatUtc()}" }
    require(endTimestamp >= klockSmallestSupportedTimestamp) { "end timestamp must be greater than or equal to ${klockSmallestSupportedTimestamp.formatUtc()} but was ${endTimestamp.formatUtc()}" }
    require(endTimestamp <= klockGreatestSupportedTimestamp) { "end timestamp must be less than or equal to ${klockGreatestSupportedTimestamp.formatUtc()} but was ${endTimestamp.formatUtc()}" }

    require(startTimestamp <= endTimestamp) { "start <$startTimestamp> (${startTimestamp.formatUtc()} must be less than or equal to end <$endTimestamp> (${endTimestamp.formatUtc()}" }

    val timeTickDistance: TimeTickDistance = TimeTickDistance.forTicks(minTickDistance)
    return timeTickDistance.calculateTicks(startTimestamp, endTimestamp, timeZone, true)
  }
}

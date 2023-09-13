/**
 * Copyright 2023 Neckar IT GmbH, Mössingen, Germany
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.meistercharts.axis.time

import com.meistercharts.time.klockGreatestSupportedTimestamp
import com.meistercharts.time.klockSmallestSupportedTimestamp
import it.neckar.open.formatting.formatUtc
import it.neckar.datetime.minimal.TimeZone
import it.neckar.open.collections.DoubleArrayList
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
  ): @ms DoubleArrayList {
    require(startTimestamp >= klockSmallestSupportedTimestamp) { "start timestamp must be greater than or equal to ${klockSmallestSupportedTimestamp.formatUtc()} but was ${startTimestamp.formatUtc()}" }
    require(startTimestamp <= klockGreatestSupportedTimestamp) { "start timestamp must be less than or equal to ${klockGreatestSupportedTimestamp.formatUtc()} but was ${startTimestamp.formatUtc()}" }
    require(endTimestamp >= klockSmallestSupportedTimestamp) { "end timestamp must be greater than or equal to ${klockSmallestSupportedTimestamp.formatUtc()} but was ${endTimestamp.formatUtc()}" }
    require(endTimestamp <= klockGreatestSupportedTimestamp) { "end timestamp must be less than or equal to ${klockGreatestSupportedTimestamp.formatUtc()} but was ${endTimestamp.formatUtc()}" }

    require(startTimestamp <= endTimestamp) { "start <$startTimestamp> (${startTimestamp.formatUtc()} must be less than or equal to end <$endTimestamp> (${endTimestamp.formatUtc()}" }

    val timeTickDistance: TimeTickDistance = TimeTickDistance.forTicks(minTickDistance)
    return timeTickDistance.calculateTicks(startTimestamp, endTimestamp, timeZone)
  }
}

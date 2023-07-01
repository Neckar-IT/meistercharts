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
package com.meistercharts.time

import it.neckar.open.collections.fastForEach
import it.neckar.open.unit.other.Sorted
import it.neckar.open.unit.si.ms
import kotlinx.serialization.Serializable

/**
 * Contains multiple time ranges.
 */
@Serializable
data class TimeRanges(
  val timeRanges: List<@Sorted @Serializable(with = TimeRangeSerializer::class) TimeRange>,
) : List<TimeRange> by timeRanges {
  /**
   * Returns the start for the first time range.
   * Throws an exception if there are no time ranges
   */
  val firstStart: @ms Double
    get() {
      return timeRanges.first().start
    }

  /**
   * Returns the end for the last time range
   * Throws an exception if there are no time ranges
   */
  val lastEnd: @ms Double
    get() {
      return timeRanges.last().end
    }

  /**
   * The delta between [lastEnd] and [firstStart]
   */
  val span: @ms Double
    get() {
      return lastEnd - firstStart
    }

  /**
   * Returns a new time ranges object that contains all time ranges of this and the additional time range
   */
  fun merge(additionalTimeRange: TimeRange, maxAcceptedGap: @ms Double = 0.0): TimeRanges {
    return createMerged(timeRanges.plus(additionalTimeRange).sorted(), maxAcceptedGap)
  }

  fun merge(additionalTimeRanges: TimeRanges, maxAcceptedGap: @ms Double = 0.0): TimeRanges {
    return createMerged(timeRanges.plus(additionalTimeRanges.timeRanges).sorted(), maxAcceptedGap)
  }

  fun fastForEach(callback: (value: TimeRange) -> Unit) {
    timeRanges.fastForEach(callback)
  }

  companion object {

    /**
     * Merges an array of time ranges into a single TimeRanges object, where adjacent time ranges with gaps less
     * than or equal to the specified maximum accepted gap are merged into a single range.
     *
     * @param timeRanges An array of TimeRange objects to be merged. These objects must be sorted in ascending order.
     * @param maxAcceptedGap The maximum allowed gap between adjacent time ranges before they are considered separate. Defaults to 0.0.
     *
     * @return A TimeRanges object representing the merged time ranges.
     */
    fun createMerged(vararg timeRanges: @Sorted TimeRange, maxAcceptedGap: @ms Double = 0.0): TimeRanges {
      return createMerged(timeRanges.toList(), maxAcceptedGap)
    }

    /**
     * Merges a list of time ranges into a single TimeRanges object, where adjacent time ranges with gaps less
     * than or equal to the specified maximum accepted gap are merged into a single range.
     *
     * @param timeRanges A list of TimeRange objects to be merged. The list must be sorted in ascending order by the start times of the time ranges.
     * @param maxAcceptedGap The maximum allowed gap between adjacent time ranges before they are considered separate. Defaults to 0.0.
     *
     * @return A TimeRanges object representing the merged time ranges.
     */
    fun createMerged(timeRanges: List<@Sorted TimeRange>, maxAcceptedGap: @ms Double = 0.0): TimeRanges {
      return TimeRanges(TimeRange.compress(timeRanges, maxAcceptedGap))
    }

    /**
     * Creates a new time ranges object
     */
    fun of(timeRange: TimeRange): TimeRanges {
      return TimeRanges(listOf(timeRange))
    }

    /**
     * Returns an empty instance
     */
    val empty: TimeRanges = TimeRanges(listOf())
  }
}

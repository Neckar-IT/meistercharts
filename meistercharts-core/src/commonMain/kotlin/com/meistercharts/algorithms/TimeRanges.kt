package com.meistercharts.algorithms

import it.neckar.open.collections.fastForEach
import it.neckar.open.unit.other.Sorted
import it.neckar.open.unit.si.ms
import kotlinx.serialization.Serializable

/**
 * Contains multiple time ranges.
 */
@Serializable
data class TimeRanges(val timeRanges: List<@Sorted @Serializable(with = TimeRangeSerializer::class) TimeRange>) : List<TimeRange> by timeRanges {
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
     * Creates a merged time ranges
     * @param maxAcceptedGap: If the gap between two gap is smaller/equal to [maxAcceptedGap] the time ranges are merged
     */
    fun createMerged(vararg timeRanges: @Sorted TimeRange, maxAcceptedGap: @ms Double = 0.0): TimeRanges {
      return createMerged(timeRanges.toList(), maxAcceptedGap)
    }

    /**
     * Creates a new time ranges object - merges the given time ranges
     * @param maxAcceptedGap: If the gap between two gap is smaller/equal to [maxAcceptedGap] the time ranges are merged
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

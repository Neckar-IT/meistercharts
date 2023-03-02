package com.meistercharts.algorithms

import com.meistercharts.annotations.TimeRelative
import it.neckar.open.collections.fastForEachIndexed
import it.neckar.open.kotlin.lang.setLast
import it.neckar.open.time.nowMillis
import it.neckar.open.formatting.formatUtc
import it.neckar.open.time.TimeConstants
import it.neckar.open.unit.other.Inclusive
import it.neckar.open.unit.other.Sorted
import it.neckar.open.unit.other.pct
import it.neckar.open.unit.quantity.Time
import it.neckar.open.unit.si.ms
import kotlinx.serialization.Serializable
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.time.Duration
import kotlin.time.DurationUnit

/**
 * Describes a time range (e.g. for a time axis)
 *
 */
class TimeRange(
  start: @Inclusive @Time @ms Double,
  end: @Inclusive @Time @ms Double
) : LinearValueRange(start, end), Comparable<TimeRange> {

  /**
   * Returns the span between start and end
   */
  val span: @ms Double
    get() = end - start

  /**
   * Returns true if both timestamps fit within the time range
   */
  fun contains(timestamp0: @Time @ms Double, timestamp1: @Time @ms Double): Boolean = timestamp0 in start..end && timestamp1 in start..end

  override fun compareTo(other: TimeRange): Int {
    if (start == other.start && end == other.end) {
      return 0
    }
    if (start >= other.end) {
      return 1
    }
    if (end <= other.start) {
      return -1
    }
    if (end >= other.end) {
      return 1
    }
    return -1
  }

  /**
   * Determines whether this time-range overlaps with [other]
   */
  fun isOverlapping(other: TimeRange): Boolean {
    return !isNotOverlapping(other)
  }

  /**
   * Returns true if the rime range does *not* overlap the other time range.
   *
   * There are two possible states where the time ranges do not overlap:
   * This is left or right of the other time range.
   */
  fun isNotOverlapping(other: TimeRange): Boolean {
    return this.end < other.start || this.start > other.end
  }

  /**
   * Determines whether the duration between this time-range and the [other] is less than or equal to [maxDuration]
   */
  fun isAdjacent(other: TimeRange, @ms maxDuration: Double = 0.0): Boolean {
    // is this before other?
    if (this.end <= other.start + maxDuration && this.end >= other.start - maxDuration) {
      return true
    }
    // is this after other?
    if (this.start >= other.end - maxDuration && this.start <= other.end + maxDuration) {
      return true
    }
    return false
  }

  /**
   * Converts a domain value to domain relative
   */
  fun time2relative(@Time @ms timeValue: Double): @TimeRelative @pct Double {
    val delta = timeValue - start
    return delta / span
  }

  /**
   * Calculates a delta for a time duration
   */
  fun time2relativeDelta(@Time @ms duration: Double): @TimeRelative @pct Double {
    return duration / span
  }

  /**
   * Converts a domain relative value back to a domain value
   */
  fun relative2time(@TimeRelative @pct timeRelative: Double): @Time Double {
    return timeRelative * span + start
  }

  /**
   * Calculates a delta for a relative time duration
   */
  @ms
  @Time
  fun relative2timeDelta(@TimeRelative @pct durationRelative: Double): Double {
    return durationRelative * span
  }

  /**
   * Formats the time range
   */
  fun format(): String {
    return "${start.formatUtc()} - ${end.formatUtc()}"
  }

  /**
   * Merges two time ranges that overlap each other
   */
  fun merge(other: TimeRange): TimeRange {
    //require overlap
    require(isOverlapping(other)) {
      "Cannot merge time ranges that do not overlap: $this - $other"
    }

    return spanning(other)
  }

  /**
   * Returns a time range that spans two time ranges:
   * Starts at the earliest start (of both time range) and ends at the latest end (of both time ranges)
   */
  fun spanning(other: TimeRange): TimeRange {
    return TimeRange(min(start, other.start), max(end, other.end))
  }

  override fun toString(): String {
    return "TimeRange[${start.formatUtc()} - ${end.formatUtc()}]"
  }

  /**
   * Returns a time range that respects the given bounds
   */
  fun fitWithin(earliestStart: @ms Double, latestEnd: @ms Double): TimeRange {
    if (earliestStart <= start && latestEnd >= end) {
      return this
    }

    return TimeRange(start.coerceAtLeast(earliestStart), end.coerceAtMost(latestEnd))
  }

  /**
   * Returns a time range that has been extended by the given amount - on both sides
   */
  fun extend(extend: @ms Double): TimeRange {
    return TimeRange(start - extend, end + extend)
  }

  companion object {
    /**
     * Creates a new time range using the provided *end* and duration
     */
    fun fromEndAndDuration(end: @ms Double, duration: @ms Double): TimeRange {
      return TimeRange(end - duration, end)
    }

    fun fromEndAndDuration(end: @ms Double, duration: @ms Duration): TimeRange {
      return TimeRange(end - duration.toDouble(DurationUnit.MILLISECONDS), end)
    }

    /**
     * Creates a new time range using the provided *end* and duration
     */
    fun fromStartAndDuration(start: @ms Double, duration: @ms Double): TimeRange {
      return TimeRange(start, start + duration)
    }

    fun fromStartAndDuration(start: @ms Double, duration: @ms Duration): TimeRange {
      return TimeRange(start, start + duration.toDouble(DurationUnit.MILLISECONDS))
    }

    /**
     * Returns a new time range that ends now and starts one minute before
     */
    fun oneMinuteUntilNow(): TimeRange {
      return fromEndAndDuration(nowMillis(), 60 * 1000.0)
    }

    /**
     * Returns a new time range that ends now and starts one hour before
     */
    fun oneHourUntilNow(): TimeRange {
      return fromEndAndDuration(nowMillis(), 60 * 60 * 1000.0)
    }

    /**
     * Returns a new time range that ends now and starts one day before
     */
    fun oneDayUntilNow(): TimeRange {
      return fromEndAndDuration(nowMillis(), 60 * 60 * 1000.0 * 24)
    }

    /**
     * The time range that starts at the reference timestamp and ends one minute later
     */
    val oneMinuteSinceReference: TimeRange = fromStartAndDuration(TimeConstants.referenceTimestamp, 60.0 * 1000.0)

    /**
     * The time range that starts at the reference timestamp and ends one hour later
     */
    val oneHourSinceReference: TimeRange = fromStartAndDuration(TimeConstants.referenceTimestamp, 60.0 * 60.0 * 1000.0)

    /**
     * The time range that starts at the reference timestamp and ends one day later
     */
    val oneDaySinceReference: TimeRange = fromStartAndDuration(TimeConstants.referenceTimestamp, 24.0 * 60.0 * 60.0 * 1000.0)

    /**
     * Creates a new time range from two values that may be unsorted (value1 might be smaller or larger than value 0)
     */
    fun fromUnsorted(value0: @ms Double, value1: @ms Double): TimeRange {
      if (value0 < value1) {
        return TimeRange(value0, value1)
      }

      return TimeRange(value1, value0)
    }

    /**
     * Compresses a list of time ranges; i.e. every mergeable time range within that list will be merged
     * [uncompressed] is a list containing time ranges - ordered by start date
     *
     * Attention: The uncompressed list must be sorted
     *
     * @param maxAcceptedGap: If the gap between two gap is smaller/equal to [maxAcceptedGap] the time ranges are merged
     */
    fun compress(uncompressed: List<@Sorted TimeRange>, maxAcceptedGap: @ms Double = 0.0): List<@Sorted TimeRange> {
      if (uncompressed.size <= 1) {
        return uncompressed
      }

      val compressed = mutableListOf<@Sorted TimeRange>()

      uncompressed.fastForEachIndexed { index, timeRange ->
        if (index == 0) {
          compressed.add(timeRange)
          return@fastForEachIndexed
        }

        val previous = compressed.last()

        //Check if it can be merged with the previous element
        if (previous.end + maxAcceptedGap >= timeRange.start) {
          val merged = previous.spanning(timeRange)

          //replace the merged element
          compressed.setLast(merged)
        } else {
          compressed.add(timeRange)
        }
      }

      return compressed
    }
  }
}

/**
 * Aligns this [TimeRange] to the given [timeSpan].
 *
 * * [TimeRange.start] of an aligned [TimeRange] is the closest integer multiple of [timeSpan] that is less than the original [TimeRange.start].
 * * [TimeRange.end] of an aligned [TimeRange] is the closest integer multiple of [timeSpan] that is greater than the original [TimeRange.end] plus [timeSpan] - 1.
 *
 * Note that an aligned [TimeRange] is greater or equal to its non-aligned version.
 *
 * Beware that this method treats all values as integer values.
 */
fun TimeRange.align(@ms timeSpan: Double): TimeRange {
  val oldStart = floor(this.start)
  val newStart = floor(oldStart / timeSpan) * timeSpan

  val oldEnd = ceil(this.end)
  val newEnd = floor(oldEnd / timeSpan) * timeSpan + timeSpan - 1

  check(newStart <= newEnd) { "$newStart > $newEnd" }
  check(newStart <= oldStart) { "$newStart > $oldStart" }
  check(newEnd >= oldEnd) { "$newEnd < $oldEnd" }
  return TimeRange(newStart, newEnd)
}


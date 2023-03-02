package com.meistercharts.history

import com.meistercharts.algorithms.TimeRange
import it.neckar.open.kotlin.lang.floor
import it.neckar.open.unit.other.Exclusive
import it.neckar.open.unit.other.Inclusive
import it.neckar.open.unit.si.ms
import kotlin.math.roundToInt

/**
 * Represents the range of a history bucket.
 *
 * ATTENTION: The bucket range is aligned relative to 1970-01-01. They do *not* take time zones into consideration.
 * When displaying the values the caller is responsible for handling the time zones correctly.
 *
 *
 * This class should *not* be published to the external API if possible. Instead use the [SamplingPeriod]
 *
 */
enum class HistoryBucketRange(
  /**
   * The sampling period
   */
  val samplingPeriod: SamplingPeriod,
  /**
   * The amount of entries
   */
  val entriesCount: Int
) {
  HundredMillis(SamplingPeriod.EveryMillisecond, 100),

  FiveSeconds(SamplingPeriod.EveryTenMillis, 500),

  OneMinute(SamplingPeriod.EveryHundredMillis, 600),

  TenMinutes(SamplingPeriod.EverySecond, 600),

  OneHour(SamplingPeriod.EveryTenSeconds, 360),

  SixHours(SamplingPeriod.EveryMinute, 360),

  /**
   * Represents 24 hours.
   */
  OneDay(SamplingPeriod.EveryTenMinutes, 6 * 24),

  /**
   * Represents 30 days. Does *NOT* start at monday but on thursday (UTC) - because 1970-01-01 has been a thursday.
   */
  ThirtyDays(SamplingPeriod.EveryHour, 24 * 30),

  /**
   * 90 days. Does *NOT* align with the calendar
   */
  OneQuarter(SamplingPeriod.Every6Hours, 4 * 90),

  /**
   * 360(!) days. Does *NOT* align with the calendar
   */
  OneYear(SamplingPeriod.Every24Hours, 360),

  /**
   * 5 times 360 days. Does *NOT* align with the calendar
   */
  FiveYears(SamplingPeriod.Every5Days, 72 * 5),

  /**
   * 30 times 360 days. Does *NOT* align with the calendar
   */
  ThirtyYears(SamplingPeriod.Every30Days, 12 * 30),

  /**
   * 90 times 360 days. Does *NOT* align with the calendar
   */
  NinetyYears(SamplingPeriod.Every90Days, 4 * 90),

  /**
   * 720 times 360 days. Does *NOT* align with the calendar
   */
  SevenHundredTwentyYears(SamplingPeriod.Every360Days, 720),

  ;

  /**
   * The duration of the bucket range - this is the product of the amount of entries and the resultion distance
   */
  val duration: @ms Double
    get() = samplingPeriod.distance * entriesCount

  /**
   * The distance between two data points
   */
  val distance: @ms Double
    get() = samplingPeriod.distance

  /**
   * Calculates the start of the bucket range for a given time.
   *
   * This method returns a value that is the same or smaller than the given time.
   */
  fun calculateStart(time: @ms Double): @ms Double {
    return calculateIndex(time) * duration
  }

  /**
   * Returns the start time for the bucket with the given index
   */
  fun calculateStartForIndex(index: Double): @Inclusive @ms Double {
    return index * duration
  }

  fun calculateEndForIndex(index: Double): @Exclusive @ms Double {
    return (index + 1) * duration
  }

  /**
   * Returns the index of the history bucket range for the given time stamp
   */
  fun calculateIndex(timestamp: @ms Double): Double {
    return (timestamp / duration).floor()
  }

  /**
   * Calculates the time range for the given time - for this bucket range
   * @return the time range from (inclusive) until to (exclusive)
   */
  fun calculateTimeRange(@ms time: Double): TimeRange {
    @ms val from = calculateStart(time)
    @ms val to = from + duration
    return TimeRange(from, to)
  }

  /**
   * Returns the exact value for the start time.
   * This method can be used to fix potentially rounding errors
   */
  fun roundStart(start: @ms Double): @ms Double {
    //Threshold is quarter of the duration
    return calculateStart(start + duration / 4.0)
  }

  /**
   * Returns the next lower bucket range - or null if there is no lower
   */
  fun lower(): HistoryBucketRange? {
    if (this == smallestRange) {
      return null
    }

    val index = values().indexOf(this)
    return values()[index - 1]
  }

  /**
   * Returns the next upper bucket range - or null if there is no upper
   */
  fun upper(): HistoryBucketRange? {
    if (this == greatestRange) {
      return null
    }

    val index = values().indexOf(this)
    return values()[index + 1]
  }

  /**
   * Calculates the amount of data points of the children that are averaged into one data point for this range.
   */
  fun downSamplingFactor(): Int {
    return lower()?.let {
      return (distance / it.distance).roundToInt()
    } ?: throw IllegalArgumentException("Not supported <$this>")
  }

  companion object {
    /**
     * Returns the history bucket range for the given resolution
     */
    fun find(samplingPeriod: SamplingPeriod): HistoryBucketRange {
      //Use when instead of values().forEach for performance reasons
      return when (samplingPeriod) {
        SamplingPeriod.EveryMillisecond -> HundredMillis
        SamplingPeriod.EveryTenMillis -> FiveSeconds
        SamplingPeriod.EveryHundredMillis -> OneMinute
        SamplingPeriod.EverySecond -> TenMinutes
        SamplingPeriod.EveryTenSeconds -> OneHour
        SamplingPeriod.EveryMinute -> SixHours
        SamplingPeriod.EveryTenMinutes -> OneDay
        SamplingPeriod.EveryHour -> ThirtyDays
        SamplingPeriod.Every6Hours -> OneQuarter
        SamplingPeriod.Every24Hours -> OneYear
        SamplingPeriod.Every5Days -> FiveYears
        SamplingPeriod.Every30Days -> ThirtyYears
        SamplingPeriod.Every90Days -> NinetyYears
        SamplingPeriod.Every360Days -> SevenHundredTwentyYears
      }
    }

    /**
     * The [HistoryBucketRange] with the smallest duration
     */
    val smallestRange: HistoryBucketRange = values().first()

    /**
     * The [HistoryBucketRange] with the greatest duration
     */
    val greatestRange: HistoryBucketRange = values().last()
  }
}

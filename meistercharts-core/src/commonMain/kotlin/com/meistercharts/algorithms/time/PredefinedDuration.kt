package com.meistercharts.algorithms.time

import it.neckar.open.collections.fastForEach
import it.neckar.open.unit.si.ms
import kotlin.time.*
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

/**
 * Enumeration that contains predefined durations that are often used
 */
@Deprecated("No longer required")
enum class PredefinedDuration(val millis: @ms Double, val label: String) {
  Duration365Days(365.days.toDouble(DurationUnit.MILLISECONDS), "365 days"),
  Duration168Days(168.days.toDouble(DurationUnit.MILLISECONDS), "168 days"),
  Duration28Days(28.days.toDouble(DurationUnit.MILLISECONDS), "28 days"),
  Duration15Days(15.days.toDouble(DurationUnit.MILLISECONDS), "15 days"),
  Duration24Hours(24.hours.toDouble(DurationUnit.MILLISECONDS), "24 hours"),
  Duration12Hours(12.hours.toDouble(DurationUnit.MILLISECONDS), "12 hours"),
  Duration1Hour(1.hours.toDouble(DurationUnit.MILLISECONDS), "1 hour"),
  Duration30Minutes(30.minutes.toDouble(DurationUnit.MILLISECONDS), "30 minutes"),
  Duration1Minute(1.minutes.toDouble(DurationUnit.MILLISECONDS), "1 minute"),
  Duration1Second(1.seconds.toDouble(DurationUnit.MILLISECONDS), "1 second"),
  Duration100Millis(100.milliseconds.toDouble(DurationUnit.MILLISECONDS), "100 millis"),
  Duration10Millis(10.milliseconds.toDouble(DurationUnit.MILLISECONDS), "10 millis"),
  Duration1Milli(1.milliseconds.toDouble(DurationUnit.MILLISECONDS), "1 milli"),
  ;


  companion object {
    /**
     * Contains all values
     */
    private val values: Array<PredefinedDuration> = values()
    private val valuesReversed: Array<PredefinedDuration> = values().reversedArray()

    /**
     * Returns the largest predefined configuration that has the same or smaller duration than
     * the given value
     */
    fun sameOrSmallerThan(maxMillis: @ms Double): PredefinedDuration {
      values.fastForEach {
        if (it.millis <= maxMillis) {
          return it
        }
      }

      throw IllegalArgumentException("No predefined configuration same or smaller than <$maxMillis> found")
    }

    /**
     * Returns the smallest predefined configuration that has the same or larger duration than
     * the given value
     */
    fun sameOrLargerThan(minMillis: @ms Double): PredefinedDuration {
      valuesReversed.fastForEach {
        if (it.millis >= minMillis) {
          return it
        }
      }

      throw IllegalArgumentException("No predefined configuration same or larger than <$minMillis> found")
    }
  }

  override fun toString(): String {
    return "PredefinedDuration($label <$millis ms>)"
  }
}

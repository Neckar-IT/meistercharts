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
package com.meistercharts.algorithms.axis

import com.meistercharts.algorithms.time.PredefinedDuration
import korlibs.time.DateTime
import korlibs.time.DateTimeSpan
import korlibs.time.DateTimeTz
import korlibs.time.Month
import korlibs.time.MonthSpan
import korlibs.time.days
import korlibs.time.hours
import korlibs.time.minutes
import korlibs.time.seconds
import it.neckar.open.collections.DoubleArrayList
import it.neckar.open.collections.emptyDoubleArray
import it.neckar.open.collections.fastForEach
import it.neckar.open.formatting.dateFormat
import it.neckar.open.formatting.dateTimeFormat
import it.neckar.open.formatting.dateTimeFormatShort
import it.neckar.open.formatting.dateTimeFormatShortWithMillis
import it.neckar.open.formatting.formatUtc
import it.neckar.open.formatting.yearMonthFormat
import it.neckar.open.i18n.I18nConfiguration
import it.neckar.open.kotlin.lang.floor
import it.neckar.open.kotlin.lang.isCloseToOrLessThan
import it.neckar.open.kotlin.lang.log10
import it.neckar.open.kotlin.lang.toIntCeil
import it.neckar.open.time.TimeZone
import it.neckar.open.unit.other.Inclusive
import it.neckar.open.unit.si.ms
import it.neckar.open.unit.si.s
import it.neckar.open.unit.time.min
import kotlin.math.pow

/**
 * Describes a time tick distance
 */
sealed interface TimeTickDistance : Comparable<TimeTickDistance> {
  /**
   * Calculates the tick values from the given start until the end
   */
  fun calculateTicks(
    start: @ms @Inclusive Double,
    end: @ms @Inclusive Double,
    timeZone: TimeZone,
    skipTicksBeforeStart: Boolean,
  ): @ms DoubleArray {
    require(start < end) { "Start <${start.formatUtc()}> must be smaller than end <${end.formatUtc()}>" }

    if (!start.isInKlockSupportedRange() || !end.isInKlockSupportedRange()) {
      //Unsupported range - fallback and return empty ticks array
      return emptyDoubleArray()
    }

    val startTz: DateTimeTz = DateTime(start).utc2DateTimeTz(timeZone)
    val endTz: DateTimeTz = DateTime(end).utc2DateTimeTz(timeZone)

    //The first tick
    val firstTick: DateTimeTz = calculateFirstTick(startTz, timeZone) ?: return emptyDoubleArray()

    //Klock does only support full milliseconds. Therefore, we can be off by 1
    require(firstTick.utc.unixMillis.isCloseToOrLessThan(start, 1.0)) {
      "First tick (${firstTick.utc.unixMillis.formatUtc()}) must be before/same as start (${start.formatUtc()}) for $this. (start: ${start.formatUtc()} - end: ${end.formatUtc()}"
    }

    return calculateTicks(startTz, firstTick, endTz, timeZone, skipTicksBeforeStart)
  }

  /**
   * Calculates the first tick - which must be lower than start.
   *
   * Returns null if no tick can be calculated. This happens if the supported library (Klock) does not support the time range
   */
  fun calculateFirstTick(exact: DateTimeTz, timeZone: TimeZone): DateTimeTz?

  /**
   * Calculates the tick values
   */
  fun calculateTicks(
    start: @Inclusive DateTimeTz,
    firstTick: DateTimeTz,
    end: @Inclusive DateTimeTz,
    timeZone: TimeZone,
    skipTicksBeforeStart: Boolean
  ): @ms DoubleArray {

    var currentTick = firstTick

    //Skip the ticks before start
    if (skipTicksBeforeStart) {
      while (currentTick < start) {
        currentTick = nextTick(currentTick, timeZone)
      }
    }

    val ticks = DoubleArrayList(0)
    while (currentTick <= end) {
      ticks.add(currentTick.utc.unixMillisDouble)
      currentTick = nextTick(currentTick, timeZone)
    }

    return ticks.toDoubleArray()
  }

  fun nextTick(currentTick: DateTimeTz, timeZone: TimeZone): DateTimeTz {
    return currentTick.plus(dateTimeSpan)
  }

  /**
   * Formats the given millis as offset
   */
  fun formatAsOffset(millis: @ms Double, i18nConfiguration: I18nConfiguration): String

  /**
   * Returns a "global" index for the tick described by the given millis.
   * The index can then be used to select alternating colors (using modulo)
   *
   * Attention: The returned value is *guess*! It should not be used to calculate real values at all.
   */
  fun calculateEstimatedIndex(millis: @ms Double, timeZone: TimeZone): GlobalTimeIndex

  /**
   * Returns the smallest possible tick distance for this *offset* distance
   * @return null if all tick distances are supported
   */
  fun smallestPossibleTickDistance(): TimeTickDistance

  /**
   * Compares two tick distances
   */
  override operator fun compareTo(other: TimeTickDistance): Int {
    return this.dateTimeSpan.compareTo(other.dateTimeSpan)
  }

  /**
   * The tick distance as date time span
   */
  val dateTimeSpan: DateTimeSpan

  companion object {
    /**
     * Returns the ideal time tick distance for the given min tick distance - use when calculating ticks
     */
    fun forTicks(minTickDistance: @ms Double): TimeTickDistance {
      require(minTickDistance >= 0.0) { "min tick distance must be greater than or equal to 0 but was <$minTickDistance>" }

      return when {
        minTickDistance > PredefinedDuration.Duration365Days.millis -> {
          DistanceYears.atLeast(minTickDistance, Factors.All)
        }

        minTickDistance > PredefinedDuration.Duration168Days.millis -> {
          DistanceYears(1)
        }

        minTickDistance > PredefinedDuration.Duration15Days.millis -> {
          DistanceMonths.atLeast(minTickDistance)
        }

        minTickDistance > PredefinedDuration.Duration12Hours.millis -> {
          DistanceDays.atLeast(minTickDistance)
        }

        minTickDistance > PredefinedDuration.Duration30Minutes.millis -> {
          DistanceHours.atLeast(minTickDistance)
        }

        minTickDistance > PredefinedDuration.Duration1Minute.millis -> {
          DistanceMinutes.atLeast(minTickDistance)
        }

        minTickDistance > 500.0 -> {
          DistanceSeconds.atLeast(minTickDistance)
        }

        else -> {
          /**
           * If the min tick distance is smaller than 500 millis, find the smaller magnitude
           */
          DistanceMillis.atLeast(minTickDistance, Factors.All)
        }
      }
    }

    /**
     * Returns the ideal time tick distance when calculating ticks for offsets
     */
    fun forOffsets(minTickDistance: @ms Double): TimeTickDistance {
      require(minTickDistance >= 0.0) { "min tick distance must be greater than or equal to 0 but was <$minTickDistance>" }

      return when {
        minTickDistance > 60.days.milliseconds -> {
          DistanceYears.atLeast(minTickDistance, Factors.Only10s)
        }

        minTickDistance > 2.days.milliseconds -> {
          DistanceMonths(1)
        }

        minTickDistance > 2.hours.milliseconds -> {
          DistanceDays(1)
        }

        minTickDistance > 2.minutes.milliseconds -> {
          DistanceHours(1)
        }

        minTickDistance > 2.seconds.milliseconds -> {
          DistanceMinutes(1)
        }

        minTickDistance > 500.0 -> {
          DistanceSeconds(1)
        }

        else -> {
          /**
           * If the min tick distance is smaller than 500 millis, find the smaller magnitude
           */
          DistanceMillis.atLeast(minTickDistance, Factors.Only10s)
        }
      }
    }
  }
}

/**
 * Distance in years
 */
class DistanceYears(val years: @ms Int) : TimeTickDistance {
  override val dateTimeSpan: DateTimeSpan = DateTimeSpan(years, 0, 0, 0, 0, 0, 0, 0.0)
  override fun calculateFirstTick(exact: DateTimeTz, timeZone: TimeZone): DateTimeTz? {
    val years = (exact.year.year / years) * years

    if (years < 1) {
      //Klock only supports dates with positive years
      return null
    }

    return DateTime(years, Month.January, 1, 0, 0, 0).local2DateTimeTz(timeZone)
  }

  override fun formatAsOffset(millis: @ms Double, i18nConfiguration: I18nConfiguration): String {
    val toDateTimeTz = DateTime(millis).utc2DateTimeTz(i18nConfiguration.timeZone)

    if (years == 1) {
      return toDateTimeTz.yearInt.toString()
    }

    toDateTimeTz.yearInt.let {
      return (it / years * years).toString()
    }
  }

  override fun calculateEstimatedIndex(millis: Double, timeZone: TimeZone): GlobalTimeIndex {
    val toDateTimeTz = DateTime(millis).utc2DateTimeTz(timeZone)
    return GlobalTimeIndex(toDateTimeTz.yearInt / years)
  }

  override fun smallestPossibleTickDistance(): TimeTickDistance {
    return DistanceDays(1)
  }

  override fun toString(): String {
    return "DistanceYears($years)"
  }

  companion object {
    fun atLeast(minTickDistance: @ms Double, factors: Factors): DistanceYears {
      @min val minTickDistanceInYears = minTickDistance / PredefinedDuration.Duration365Days.millis
      val log10 = minTickDistanceInYears.log10().floor()
      val magnitude = 10.0.pow(log10)
      val factor = factors.calculateFactor(minTickDistanceInYears / magnitude)
      val tickDistanceYears = magnitude * factor
      return DistanceYears(tickDistanceYears.toIntCeil())
    }

    val OneYear: DistanceYears = DistanceYears(1)
  }
}

/**
 * Distance in months
 */
class DistanceMonths(val months: @ms Int) : TimeTickDistance {
  override val dateTimeSpan: DateTimeSpan = DateTimeSpan(0, months)

  override fun calculateFirstTick(exact: DateTimeTz, timeZone: TimeZone): DateTimeTz {
    val startMonth = if (exact.month < Month.July) Month.January else Month.July
    return DateTime(exact.year, startMonth, 1, 0, 0, 0).local2DateTimeTz(timeZone)
  }

  override fun formatAsOffset(millis: Double, i18nConfiguration: I18nConfiguration): String {
    return yearMonthFormat.format(millis, i18nConfiguration)
  }

  override fun calculateEstimatedIndex(millis: Double, timeZone: TimeZone): GlobalTimeIndex {
    val toDateTimeTz = DateTime(millis).utc2DateTimeTz(timeZone)
    return GlobalTimeIndex(toDateTimeTz.month0 / months)
  }

  override fun smallestPossibleTickDistance(): TimeTickDistance {
    return DistanceDays(1)
  }

  override fun toString(): String {
    return "DistanceMonths($months)"
  }

  companion object {
    private val possibleValues = intArrayOf(1, 2, 3, 6)

    fun atLeast(minTickDistance: @ms Double): DistanceMonths {
      @min val minTickDistanceInMonths = minTickDistance / PredefinedDuration.Duration28Days.millis
      @min val tickDistanceInMonths = possibleValues.first { it >= minTickDistanceInMonths }

      return DistanceMonths(tickDistanceInMonths)
    }
  }
}

/**
 * Distance in days
 */
class DistanceDays(val days: @ms Int) : TimeTickDistance {
  override val dateTimeSpan: DateTimeSpan = DateTimeSpan(0, 0, 0, days)

  override fun calculateFirstTick(exact: DateTimeTz, timeZone: TimeZone): DateTimeTz {
    val day = if (exact.dayOfMonth > 15) 16 else 1

    return DateTime(exact.year, exact.month, day, 0, 0, 0).local2DateTimeTz(timeZone)
  }

  override fun nextTick(currentTick: DateTimeTz, timeZone: TimeZone): DateTimeTz {
    if (days == 1) { // a tick every day
      //this can be handled by the DateTimeTz instance itself
      return currentTick.plus(dateTimeSpan)
    }

    if (days <= 5) { //a tick every 1., 5., 10., 15., 20., 25.
      val dayOfMonth = currentTick.dayOfMonth
      return when {
        dayOfMonth < 5 -> DateTime(currentTick.year, currentTick.month, 5).local2DateTimeTz(timeZone)
        dayOfMonth < 10 -> DateTime(currentTick.year, currentTick.month, 10).local2DateTimeTz(timeZone)
        dayOfMonth < 15 -> DateTime(currentTick.year, currentTick.month, 15).local2DateTimeTz(timeZone)
        dayOfMonth < 20 -> DateTime(currentTick.year, currentTick.month, 20).local2DateTimeTz(timeZone)
        dayOfMonth < 25 -> DateTime(currentTick.year, currentTick.month, 25).local2DateTimeTz(timeZone)
        else -> DateTime(currentTick.year, currentTick.month, 1).plus(MonthSpan(1)).local2DateTimeTz(timeZone)
      }
    }

    if (days <= 10) { //a tick every 1., 10., 20.
      val dayOfMonth = currentTick.dayOfMonth
      return when {
        dayOfMonth < 10 -> DateTime(currentTick.year, currentTick.month, 10).local2DateTimeTz(timeZone)
        dayOfMonth < 20 -> DateTime(currentTick.year, currentTick.month, 20).local2DateTimeTz(timeZone)
        else -> DateTime(currentTick.year, currentTick.month, 1).plus(MonthSpan(1)).local2DateTimeTz(timeZone)
      }
    }

    //a tick every 1. and 15.
    val dayOfMonth = currentTick.dayOfMonth
    return when {
      dayOfMonth < 15 -> DateTime(currentTick.year, currentTick.month, 15).local2DateTimeTz(timeZone)
      else -> DateTime(currentTick.year, currentTick.month, 1).plus(MonthSpan(1)).local2DateTimeTz(timeZone)
    }
  }

  override fun formatAsOffset(millis: Double, i18nConfiguration: I18nConfiguration): String {
    return dateFormat.format(millis, i18nConfiguration)
  }

  override fun calculateEstimatedIndex(millis: Double, timeZone: TimeZone): GlobalTimeIndex {
    val toDateTimeTz = DateTime(millis).utc2DateTimeTz(timeZone)
    return GlobalTimeIndex(toDateTimeTz.dayOfYear / days)
  }

  override fun smallestPossibleTickDistance(): TimeTickDistance {
    return DistanceMinutes(1)
  }

  override fun toString(): String {
    return "DistanceDays($days)"
  }

  companion object {
    private val possibleValues = intArrayOf(1, 5, 10, 15)

    fun atLeast(minTickDistance: @ms Double): DistanceDays {
      @min val minTickDistanceInDays = minTickDistance / PredefinedDuration.Duration24Hours.millis
      @min val tickDistanceInDays = possibleValues.first { it >= minTickDistanceInDays }

      return DistanceDays(tickDistanceInDays)
    }
  }
}

/**
 * Distance in hours
 */
class DistanceHours(val hours: @ms Int) : TimeTickDistance {
  override val dateTimeSpan: DateTimeSpan = DateTimeSpan(0, 0, 0, 0, hours)

  override fun calculateFirstTick(exact: DateTimeTz, timeZone: TimeZone): DateTimeTz {
    val startHours: Int = (exact.hours / hours) * hours

    return DateTime(exact.year, exact.month, exact.dayOfMonth, startHours, 0, 0).local2DateTimeTz(timeZone)
  }

  override fun formatAsOffset(millis: Double, i18nConfiguration: I18nConfiguration): String {
    return dateTimeFormatShort.format(millis, i18nConfiguration)
  }

  override fun calculateEstimatedIndex(millis: Double, timeZone: TimeZone): GlobalTimeIndex {
    val toDateTimeTz = DateTime(millis).utc2DateTimeTz(timeZone)
    return GlobalTimeIndex(toDateTimeTz.hours / hours)
  }

  override fun smallestPossibleTickDistance(): TimeTickDistance {
    return DistanceMillis.smallest
  }

  override fun toString(): String {
    return "DistanceHours(hours=$hours)"
  }

  companion object {
    private val possibleValues = intArrayOf(1, 2, 3, 4, 6, 12)

    fun atLeast(minTickDistance: @ms Double): DistanceHours {
      @min val minTickDistanceInHours = minTickDistance / PredefinedDuration.Duration1Hour.millis
      @min val tickDistanceInHours = possibleValues.first { it >= minTickDistanceInHours }

      return DistanceHours(tickDistanceInHours)
    }
  }
}

/**
 * Distance in minutes
 */
class DistanceMinutes(val minutes: @ms Int) : TimeTickDistance {
  override val dateTimeSpan: DateTimeSpan = DateTimeSpan(0, 0, 0, 0, 0, minutes)

  override fun calculateFirstTick(exact: DateTimeTz, timeZone: TimeZone): DateTimeTz {
    val startMinutes: Int = (exact.minutes / minutes) * minutes

    return DateTime(exact.year, exact.month, exact.dayOfMonth, exact.hours, startMinutes, 0).local2DateTimeTz(timeZone)
  }

  override fun formatAsOffset(millis: Double, i18nConfiguration: I18nConfiguration): String {
    return dateTimeFormatShort.format(millis, i18nConfiguration)
  }

  override fun calculateEstimatedIndex(millis: Double, timeZone: TimeZone): GlobalTimeIndex {
    val toDateTimeTz = DateTime(millis).utc2DateTimeTz(timeZone)
    return GlobalTimeIndex(toDateTimeTz.minutesOfDay / minutes)
  }

  override fun smallestPossibleTickDistance(): TimeTickDistance {
    return DistanceMillis.smallest
  }

  override fun toString(): String {
    return "DistanceMinutes($minutes)"
  }

  companion object {
    private val possibleValues: IntArray = intArrayOf(1, 2, 5, 10, 15, 30)

    fun atLeast(minTickDistance: @ms Double): DistanceMinutes {
      @min val minTickDistanceInMinutes = minTickDistance / PredefinedDuration.Duration1Minute.millis
      @min val tickDistanceInMinutes = possibleValues.first { it >= minTickDistanceInMinutes }


      return DistanceMinutes(tickDistanceInMinutes)
    }
  }
}

/**
 * Distance in seconds
 */
class DistanceSeconds(val seconds: @ms Int) : TimeTickDistance {
  override val dateTimeSpan: DateTimeSpan = DateTimeSpan(0, 0, 0, 0, 0, 0, seconds)

  override fun calculateFirstTick(exact: DateTimeTz, timeZone: TimeZone): DateTimeTz {
    val startSeconds: Int = (exact.seconds / seconds) * seconds

    return DateTime(exact.year, exact.month, exact.dayOfMonth, exact.hours, exact.minutes, startSeconds).local2DateTimeTz(timeZone)
  }

  override fun formatAsOffset(millis: Double, i18nConfiguration: I18nConfiguration): String {
    return dateTimeFormat.format(millis, i18nConfiguration)
  }

  override fun calculateEstimatedIndex(millis: Double, timeZone: TimeZone): GlobalTimeIndex {
    val toDateTimeTz = DateTime(millis).utc2DateTimeTz(timeZone)
    return GlobalTimeIndex(toDateTimeTz.secondsOfDay / seconds)
  }

  override fun smallestPossibleTickDistance(): TimeTickDistance {
    return DistanceMillis.smallest
  }

  override fun toString(): String {
    return "DistanceSeconds($seconds)"
  }

  companion object {
    private val possibleValues: IntArray = intArrayOf(1, 2, 5, 10, 15, 30, 60)

    fun atLeast(minTickDistance: @ms Double): DistanceSeconds {
      @s val minTickDistanceInSeconds = minTickDistance / PredefinedDuration.Duration1Second.millis
      @s val tickDistanceInSeconds = possibleValues.first { it >= minTickDistanceInSeconds }

      return DistanceSeconds(tickDistanceInSeconds)
    }
  }
}

/**
 * A time tick distance in millis
 */
class DistanceMillis(val millis: @ms Double) : TimeTickDistance {
  override val dateTimeSpan: DateTimeSpan = DateTimeSpan(0, 0, 0, 0, 0, 0, 0, millis)

  override fun calculateFirstTick(exact: DateTimeTz, timeZone: TimeZone): DateTimeTz {
    val startMillis: Int = if (millis < 0) {
      exact.milliseconds
    } else {
      ((exact.milliseconds / millis).floor() * millis).toInt()
    }

    return DateTime(exact.year, exact.month, exact.dayOfMonth, exact.hours, exact.minutes, exact.seconds, startMillis).local2DateTimeTz(timeZone)
  }

  override fun formatAsOffset(millis: Double, i18nConfiguration: I18nConfiguration): String {
    return dateTimeFormatShortWithMillis.format(millis, i18nConfiguration)
  }

  override fun calculateEstimatedIndex(millis: Double, timeZone: TimeZone): GlobalTimeIndex {
    return GlobalTimeIndex(((millis / this.millis) % Int.MAX_VALUE).toInt())
  }

  override fun smallestPossibleTickDistance(): TimeTickDistance {
    return smallest
  }

  override fun toString(): String {
    return "DistanceMillis($millis)"
  }

  companion object {
    val smallest: DistanceMillis = DistanceMillis(1.0)

    fun atLeast(minTickDistance: @ms Double, factors: Factors): DistanceMillis {
      val log10 = minTickDistance.log10().floor()
      val magnitude = 10.0.pow(log10)
      val factor = factors.calculateFactor(minTickDistance / magnitude)
      @ms val tickDistance = (magnitude * factor).coerceAtLeast(1.0)
      check(tickDistance >= minTickDistance) { "$tickDistance < $minTickDistance" }

      return DistanceMillis(tickDistance)
    }
  }
}


enum class Factors(val values: DoubleArray) {
  All(doubleArrayOf(2.0, 2.5, 5.0, 10.0)),
  Default(doubleArrayOf(2.0, 5.0, 10.0)),
  Only5s(doubleArrayOf(5.0, 10.0)),
  Only10s(doubleArrayOf(10.0));


  /**
   * Calculates a factor
   */
  fun calculateFactor(minValue: Double): Double {
    var factor = 1.0
    while (factor < minValue) {
      values.fastForEach {
        if (factor * it > minValue) {
          return factor * it
        }
      }
      factor *= values.last()
    }
    return factor
  }
}

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

import com.meistercharts.axis.time.DistanceDays.TicksPerMonth.EveryDay
import it.neckar.datetime.minimal.DayOfMonth
import it.neckar.datetime.minimal.LocalDate
import it.neckar.datetime.minimal.LocalDateTime
import it.neckar.datetime.minimal.Month
import it.neckar.datetime.minimal.TimeConstants
import it.neckar.datetime.minimal.TimeZone
import it.neckar.datetime.minimal.Year
import it.neckar.datetime.minimal.fromMillisCurrentTimeZone
import it.neckar.datetime.minimal.toMillis
import it.neckar.datetime.minimal.toMillisAtStartOfDay
import it.neckar.open.collections.DoubleArrayList
import it.neckar.open.collections.emptyIntArray
import it.neckar.open.collections.fastForEach
import it.neckar.open.collections.fastForEachReversed
import it.neckar.open.collections.first
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
import it.neckar.open.kotlin.lang.roundDownToBase
import it.neckar.open.kotlin.lang.toIntCeil
import it.neckar.open.kotlin.lang.toIntFloor
import it.neckar.open.time.formatUtcForDebug
import it.neckar.open.unit.other.Inclusive
import it.neckar.open.unit.si.ms
import it.neckar.open.unit.si.s
import it.neckar.open.unit.time.a
import it.neckar.open.unit.time.min
import kotlin.math.pow
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit

/**
 * Describes a time tick distance
 *
 * ATTENTION: The time zone is *not* supported if it is different from the local time zone.
 * JS does not support other time zones than the currently set.
 *
 * But: In the future we want to support other time zones somehow.
 */
sealed interface TimeTickDistance : Comparable<TimeTickDistance> {
  /**
   * Calculates the tick values from the given start until the end
   */
  fun calculateTicks(
    start: @ms @Inclusive Double,
    end: @ms @Inclusive Double,
    timeZone: TimeZone, //TODO remove time zone - or check if the timezone is valid!
  ): @ms DoubleArrayList {
    require(start < end) { "Start <${start.formatUtc()}> must be smaller than end <${end.formatUtc()}>" }

    val ticks = calculateTicksInternal(start, end, timeZone)

    //Plausibility check
    if (ticks.isNotEmpty()) {
      @ms val firstTick: @ms Double = ticks.first()

      require(firstTick.isCloseToOrLessThan(start, 1.0)) {
        "First tick (${firstTick.formatUtc()}) must be before/same as start (${start.formatUtc()}) for $this. (start: ${start.formatUtc()} - end: ${end.formatUtc()}"
      }
    }

    return ticks
  }

  /**
   * Calculates the tick values - without any checks
   */
  fun calculateTicksInternal(
    start: @Inclusive @ms Double,
    end: @Inclusive @ms Double,
    timeZone: TimeZone,
  ): @ms DoubleArrayList

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
   * Compares two tick distances.
   * * First criteria: [timeMagnitude].
   * * Second criteria: [typicalDistanceForComparison]
   */
  override operator fun compareTo(other: TimeTickDistance): Int {
    return when {
      timeMagnitude != other.timeMagnitude -> {
        timeMagnitude.compareTo(other.timeMagnitude)
      }

      else -> {
        typicalDistanceForComparison().compareTo(other.typicalDistanceForComparison())
      }
    }
  }

  /**
   * Returns the distance between two ticks in millis.
   * This method is (only) used in [compareTo]
   */
  fun typicalDistanceForComparison(): @ms Double

  /**
   * Returns the time magnitude.
   * This method is (only) used in [compareTo]
   */
  val timeMagnitude: TimeMagnitude

  companion object {
    /**
     * Returns the ideal time tick distance for the given min tick distance - use when calculating ticks
     */
    fun forTicks(minTickDistance: @ms Double): TimeTickDistance {
      require(minTickDistance >= 0.0) { "min tick distance must be greater than or equal to 0 but was <$minTickDistance>" }

      return when {
        minTickDistance > 365.days.toDouble(DurationUnit.MILLISECONDS) -> {
          DistanceYears.atLeast(minTickDistance, Factors.All)
        }

        minTickDistance > 168.days.toDouble(DurationUnit.MILLISECONDS) -> {
          DistanceYears(1)
        }

        minTickDistance > 15.days.toDouble(DurationUnit.MILLISECONDS) -> {
          DistanceMonths.atLeast(minTickDistance)
        }

        minTickDistance > 12.hours.toDouble(DurationUnit.MILLISECONDS) -> {
          DistanceDays.atLeast(minTickDistance)
        }

        minTickDistance > 30.minutes.toDouble(DurationUnit.MILLISECONDS) -> {
          DistanceHours.atLeast(minTickDistance)
        }

        minTickDistance > 1.minutes.toDouble(DurationUnit.MILLISECONDS) -> {
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
        minTickDistance > 60.days.toDouble(DurationUnit.MILLISECONDS) -> {
          DistanceYears.atLeast(minTickDistance, Factors.Only10s)
        }

        minTickDistance > 2.days.toDouble(DurationUnit.MILLISECONDS) -> {
          DistanceMonths(1)
        }

        minTickDistance > 2.hours.toDouble(DurationUnit.MILLISECONDS) -> {
          DistanceDays(EveryDay)
        }

        minTickDistance > 2.minutes.toDouble(DurationUnit.MILLISECONDS) -> {
          DistanceHours(1)
        }

        minTickDistance > 2.seconds.toDouble(DurationUnit.MILLISECONDS) -> {
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

  /**
   * Used to compare two [TimeTickDistance] instances
   */
  enum class TimeMagnitude {
    Millis,
    Seconds,
    Minutes,
    Hours,
    Days,
    Months,
    Years,
  }
}

/**
 * Distance in years
 */
class DistanceYears(val distanceInYears: @a Int) : TimeTickDistance {
  override val timeMagnitude: TimeTickDistance.TimeMagnitude = TimeTickDistance.TimeMagnitude.Years

  override fun typicalDistanceForComparison(): @ms Double {
    return distanceInYears * TimeConstants.millisPerYear
  }

  override fun calculateTicksInternal(start: Double, end: Double, timeZone: TimeZone): DoubleArrayList {
    val startLocalDateExact = LocalDate.fromMillisCurrentTimeZone(start, timeZone).atStartOfYear()
    val endLocalDate = LocalDate.fromMillisCurrentTimeZone(end, timeZone).atStartOfNextYear()

    val startLocalDate = startLocalDateExact.withYear(
      Year(startLocalDateExact.year.value.roundDownToBase(distanceInYears))
    )

    return DoubleArrayList().also {
      var current = startLocalDate

      while (current <= endLocalDate) {
        it.add(current.toMillisAtStartOfDay(timeZone))
        current = current.plusYears(distanceInYears)
      }
    }
  }

  override fun formatAsOffset(millis: @ms Double, i18nConfiguration: I18nConfiguration): String {
    val localDate = LocalDate.fromMillisCurrentTimeZone(millis, i18nConfiguration.timeZone)

    if (distanceInYears == 1) {
      //Show every year
      return localDate.year.value.toString()
    }

    val yearAsInt = localDate.year.value
    val floored = yearAsInt / distanceInYears * distanceInYears

    return floored.toString()
  }

  override fun calculateEstimatedIndex(millis: @ms Double, timeZone: TimeZone): GlobalTimeIndex {
    val localDate = LocalDate.fromMillisCurrentTimeZone(millis, timeZone)
    return GlobalTimeIndex(localDate.year.value / distanceInYears)
  }

  override fun smallestPossibleTickDistance(): TimeTickDistance {
    return DistanceDays(EveryDay)
  }

  override fun toString(): String {
    return "DistanceYears($distanceInYears)"
  }

  companion object {
    fun atLeast(minTickDistance: @ms Double, factors: Factors): DistanceYears {
      @min val minTickDistanceInYears = minTickDistance / 365.days.toDouble(DurationUnit.MILLISECONDS)
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
class DistanceMonths(val distanceInMonths: Int) : TimeTickDistance {
  init {
    require(distanceInMonths in 1..11) { "invalid distanceInMonths: $distanceInMonths" }
  }

  override val timeMagnitude: TimeTickDistance.TimeMagnitude = TimeTickDistance.TimeMagnitude.Months

  override fun typicalDistanceForComparison(): @ms Double {
    return distanceInMonths * TimeConstants.millisPerDay * 30
  }

  override fun calculateTicksInternal(start: Double, end: Double, timeZone: TimeZone): DoubleArrayList {
    val startLocalDateExact = LocalDate.fromMillisCurrentTimeZone(start, timeZone).atStartOfMonth()
    val endLocalDate = LocalDate.fromMillisCurrentTimeZone(end, timeZone).atStartOfNextMonth()

    val startLocalDate = startLocalDateExact.withMonth(
      Month((startLocalDateExact.month.value - 1).roundDownToBase(distanceInMonths).plus(1))
    )

    return DoubleArrayList().also {
      var current = startLocalDate

      while (current <= endLocalDate) {
        it.add(current.toMillisAtStartOfDay(timeZone))
        current = current.plusMonths(distanceInMonths)
      }
    }
  }

  override fun formatAsOffset(millis: Double, i18nConfiguration: I18nConfiguration): String {
    return yearMonthFormat.format(millis, i18nConfiguration)
  }

  override fun calculateEstimatedIndex(millis: Double, timeZone: TimeZone): GlobalTimeIndex {
    val localDate = LocalDate.fromMillisCurrentTimeZone(millis, timeZone)
    return GlobalTimeIndex(localDate.monthOfEpoche() / distanceInMonths)
  }

  override fun smallestPossibleTickDistance(): TimeTickDistance {
    return DistanceDays(EveryDay)
  }

  override fun toString(): String {
    return "DistanceMonths($distanceInMonths)"
  }

  companion object {
    private val possibleValues = intArrayOf(1, 2, 3, 6)

    fun atLeast(minTickDistance: @ms Double): DistanceMonths {
      @min val minTickDistanceInMonths = minTickDistance / 28.days.toDouble(DurationUnit.MILLISECONDS)
      @min val tickDistanceInMonths = possibleValues.first { it >= minTickDistanceInMonths }

      return DistanceMonths(tickDistanceInMonths)
    }
  }
}

/**
 * Distance in days
 */
class DistanceDays(val ticksPerMonth: TicksPerMonth) : TimeTickDistance {
  override val timeMagnitude: TimeTickDistance.TimeMagnitude = TimeTickDistance.TimeMagnitude.Days

  override fun typicalDistanceForComparison(): @ms Double {
    return ticksPerMonth.worstCaseDaysDistance() * TimeConstants.millisPerDay
  }

  /**
   * Contains the possible distances for days
   */
  enum class TicksPerMonth(
    /**
     * Contains the potential days.
     * Attention: Is left empty for [EveryDay].
     */
    val days: IntArray,
  ) {
    EveryDay(emptyIntArray()),
    Every5Days(intArrayOf(1, 5, 10, 15, 20, 25)),
    Every10Days(intArrayOf(1, 10, 20)),
    Every15Days(intArrayOf(1, 15)),
    ;

    /**
     * Returns the distance between two ticks in days - in the worst case
     */
    fun worstCaseDaysDistance(): Int {
      return when (this) {
        EveryDay -> 1
        Every5Days -> 7 //25 --> 32
        Every10Days -> 12 //20 --> 32
        Every15Days -> 17 //15 --> 32
      }
    }

    fun typicalCaseDaysDistance(): Int {
      return when (this) {
        EveryDay -> 1
        Every5Days -> 5
        Every10Days -> 10
        Every15Days -> 15
      }
    }

    /**
     * Returns the same day - or the one below for this enum option
     */
    fun sameOrBelow(day: DayOfMonth): DayOfMonth {
      when (this) {
        EveryDay -> return day
        Every5Days, Every10Days, Every15Days -> {
          days.fastForEachReversed { potentialDayAsInt ->
            if (potentialDayAsInt <= day.value) {
              return DayOfMonth(potentialDayAsInt)
            }
          }
        }
      }
      throw IllegalStateException("No valid day found! for $day")
    }

    /**
     * Iterates from start to end. Calls the callback for each steap
     */
    inline fun iterate(start: @Inclusive LocalDate, end: @Inclusive LocalDate, callback: (LocalDate) -> Unit) {
      var current = start

      while (current <= end) {
        callback(current)

        current = calculateNext(current)
      }
    }

    /**
     * Calculates the next local date
     */
    fun calculateNext(current: @Inclusive LocalDate): @Inclusive LocalDate {
      return when (this) {
        EveryDay -> current.plusDays(1)
        Every5Days -> {
          val nextDayValue = days.firstOrNull { it > current.dayOfMonth.value } ?: return current.atStartOfNextMonth()
          current.atDayOfMonth(DayOfMonth(nextDayValue))
        }

        Every10Days, Every15Days -> {
          val nextDayValue = days.firstOrNull { it > current.dayOfMonth.value } ?: return current.atStartOfNextMonth()
          current.atDayOfMonth(DayOfMonth(nextDayValue))
        }
      }
    }

    /**
     * Returns the ticks per month (estimation)
     */
    fun ticksPerMonth(): Int {
      return when (this) {
        EveryDay -> 31
        else -> days.size
      }
    }

    companion object {
      fun forMinDistance(minTickDistance: @ms Double): TicksPerMonth {
        @min val minTickDistanceInDays = minTickDistance / 24.hours.toDouble(DurationUnit.MILLISECONDS)
        @min val tickDistanceInDays = possibleValues.first { it >= minTickDistanceInDays }

        return when {
          tickDistanceInDays >= 15 -> Every15Days
          tickDistanceInDays >= 10 -> Every10Days
          tickDistanceInDays >= 5 -> Every5Days
          else -> EveryDay
        }
      }
    }
  }

  override fun calculateTicksInternal(start: Double, end: Double, timeZone: TimeZone): DoubleArrayList {
    val startLocalDateExact = LocalDate.fromMillisCurrentTimeZone(start, timeZone)
    val endLocalDate = LocalDate.fromMillisCurrentTimeZone(end, timeZone).plusDays(ticksPerMonth.worstCaseDaysDistance())

    val startLocalDate = startLocalDateExact.withDay(
      ticksPerMonth.sameOrBelow(startLocalDateExact.dayOfMonth)
    )

    return DoubleArrayList().also { list ->
      ticksPerMonth.iterate(startLocalDate, endLocalDate) {
        list.add(it.toMillisAtStartOfDay(timeZone))
      }
    }
  }

  override fun formatAsOffset(millis: Double, i18nConfiguration: I18nConfiguration): String {
    return dateFormat.format(millis, i18nConfiguration)
  }

  override fun calculateEstimatedIndex(millis: @ms Double, timeZone: TimeZone): GlobalTimeIndex {
    val localDate = LocalDate.fromMillisCurrentTimeZone(millis, timeZone)
    val monthOfEpoche = localDate.monthOfEpoche()
    val dayOfMonth = localDate.dayOfMonth

    val tickOfEpoche = monthOfEpoche * ticksPerMonth.ticksPerMonth() + dayOfMonth.value / ticksPerMonth.typicalCaseDaysDistance()

    return GlobalTimeIndex(tickOfEpoche)
  }

  override fun smallestPossibleTickDistance(): TimeTickDistance {
    return DistanceMinutes(1)
  }

  override fun toString(): String {
    return "DistanceDays($ticksPerMonth)"
  }

  companion object {
    private val possibleValues = intArrayOf(1, 5, 10, 15)

    fun atLeast(minTickDistance: @ms Double): DistanceDays {
      return DistanceDays(TicksPerMonth.forMinDistance(minTickDistance))
    }
  }
}

/**
 * Distance in hours.
 * ATTENTION: There are timezones with 30 minute offset. Therefore, it is necessary to use [LocalDate] here, too.
 */
class DistanceHours(val distanceInHours: Int) : TimeTickDistance {
  override val timeMagnitude: TimeTickDistance.TimeMagnitude = TimeTickDistance.TimeMagnitude.Hours

  override fun typicalDistanceForComparison(): @ms Double {
    return distanceInHours * TimeConstants.millisPerHour
  }

  override fun calculateTicksInternal(start: @Inclusive @ms Double, end: @Inclusive @ms Double, timeZone: TimeZone): DoubleArrayList {
    val startLocalDateTimeExact = LocalDateTime.fromMillisCurrentTimeZone(start, timeZone)

    //The local hour of day
    val hoursOfDay = startLocalDateTimeExact.hour
    val startHour = hoursOfDay.roundDownToBase(distanceInHours)

    val startLocalDateTime = startLocalDateTimeExact.atStartOfHour(startHour)
    @ms val startMillis = startLocalDateTime.toMillis(timeZone)

    require(startMillis <= start) {
      "startMillis: ${startMillis.formatUtcForDebug()} must be <= ${start.formatUtcForDebug()}"
    }

    return DoubleArrayList().also { list ->
      @ms var current = startMillis
      while (current <= end) {
        list.add(current)
        current += distanceInHours * TimeConstants.millisPerHour

        require(list.size < 1000) {
          "List too large. Current ${current.formatUtcForDebug()}. End: ${end.formatUtcForDebug()}"
        }
      }
    }
  }

  override fun formatAsOffset(millis: Double, i18nConfiguration: I18nConfiguration): String {
    return dateTimeFormatShort.format(millis, i18nConfiguration)
  }

  override fun calculateEstimatedIndex(millis: Double, timeZone: TimeZone): GlobalTimeIndex {
    return GlobalTimeIndex((millis / TimeConstants.millisPerHour).toInt())
  }

  override fun smallestPossibleTickDistance(): TimeTickDistance {
    return DistanceMillis.smallest
  }

  override fun toString(): String {
    return "DistanceHours(hours=$distanceInHours)"
  }

  companion object {
    private val possibleValues = intArrayOf(1, 2, 3, 4, 6, 12)

    fun atLeast(minTickDistance: @ms Double): DistanceHours {
      @min val minTickDistanceInHours = minTickDistance / 1.hours.toDouble(DurationUnit.MILLISECONDS)
      @min val tickDistanceInHours = possibleValues.first { it >= minTickDistanceInHours }

      return DistanceHours(tickDistanceInHours)
    }
  }
}

/**
 * Distance in minutes
 */
class DistanceMinutes(val distanceInMinutes: @min Int) : TimeTickDistance {
  override val timeMagnitude: TimeTickDistance.TimeMagnitude = TimeTickDistance.TimeMagnitude.Minutes

  override fun typicalDistanceForComparison(): @ms Double {
    return distanceInMinutes * TimeConstants.millisPerMinute
  }

  override fun calculateTicksInternal(start: Double, end: Double, timeZone: TimeZone): DoubleArrayList {
    @min val startMinuteOfEpoch = (start / TimeConstants.millisPerMinute).toIntFloor().roundDownToBase(distanceInMinutes)
    @ms val startMillis = startMinuteOfEpoch * TimeConstants.millisPerMinute

    return DoubleArrayList().also { list ->
      @ms var current = startMillis
      while (current <= end) {
        list.add(current)
        current += distanceInMinutes * TimeConstants.millisPerMinute

        require(list.size < 1000) {
          "List too large. Current ${current.formatUtcForDebug()}. End: ${end.formatUtcForDebug()}"
        }
      }
    }
  }

  override fun formatAsOffset(millis: Double, i18nConfiguration: I18nConfiguration): String {
    return dateTimeFormatShort.format(millis, i18nConfiguration)
  }

  override fun calculateEstimatedIndex(millis: Double, timeZone: TimeZone): GlobalTimeIndex {
    @min val minuteOfEpoch = (millis / TimeConstants.millisPerMinute).toIntFloor()
    return GlobalTimeIndex(minuteOfEpoch / distanceInMinutes)
  }

  override fun smallestPossibleTickDistance(): TimeTickDistance {
    return DistanceMillis.smallest
  }

  override fun toString(): String {
    return "DistanceMinutes($distanceInMinutes)"
  }

  companion object {
    private val possibleValues: IntArray = intArrayOf(1, 2, 5, 10, 15, 30)

    fun atLeast(minTickDistance: @ms Double): DistanceMinutes {
      @min val minTickDistanceInMinutes = minTickDistance / 1.minutes.toDouble(DurationUnit.MILLISECONDS)
      @min val tickDistanceInMinutes = possibleValues.first { it >= minTickDistanceInMinutes }

      return DistanceMinutes(tickDistanceInMinutes)
    }
  }
}

/**
 * Distance in seconds
 */
class DistanceSeconds(val distanceInSeconds: @s Int) : TimeTickDistance {
  override val timeMagnitude: TimeTickDistance.TimeMagnitude = TimeTickDistance.TimeMagnitude.Seconds

  override fun typicalDistanceForComparison(): @ms Double {
    return distanceInSeconds * TimeConstants.millisPerSecond
  }

  override fun calculateTicksInternal(start: Double, end: Double, timeZone: TimeZone): DoubleArrayList {
    @s val startSecondOfEpoch = (start / TimeConstants.millisPerSecond).roundDownToBase(distanceInSeconds.toDouble())
    @ms val startMillis = startSecondOfEpoch * TimeConstants.millisPerSecond

    return DoubleArrayList().also { list ->
      @ms var current = startMillis
      while (current <= end) {
        list.add(current)
        current += distanceInSeconds * TimeConstants.millisPerSecond

        require(list.size < 1000) {
          "List too large. Current ${current.formatUtcForDebug()}. End: ${end.formatUtcForDebug()}"
        }
      }
    }
  }

  override fun formatAsOffset(millis: Double, i18nConfiguration: I18nConfiguration): String {
    return dateTimeFormat.format(millis, i18nConfiguration)
  }

  override fun calculateEstimatedIndex(millis: Double, timeZone: TimeZone): GlobalTimeIndex {
    @s val startSecondOfEpoch = (millis / TimeConstants.millisPerSecond) % Int.MAX_VALUE
    return GlobalTimeIndex(startSecondOfEpoch.toIntFloor() / distanceInSeconds)
  }

  override fun smallestPossibleTickDistance(): TimeTickDistance {
    return DistanceMillis.smallest
  }

  override fun toString(): String {
    return "DistanceSeconds($distanceInSeconds)"
  }

  companion object {
    private val possibleValues: IntArray = intArrayOf(1, 2, 5, 10, 15, 30, 60)

    fun atLeast(minTickDistance: @ms Double): DistanceSeconds {
      @s val minTickDistanceInSeconds = minTickDistance / 1.seconds.toDouble(DurationUnit.MILLISECONDS)
      @s val tickDistanceInSeconds = possibleValues.first { it >= minTickDistanceInSeconds }

      return DistanceSeconds(tickDistanceInSeconds)
    }
  }
}

/**
 * A time tick distance in millis
 */
class DistanceMillis(val distanceInMillis: @ms Double) : TimeTickDistance {
  init {
    require(distanceInMillis >= 0.0) { "distanceInMillis must be >= 0 but was <$distanceInMillis>" }
  }

  override val timeMagnitude: TimeTickDistance.TimeMagnitude = TimeTickDistance.TimeMagnitude.Millis

  override fun typicalDistanceForComparison(): @ms Double {
    return distanceInMillis
  }

  override fun calculateTicksInternal(start: Double, end: Double, timeZone: TimeZone): DoubleArrayList {
    @ms val startMillis: Double = if (distanceInMillis < 0.0) {
      start
    } else {
      start.roundDownToBase(distanceInMillis)
    }

    return DoubleArrayList().also { list ->
      @ms var current = startMillis
      while (current <= end) {
        list.add(current)
        current += distanceInMillis

        require(list.size < 1000) {
          "List too large. Current ${current.formatUtcForDebug()}. End: ${end.formatUtcForDebug()}"
        }
      }
    }
  }

  override fun formatAsOffset(millis: Double, i18nConfiguration: I18nConfiguration): String {
    return dateTimeFormatShortWithMillis.format(millis, i18nConfiguration)
  }

  override fun calculateEstimatedIndex(millis: Double, timeZone: TimeZone): GlobalTimeIndex {
    return GlobalTimeIndex(((millis / this.distanceInMillis) % Int.MAX_VALUE).toInt())
  }

  override fun smallestPossibleTickDistance(): TimeTickDistance {
    return smallest
  }

  override fun toString(): String {
    return "DistanceMillis($distanceInMillis)"
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

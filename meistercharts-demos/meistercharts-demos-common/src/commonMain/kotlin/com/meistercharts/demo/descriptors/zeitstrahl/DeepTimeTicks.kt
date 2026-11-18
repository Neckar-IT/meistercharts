/*
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
package com.meistercharts.demo.descriptors.zeitstrahl

import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min

/**
 * Ticks of the deep time axis from five billion years down to one hour.
 * Year steps count years before present in a window of [DeepTimeFormat.isDeepTimeWindow], else calendar years.
 * Months, days and hours carry no year in such a window.
 */
object DeepTimeTicks {
  private const val MinTickDistance: Double = 110.0

  private val steps: List<DeepTimeTickStep> = buildList {
    listOf(1, 3, 6, 12).forEach { add(DeepTimeTickStep.Hours(it)) }
    listOf(1, 2, 5, 10).forEach { add(DeepTimeTickStep.Days(it)) }
    listOf(1, 3, 6).forEach { add(DeepTimeTickStep.Months(it)) }
    var power = 1L
    repeat(10) {
      listOf(1L, 2L, 5L).forEach { add(DeepTimeTickStep.Years(it * power)) }
      power *= 10
    }
  }

  /** The finest step whose ticks lie at least [MinTickDistance] apart; a canvas under about 334 px gets the coarsest step even closer. */
  fun step(secondsPerPixel: Double): DeepTimeTickStep = steps.firstOrNull { it.approximateSeconds / secondsPerPixel >= MinTickDistance } ?: steps.last()

  fun ticks(from: Double, to: Double, centerSeconds: Double, step: DeepTimeTickStep): List<DeepTimeTick> {
    val deepTime = DeepTimeFormat.isDeepTimeWindow(centerSeconds, to - from)
    return when (step) {
      is DeepTimeTickStep.Years -> if (deepTime) yearsBeforePresentTicks(from, to, step.years) else calendarYearTicks(from, to, step.years)
      is DeepTimeTickStep.Months -> monthTicks(from, to, step.months, deepTime)
      is DeepTimeTickStep.Days -> dayTicks(from, to, step.days, deepTime)
      is DeepTimeTickStep.Hours -> hourTicks(from, to, step.hours, deepTime)
    }
  }

  /** The date of the window center in the precision of [step], shown above the axis; before 1950 a deep time window shows whole years before present at most. */
  fun centerLabel(centerSeconds: Double, windowSeconds: Double, step: DeepTimeTickStep): String {
    if (DeepTimeFormat.isDeepTimeWindow(centerSeconds, windowSeconds) && centerSeconds < DeepTimeFormat.secondsOfYearsBeforePresent(0.0)) {
      return DeepTimeFormat.yearsAgo(DeepTimeFormat.yearsBeforePresent(centerSeconds), step.approximateSeconds / ProlepticGregorianCalendar.SecondsPerYear)
    }
    val date = ProlepticGregorianCalendar.civilDate(centerSeconds)
    return when (step) {
      is DeepTimeTickStep.Years -> DeepTimeFormat.historicalYear(date.year)
      is DeepTimeTickStep.Months -> DeepTimeFormat.month(date)
      is DeepTimeTickStep.Days, is DeepTimeTickStep.Hours -> DeepTimeFormat.day(date)
    }
  }

  private fun yearsBeforePresentTicks(from: Double, to: Double, years: Long): List<DeepTimeTick> {
    val reference = DeepTimeFormat.secondsOfYearsBeforePresent(0.0)
    val stepSeconds = years * ProlepticGregorianCalendar.SecondsPerYear
    // No tick after 1950: the window may reach the present and half a window beyond.
    val firstMultiple = ceil((reference - to) / stepSeconds).toLong().coerceAtLeast(0)
    val lastMultiple = floor((reference - from) / stepSeconds).toLong()
    return (lastMultiple downTo firstMultiple).map { multiple ->
      val label = if (multiple == 0L) DeepTimeFormat.BeforePresentReferenceYear.toString() else DeepTimeFormat.yearsAgo((multiple * years).toDouble(), years.toDouble())
      DeepTimeTick(reference - multiple * stepSeconds, label)
    }
  }

  /** Round historical years on both sides of the missing year 0: "50 v. Chr.", "45 v. Chr." and "5", "10". */
  private fun calendarYearTicks(from: Double, to: Double, years: Long): List<DeepTimeTick> {
    val fromYear = ProlepticGregorianCalendar.civilDate(from).year
    val toYear = ProlepticGregorianCalendar.civilDate(to).year
    val yearsBeforeChrist = (ceilToMultiple(max(1 - min(toYear, 0L), 1L), years)..(1 - fromYear) step years).map { 1 - it }
    val yearsAfterChrist = (ceilToMultiple(max(fromYear, 1L), years)..toYear step years).toList()
    return (yearsBeforeChrist + yearsAfterChrist)
      .map { year -> DeepTimeTick(ProlepticGregorianCalendar.secondsOf(CivilDate(year, 1, 1)), DeepTimeFormat.historicalYear(year)) }
      .filter { it.seconds in from..to }
      .sortedBy { it.seconds }
  }

  private fun monthTicks(from: Double, to: Double, months: Int, deepTime: Boolean): List<DeepTimeTick> {
    val start = ProlepticGregorianCalendar.civilDate(from)
    val firstMonthIndex = ceilToMultiple(start.year * 12 + start.month - 1, months.toLong())
    return generateSequence(firstMonthIndex) { it + months }
      .map { monthIndex ->
        val date = CivilDate(monthIndex.floorDiv(12L), (monthIndex.mod(12L) + 1).toInt(), 1)
        val label = if (deepTime) DeepTimeFormat.monthName(date.month) else DeepTimeFormat.month(date)
        DeepTimeTick(ProlepticGregorianCalendar.secondsOf(date), label)
      }
      .dropWhile { it.seconds < from }
      .takeWhile { it.seconds <= to }
      .toList()
  }

  private fun dayTicks(from: Double, to: Double, days: Int, deepTime: Boolean): List<DeepTimeTick> {
    return (ProlepticGregorianCalendar.daysSinceEpoch(from)..ProlepticGregorianCalendar.daysSinceEpoch(to))
      .map { ProlepticGregorianCalendar.civilDateOfDay(it) }
      .filter { date -> date.isTickDay(days) }
      .map { date -> DeepTimeTick(ProlepticGregorianCalendar.secondsOf(date), dayLabel(date, deepTime)) }
      .filter { it.seconds in from..to }
  }

  /** Every [days]-th day of a month from the 1st; the last one of the month keeps a full step to the next 1st. */
  private fun CivilDate.isTickDay(days: Int): Boolean = (day - 1) % days == 0 && day + days - 1 <= ProlepticGregorianCalendar.daysInMonth(year, month)

  private fun hourTicks(from: Double, to: Double, hours: Int, deepTime: Boolean): List<DeepTimeTick> {
    val stepSeconds = hours * ProlepticGregorianCalendar.SecondsPerHour
    val first = ceil(from / stepSeconds).toLong()
    val last = floor(to / stepSeconds).toLong()
    return (first..last).map { multiple ->
      // Hour and day come from the Long hour index: near the Big Bang a Double second is only exact to 64 s.
      val hourIndex = multiple * hours
      val hour = hourIndex.mod(24L).toInt()
      val label = if (hour == 0) dayLabel(ProlepticGregorianCalendar.civilDateOfDay(hourIndex.floorDiv(24L)), deepTime) else "${hour.toString().padStart(2, '0')}:00"
      DeepTimeTick(multiple * stepSeconds, label)
    }
  }

  private fun dayLabel(date: CivilDate, deepTime: Boolean): String = if (deepTime) "${date.day}. ${DeepTimeFormat.monthName(date.month)}" else DeepTimeFormat.day(date)

  private fun ceilToMultiple(value: Long, step: Long): Long = -((-value).floorDiv(step)) * step
}

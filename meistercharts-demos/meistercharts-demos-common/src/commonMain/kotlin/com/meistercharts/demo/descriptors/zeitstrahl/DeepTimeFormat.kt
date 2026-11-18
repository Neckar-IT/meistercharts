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

import kotlin.math.abs
import kotlin.math.roundToLong

/**
 * German labels of the deep time axis: "vor 66 Mio. Jahren" in a window of [isDeepTimeWindow], else "44 v. Chr." and "14. Juli 1789".
 */
object DeepTimeFormat {
  /** Astronomical year of 10,000 BC; earlier years read as years before present. */
  private const val FirstHistoricalYear: Long = -9_999

  /** "Before present" counts from 1950, the convention of radiocarbon dating and the ICS chart. */
  const val BeforePresentReferenceYear: Long = 1_950

  private val monthNames: List<String> = listOf("Jan.", "Feb.", "März", "Apr.", "Mai", "Juni", "Juli", "Aug.", "Sep.", "Okt.", "Nov.", "Dez.")

  fun monthName(month: Int): String = monthNames[month - 1]

  /** "1789", "44 v. Chr." for astronomical year -43, "10.000 v. Chr."; a year of five digits and more is grouped on both sides */
  fun historicalYear(year: Long): String = if (year <= 0) "${yearNumber(1 - year)} v. Chr." else yearNumber(year)

  private fun yearNumber(year: Long): String = if (year >= 10_000) grouped(year) else year.toString()

  /**
   * "vor 13,8 Mrd. Jahren", "vor 66 Mio. Jahren", "vor 5.700.010 Jahren", "vor 12.000 Jahren".
   * Billions and millions with one decimal stand only where [resolutionYears] is coarse enough to tell two ticks apart.
   */
  fun yearsAgo(years: Double, resolutionYears: Double): String = when {
    years >= 1e9 && resolutionYears >= 1e8 -> "vor ${oneDecimal(years / 1e9)} Mrd. Jahren"
    years >= 1e6 && resolutionYears >= 1e5 -> "vor ${oneDecimal(years / 1e6)} Mio. Jahren"
    else -> "vor ${grouped(years.roundToLong())} Jahren"
  }

  /** A window centered before 10,000 BC or wider than 50,000 years counts years before present. */
  fun isDeepTimeWindow(centerSeconds: Double, windowSeconds: Double): Boolean {
    return ProlepticGregorianCalendar.civilDate(centerSeconds).year < FirstHistoricalYear || windowSeconds > 50_000 * ProlepticGregorianCalendar.SecondsPerYear
  }

  fun yearsBeforePresent(seconds: Double): Double = (beforePresentReferenceSeconds - seconds) / ProlepticGregorianCalendar.SecondsPerYear

  fun secondsOfYearsBeforePresent(years: Double): Double = beforePresentReferenceSeconds - years * ProlepticGregorianCalendar.SecondsPerYear

  private val beforePresentReferenceSeconds: Double = ProlepticGregorianCalendar.secondsOf(CivilDate(BeforePresentReferenceYear, 1, 1))

  /** "Juli 1789" */
  fun month(date: CivilDate): String = "${monthName(date.month)} ${historicalYear(date.year)}"

  /** "14. Juli 1789" */
  fun day(date: CivilDate): String = "${date.day}. ${monthName(date.month)} ${historicalYear(date.year)}"

  /** "3.190 Jahre", "45 Tage", "6 Stunden" */
  fun duration(seconds: Double): String = when {
    seconds >= 1e9 * ProlepticGregorianCalendar.SecondsPerYear -> "${oneDecimal(seconds / ProlepticGregorianCalendar.SecondsPerYear / 1e9)} Mrd. Jahre"
    seconds >= 1e6 * ProlepticGregorianCalendar.SecondsPerYear -> "${oneDecimal(seconds / ProlepticGregorianCalendar.SecondsPerYear / 1e6)} Mio. Jahre"
    seconds >= 2 * ProlepticGregorianCalendar.SecondsPerYear -> "${grouped((seconds / ProlepticGregorianCalendar.SecondsPerYear).roundToLong())} Jahre"
    seconds >= 2 * ProlepticGregorianCalendar.SecondsPerDay -> "${grouped((seconds / ProlepticGregorianCalendar.SecondsPerDay).roundToLong())} Tage"
    else -> "${(seconds / ProlepticGregorianCalendar.SecondsPerHour).roundToLong()} Stunden"
  }

  /** "66.000.000" */
  fun grouped(value: Long): String {
    val digits = abs(value).toString()
    val groups = digits.reversed().chunked(3).joinToString(".").reversed()
    return if (value < 0) "-$groups" else groups
  }

  /** "13,8", "66" */
  private fun oneDecimal(value: Double): String {
    val tenths = (value * 10).roundToLong()
    val fraction = tenths % 10
    return if (fraction == 0L) grouped(tenths / 10) else "${grouped(tenths / 10)},$fraction"
  }
}

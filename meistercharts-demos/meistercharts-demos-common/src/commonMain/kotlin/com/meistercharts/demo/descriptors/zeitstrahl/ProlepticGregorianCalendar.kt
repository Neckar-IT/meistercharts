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

import kotlin.math.floor

/**
 * Converts seconds since 1970-01-01T00:00:00Z to days of the proleptic Gregorian calendar and back, for every year since the Big Bang.
 * `kotlin.js.Date` ends at ±273,790 years; this arithmetic on `Long` days does not.
 */
object ProlepticGregorianCalendar {
  const val SecondsPerHour: Double = 3_600.0
  const val SecondsPerDay: Double = 86_400.0

  /** The mean Gregorian year. */
  const val SecondsPerYear: Double = 31_556_952.0

  private const val DaysPerEra: Long = 146_097
  private const val DaysFrom0000To1970: Long = 719_468

  fun daysSinceEpoch(seconds: Double): Long = floor(seconds / SecondsPerDay).toLong()

  fun civilDate(seconds: Double): CivilDate = civilDateOfDay(daysSinceEpoch(seconds))

  /** Days since 1970-01-01 of [date]; the algorithm `days_from_civil` by Howard Hinnant. */
  fun dayOf(date: CivilDate): Long {
    val year = if (date.month <= 2) date.year - 1 else date.year
    val era = year.floorDiv(400L)
    val yearOfEra = year - era * 400
    val monthFromMarch = (date.month + 9) % 12
    val dayOfYear = (153 * monthFromMarch + 2) / 5 + date.day - 1
    val dayOfEra = yearOfEra * 365 + yearOfEra / 4 - yearOfEra / 100 + dayOfYear
    return era * DaysPerEra + dayOfEra - DaysFrom0000To1970
  }

  fun daysInMonth(year: Long, month: Int): Int = when (month) {
    2 -> if (isLeapYear(year)) 29 else 28
    4, 6, 9, 11 -> 30
    else -> 31
  }

  fun isLeapYear(year: Long): Boolean = year.mod(4L) == 0L && (year.mod(100L) != 0L || year.mod(400L) == 0L)

  fun secondsOf(date: CivilDate): Double = dayOf(date) * SecondsPerDay

  /** The date of [day] since 1970-01-01; the algorithm `civil_from_days` by Howard Hinnant. */
  fun civilDateOfDay(day: Long): CivilDate {
    val shifted = day + DaysFrom0000To1970
    val era = shifted.floorDiv(DaysPerEra)
    val dayOfEra = shifted - era * DaysPerEra
    val yearOfEra = (dayOfEra - dayOfEra / 1460 + dayOfEra / 36524 - dayOfEra / 146096) / 365
    val dayOfYear = dayOfEra - (365 * yearOfEra + yearOfEra / 4 - yearOfEra / 100)
    val monthFromMarch = (5 * dayOfYear + 2) / 153
    val dayOfMonth = (dayOfYear - (153 * monthFromMarch + 2) / 5 + 1).toInt()
    val month = (if (monthFromMarch < 10) monthFromMarch + 3 else monthFromMarch - 9).toInt()
    val year = yearOfEra + era * 400
    return CivilDate(if (month <= 2) year + 1 else year, month, dayOfMonth)
  }
}

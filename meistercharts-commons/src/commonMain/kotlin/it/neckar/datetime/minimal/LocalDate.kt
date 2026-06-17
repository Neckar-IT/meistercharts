/*
 * Copyright (C) 2013-2026 Neckar IT GmbH, Mössingen, Germany
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Linking this library statically or dynamically with other modules is
 * making a combined work based on this library. Thus, the terms and
 * conditions of the GNU General Public License cover the whole combination.
 *
 * As a special exception, the copyright holders of this library give you
 * permission to link this library with independent modules, regardless of
 * the license terms of these independent modules, and to copy and distribute
 * the resulting combined work under terms of your choice, provided that every
 * copy of the combined work is accompanied by a complete copy of the source
 * code of this library.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package it.neckar.datetime.minimal

import it.neckar.datetime.minimal.io.LocalDateSerializer
import it.neckar.open.annotations.CrossLayer
import it.neckar.open.unit.si.ms
import kotlinx.serialization.Serializable

/**
 * Represents a local date.
 *
 * Attention: This is a very minimalistic implementation without any special checks.
 */
@CrossLayer("Calendar date — a single ISO yyyy-MM-dd scalar that lives identically in Domain, Mongo Entity, and on the REST wire, like NativeInstant and NativeUuid. Serializes as a primitive string, so embedding it in a DataView exposes no business internals.")
@Serializable(with = LocalDateSerializer::class)
data class LocalDate(
  val year: Year,
  val month: Month,
  val dayOfMonth: DayOfMonth,
) : Comparable<LocalDate?> {

  /**
   * Calculates the months since the start of the epoch
   */
  fun monthOfEpoche(): Int {
    return year.value * 12 + month.value
  }

  fun dayOfYear(): Int {
    return month.daysBeforeThisMonth(year) + dayOfMonth.value
  }

  fun format(): String {
    val formattedYear = year.value.toString()
    val formattedMonth = month.value.toString().padStart(2, '0')
    val formattedDay = dayOfMonth.value.toString().padStart(2, '0')

    return "$formattedYear-$formattedMonth-$formattedDay"
  }

  /**
   * Creates a new date at the start of the month
   */
  fun atStartOfMonth(): LocalDate {
    return atDayOfMonth(DayOfMonth.FirstDay)
  }

  fun atStartOfNextMonth(): LocalDate {
    return atStartOfMonth().plusMonths(1)
  }

  fun atStartOfYear(): LocalDate {
    return LocalDate(year, Month.January, DayOfMonth.FirstDay)
  }

  fun atStartOfNextYear(): LocalDate {
    return LocalDate(year + 1, Month.January, DayOfMonth.FirstDay)
  }

  /**
   * Creates a new instance with the new day of month
   */
  fun atDayOfMonth(newDayOfMonth: DayOfMonth): LocalDate {
    return LocalDate(year, month, newDayOfMonth)
  }

  fun withYear(year: Year): LocalDate {
    return LocalDate(year, month, dayOfMonth)
  }

  fun withMonth(month: Month): LocalDate {
    val updatedDayOfMonth = dayOfMonth.coerceDayOfMonth(year, month)
    return LocalDate(year, month, updatedDayOfMonth)
  }

  fun withDay(dayOfMonth: DayOfMonth): LocalDate {
    return LocalDate(year, month, dayOfMonth)
  }

  fun plusMonths(monthsToAdd: Int): LocalDate {
    if (monthsToAdd == 0) {
      return this
    }

    val newMonthValue = (month.value - 1 + monthsToAdd + 12) % 12 + 1
    val newYearValue = year.value + (month.value - 1 + monthsToAdd) / 12

    val newYear = Year(newYearValue)
    val newMonth = Month(newMonthValue)

    val newDayOfMonth = dayOfMonth.coerceDayOfMonth(newYear, newMonth)
    return LocalDate(newYear, newMonth, newDayOfMonth)
  }

  fun plusDays(daysToAdd: Int): LocalDate {
    require(daysToAdd in -10_000..10_000) {
      "Days to add ($daysToAdd) are out of range - performance too bad"
    }

    var newDayOfMonth = this.dayOfMonth.value + daysToAdd
    var newMonth = this.month
    var newYear = this.year

    while (newDayOfMonth > newMonth.daysInMonth(newYear).value) {
      newDayOfMonth -= newMonth.daysInMonth(newYear).value
      newMonth = Month((newMonth.value % 12) + 1)
      if (newMonth.value == 1) {
        newYear = Year(newYear.value + 1)
      }
    }

    while (newDayOfMonth < 1) {
      // Go back one month, then add that month's days to bring newDayOfMonth into range.
      newMonth = if (newMonth.value == 1) {
        newYear = Year(newYear.value - 1)
        Month(12)
      } else {
        Month(newMonth.value - 1)
      }
      newDayOfMonth += newMonth.daysInMonth(newYear).value
    }

    return LocalDate(newYear, newMonth, DayOfMonth(newDayOfMonth))
  }

  fun plusYears(yearsToAdd: Int): LocalDate {
    val newYear = year + yearsToAdd
    // Coerce day of month to handle Feb 29 in non-leap years (matches plusMonths semantics).
    return LocalDate(newYear, month, dayOfMonth.coerceDayOfMonth(newYear, month))
  }

  override fun compareTo(other: LocalDate?): Int {
    return when {
      other == null -> 1
      this.year.value != other.year.value -> this.year.value - other.year.value
      this.month.value != other.month.value -> this.month.value - other.month.value
      else -> this.dayOfMonth.value - other.dayOfMonth.value
    }
  }

  override fun toString(): String {
    return format()
  }

  companion object {
    operator fun invoke(
      year: Int,
      month: Int,
      dayOfMonth: Int,
    ): LocalDate {
      return LocalDate(Year(year), Month(month), DayOfMonth(dayOfMonth))
    }

    /**
     * Parses a date in the format YYYY-MM-DD
     */
    fun parse(decodeString: String): LocalDate {
      val parts = decodeString.split("-")
      require(parts.size == 3) {
        "Invalid date format: $decodeString"
      }
      val year = parts[0].toInt()
      val month = parts[1].toInt()
      val dayOfMonth = parts[2].toInt()
      return LocalDate(year, month, dayOfMonth)
    }
  }
}

/**
 * Calculates the UTC timestamp
 *
 * Attention: At the moment the time zone is ignored on JS
 */
expect fun LocalDate.toMillisAtStartOfDay(timeZone: TimeZone): @ms Double

/**
 * Creates a [LocalDate] from the provided milliseconds value.
 * Does only support the *current* time zone - due to limitations of the browsers.
 *
 * Provide the [expectedTimeZone] to avoid errors due to different time zones.
 */
expect fun LocalDate.Companion.fromMillisCurrentTimeZone(millis: @ms Double, expectedTimeZone: TimeZone): LocalDate

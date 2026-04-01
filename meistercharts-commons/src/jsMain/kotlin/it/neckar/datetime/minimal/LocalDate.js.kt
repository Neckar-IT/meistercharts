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

import it.neckar.open.unit.si.ms
import kotlin.js.Date

/**
 * Extracts the local date from the current time zone
 */
actual fun LocalDate.Companion.fromMillisCurrentTimeZone(millis: @ms Double, expectedTimeZone: TimeZone): LocalDate {
  verifySystemTimeZone(expectedTimeZone)
  return Date(millis).toLocalDate()
}

fun verifySystemTimeZone(expectedTimeZone: TimeZone) {
  //TODO implement me somehow
}

/**
 * Creates a new JS date using the local timezone
 */
fun LocalDate.toJsDateCurrentTimeZone(): Date {
  return Date(year.value, month.value - 1, dayOfMonth.value)
}

/**
 * Converts the date to a *local* [LocalDate]
 */
fun Date.toLocalDate(): LocalDate {
  return LocalDate(
    this.getFullYear(),
    this.getMonth() + 1,
    this.getDate(), //this is correct! getDay() returns the day of the week
  )
}

/**
 * Calculates the UTC timestamp
 * If no [timeZone] is provided, the system time zone is used.
 *
 *
 * Attention: At the moment the time zone is ignored on JS
 */
actual fun LocalDate.toMillisAtStartOfDay(timeZone: TimeZone): Double {
  return toJsDateCurrentTimeZone().getTime()
}

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

import it.neckar.open.time.millis2Instant
import it.neckar.open.time.toDoubleMillis
import it.neckar.open.unit.si.ms
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime

/**
 *
 */
actual fun it.neckar.datetime.minimal.LocalDate.Companion.fromMillisCurrentTimeZone(millis: @ms Double, expectedTimeZone: TimeZone): it.neckar.datetime.minimal.LocalDate {
  verifySystemTimeZone(expectedTimeZone)

  val instant = millis2Instant(millis)
  val zonedDateTime = instant.atZone(ZoneId.systemDefault())
  return zonedDateTime.toLocalDate().toNeckarItLocalDate()
}

/**
 * Converts a JVM [LocalDate] to a Neckar IT commons [it.neckar.datetime.minimal.LocalDate]
 */
fun java.time.LocalDate.toNeckarItLocalDate(): it.neckar.datetime.minimal.LocalDate {
  return LocalDate(this.year, this.monthValue, this.dayOfMonth)
}

fun it.neckar.datetime.minimal.LocalDate.toJava(): LocalDate {
  val dayOfMonthUnbound = dayOfMonth.value
  return LocalDate.of(year.value, month.value, 1).plusDays(dayOfMonthUnbound.toLong() - 1)
}

/**
 * Calculates the UTC timestamp at the start of the day
 */
actual fun it.neckar.datetime.minimal.LocalDate.toMillisAtStartOfDay(timeZone: TimeZone): Double {
  return toZonedDateTimeAtStartOfDay(timeZone).toDoubleMillis()
}

/**
 * Converts the local date to a zoned date time at start of day
 */
fun it.neckar.datetime.minimal.LocalDate.toZonedDateTimeAtStartOfDay(timeZone: TimeZone): ZonedDateTime {
  return toJava().atStartOfDay(timeZone.toZoneId())
}

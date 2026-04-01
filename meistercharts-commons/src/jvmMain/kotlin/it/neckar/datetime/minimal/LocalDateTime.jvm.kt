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
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit

actual fun LocalDateTime.Companion.fromMillisCurrentTimeZone(millis: @ms Double, expectedTimeZone: TimeZone): LocalDateTime {
  verifySystemTimeZone(expectedTimeZone)

  val instant = millis2Instant(millis)
  val zonedDateTime = instant.atZone(ZoneId.systemDefault())

  return LocalDateTime(
    date = zonedDateTime.toLocalDate().toNeckarItLocalDate(),
    time = zonedDateTime.toLocalTime().toNeckarItLocalTime()
  )
}

fun verifySystemTimeZone(expectedTimeZone: TimeZone) {
  require(ZoneId.systemDefault().id == expectedTimeZone.zoneId) {
    "System time zone [${ZoneId.systemDefault().id}] does not match expected time zone [${expectedTimeZone.zoneId}]"
  }
}


actual fun LocalDateTime.toMillis(timeZone: TimeZone): Double {
  val zonedDateTime = toZonedDateTime(timeZone)
  return zonedDateTime.toDoubleMillis()
}

/**
 * Converts the local date to a zoned date time at start of day
 */
fun it.neckar.datetime.minimal.LocalDateTime.toZonedDateTime(timeZone: TimeZone): ZonedDateTime {
  return toJava().atZone(timeZone.toZoneId())
}

fun it.neckar.datetime.minimal.LocalDateTime.toJava(): java.time.LocalDateTime {
  return java.time.LocalDateTime.of(
    year.value, month.value, dayOfMonth.value,
    hour, minute, second, TimeUnit.MILLISECONDS.toNanos(millis.toLong()).toInt()
  )
}

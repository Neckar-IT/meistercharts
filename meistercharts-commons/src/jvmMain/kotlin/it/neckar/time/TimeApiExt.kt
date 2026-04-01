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
package it.neckar.open.time

import it.neckar.open.unit.si.ms
import it.neckar.open.unit.si.ns
import it.neckar.open.unit.si.s
import java.time.Instant
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.chrono.ChronoZonedDateTime
import java.time.format.DateTimeFormatter
import kotlin.time.Duration

/**
 * Returns the double millis from the instant
 */
fun Instant.toDoubleMillis(): @ms Double {
  @s val secondsPart = epochSecond * 1_000.0
  @ns val nanosPart = nano.toLong().nanos2millis()

  return secondsPart + nanosPart
}

/**
 * Returns the double millis from a zoned date time
 */
fun ChronoZonedDateTime<*>.toDoubleMillis(): @ms Double {
  return toInstant().toDoubleMillis()
}

/**
 * Returns the milliseconds as long
 */
fun ChronoZonedDateTime<*>.toEpochMillis(): @ms Long {
  return this.toInstant().toEpochMilli()
}

fun millis2Instant(@ms millis: Double): Instant {
  @s val secondsPart = (millis / 1000.0).toLong()
  @ms val remainingMillis = millis - secondsPart * 1_000

  @s val nanosPart = remainingMillis.millis2nanos()
  return Instant.ofEpochSecond(secondsPart, nanosPart)
}

fun millisToUtc(millisInDouble: Double): OffsetDateTime {
  return millis2Instant(millisInDouble).toUtc()
}

fun millisToLocalDate(millisInDouble: Double): LocalDate {
  val instant = millis2Instant(millisInDouble)
  return instant.atZone(ZoneId.systemDefault()).toLocalDate()
}

/**
 * Converts the instant to the offset date time in UTC
 */
fun Instant.toUtc(): @ms OffsetDateTime {
  return OffsetDateTime.ofInstant(this, ZoneOffset.UTC)
}

/**
 * Returns the milliseconds for the entire offset date time
 */
fun OffsetDateTime.toMillis(): @ms Double {
  return this.toInstant().toDoubleMillis()
}

/**
 * Returns the milliseconds of the offset date time
 */
val OffsetDateTime.millis: Int
  get() {
    return this.nano / 1000000
  }


fun ZonedDateTime.toISOString(): String {
  return DateTimeFormatter.ISO_DATE_TIME.format(this)
}

/**
 * Calculates a new instant by adding the duration to the instant
 */
operator fun Instant.plus(duration: Duration): Instant {
  return this.plusMillis(duration.inWholeMilliseconds)
}

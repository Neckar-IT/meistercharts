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
import kotlin.time.Instant


/**
 * Converts this millis value to nanos (Long)
 */
fun Double.millis2nanos(): @ns Long {
  return (this * 1_000_000).toLong()
}

/**
 * Converts this nano value to millis (Double)
 */
fun Long.nanos2millis(): @ms Double {
  return this.toDouble().nanos2millis()
}

/**
 * Converts this nano value to millis (Double)
 */
fun Double.nanos2millis(): @ms Double {
  return this / 1_000_000.0
}

/**
 * Converts this double value (in milliseconds) to an Instant
 */
fun @ms Double.toInstant(): Instant {
  val epochSeconds = (this / 1000.0)
  val nanoAdjustment = ((this % 1000.0) * 1_000_000.0).toInt()

  return Instant.fromEpochSeconds(epochSeconds.toLong(), nanoAdjustment)
}

/**
 * Converts this instant to milliseconds (Double)
 */
fun Instant.toMillis(): @ms Double {
  val epochSeconds = this.epochSeconds
  val nanoAdjustment = this.nanosecondsOfSecond

  return epochSeconds.toDouble() * 1000.0 + (nanoAdjustment.toDouble() / 1_000_000.0)
}

/**
 * Formats this instant as a UTC string.
 * This method must only be used for debugging purposes.
 *
 * Use the `it.neckar.open.formatting.DateTimeFormatKt#formatUtc` method from the project `i18n` if possible.
 */
fun Instant.formatUtcForDebug(): String {
  return this.toEpochMilliseconds().toDouble().formatUtcForDebug()
}

/**
 * Formats a double value (in milliseconds) as UTC string.
 * This method must only be used for debugging purposes.
 *
 * Use the `it.neckar.open.formatting.DateTimeFormatKt#formatUtc` method from the project `i18n` if possible.
 */
expect fun @ms Double.formatUtcForDebug(): String

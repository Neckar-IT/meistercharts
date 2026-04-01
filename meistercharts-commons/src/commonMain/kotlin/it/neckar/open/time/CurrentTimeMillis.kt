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

import it.neckar.open.unit.number.IsFinite
import it.neckar.open.unit.si.ms
import kotlin.time.Instant

/**
 * Returns the current time in millis.
 *
 * Attention: When using the current time in painting operations, use `currentFrameTimestamp` instead
 *
 * @returns the number of milliseconds elapsed since January 1, 1970 00:00:00 UTC.
 */
fun nowMillis(): @ms @IsFinite Double {
  return nowProvider.nowMillis()
}

/**
 * Returns the current time as Instant
 */
fun now(): @ms @IsFinite Instant {
  return nowMillis().toInstant()
}

/**
 * Returns the current time in millis.
 */
fun nowMillisLong(): @ms @IsFinite Long {
  return nowMillis().toLong()
}

/**
 * The provider that is used to return now
 */
var nowProvider: NowProvider = ClockNowProvider

/**
 * Resets the now provider to [ClockNowProvider].
 * This method should be called to revert the changes after unit tests
 */
fun resetNowProvider() {
  nowProvider = ClockNowProvider
}

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
import kotlin.concurrent.Volatile
import kotlin.time.ComparableTimeMark
import kotlin.time.DurationUnit
import kotlin.time.TimeSource

/**
 * Source of every measured duration, deadline and interval; a point in time comes from [now].
 * A wall clock adjustment changes [now], not the elapsed time of a mark.
 * A test replaces it together with [nowProvider] by one [VirtualNowProvider].
 */
var monotonicTimeSource: TimeSource.WithComparableMarks = TimeSource.Monotonic

/**
 * Resets [monotonicTimeSource] to [TimeSource.Monotonic].
 */
fun resetMonotonicTimeSource() {
  monotonicTimeSource = TimeSource.Monotonic
}

/**
 * The milliseconds elapsed on [monotonicTimeSource] since the first call for that source; only the difference of two values is an elapsed time.
 * Code that subtracts times on every frame subtracts these values: [kotlin.time.Duration] arithmetic allocates on Kotlin/JS.
 * Replacing [monotonicTimeSource] restarts the values at 0.
 */
fun monotonicMillis(): @ms Double {
  val source = monotonicTimeSource
  val origin = monotonicOrigin?.takeIf { it.source === source } ?: MonotonicOrigin(source, source.markNow()).also { monotonicOrigin = it }
  return (source.markNow() - origin.mark).toDouble(DurationUnit.MILLISECONDS)
}

/**
 * The mark [monotonicMillis] counts from, together with the source it was taken from
 */
private class MonotonicOrigin(val source: TimeSource.WithComparableMarks, val mark: ComparableTimeMark)

@Volatile
private var monotonicOrigin: MonotonicOrigin? = null

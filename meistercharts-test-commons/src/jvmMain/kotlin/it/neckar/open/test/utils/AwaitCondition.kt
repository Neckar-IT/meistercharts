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
package it.neckar.open.test.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeSource

/**
 * Suspends until [condition] holds, which another thread or process brings about, and throws an [IllegalStateException]
 * naming [description] once [timeout] has passed. [condition] runs every [pollInterval].
 *
 * Waits in real time, also under `runTest` and a `VirtualNowProvider`: the poll runs on [Dispatchers.IO], and the deadline
 * reads `TimeSource.Monotonic`.
 */
suspend fun awaitCondition(timeout: Duration, description: () -> String, pollInterval: Duration = 10.milliseconds, condition: suspend () -> Boolean) {
  withContext(Dispatchers.IO) {
    // A virtual monotonicTimeSource would let the deadline pass at once or never.
    @Suppress("MonotonicTimeSourceViaProvider")
    val deadline = TimeSource.Monotonic.markNow() + timeout
    while (condition().not()) {
      check(deadline.hasNotPassedNow()) { "Timed out after $timeout waiting for ${description()}" }
      delay(pollInterval)
    }
  }
}

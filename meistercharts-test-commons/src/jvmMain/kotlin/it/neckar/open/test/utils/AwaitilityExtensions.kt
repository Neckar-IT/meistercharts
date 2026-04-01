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

import kotlinx.coroutines.*
import org.awaitility.core.ConditionFactory
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

/**
 */
@Deprecated("use untilTrue(). Do not use this in coroutines, use AtomicBoolean.waitUntilTrue() instead!", ReplaceWith("untilTrue(atomicBoolean)"))
fun ConditionFactory.untilAtomicIsTrue(atomicBoolean: AtomicBoolean) {
  return untilTrue(atomicBoolean)
}

fun ConditionFactory.atMostMillis(millis: Long): ConditionFactory {
  return atMost(millis, TimeUnit.MILLISECONDS)
}


/**
 * Delays the thread until the Atomic Boolean is true.
 */
suspend fun AtomicBoolean.waitUntilTrue(timeoutDuration: Duration = 10.seconds, delay: Duration = 10.milliseconds): Unit {
  this.waitUntil(true, timeoutDuration, delay)
}

/**
 * Delays the thread until the Atomic Boolean is false.
 */
suspend fun AtomicBoolean.waitUntilFalse(timeoutDuration: Duration = 10.seconds, delay: Duration = 10.milliseconds): Unit {
  this.waitUntil(false, timeoutDuration, delay)
}

/**
 * Waits until the expectedValue is achieved.
 */
suspend fun AtomicBoolean.waitUntil(expectedValue: Boolean, timeoutDuration: Duration = 10.seconds, delay: Duration = 10.milliseconds): Unit {
  withTimeout(timeoutDuration) {
    while (isActive && get() != expectedValue) {
      delay(delay)
    }
  }
}

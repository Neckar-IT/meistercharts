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

import it.neckar.open.dispose.Disposable
import kotlinx.coroutines.*
import kotlin.time.Duration

/**
 * Timer support that uses Coroutines
 */
class JVMTimerCoroutineImplementation(
  /**
   * The scope that is used to delay the callbacks
   */
  val delayScope: CoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob()),
  /**
   * The scope that is used to repeat the callbacks
   */
  val repeatScope: CoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob()),
) : TimerImplementation {

  override fun delay(delay: Duration, callback: () -> Unit): Disposable {
    val job = delayScope.launch {
      delay(delay)
      callback()
    }
    return Disposable { job.cancel("Disposed of delayScope") }
  }

  override fun repeat(delay: Duration, callback: () -> Unit): Disposable {
    require(delay.inWholeMilliseconds >= 1) { "delay must be at least 1 millisecond but was $delay" }

    val job = repeatScope.launch {
      while (isActive) {
        callback()
        delay(delay)
      }
    }
    return Disposable { job.cancel("Disposed of repeatScope") }
  }
}

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
package it.neckar.open.async

import it.neckar.open.dispose.Disposable
import it.neckar.open.dispose.OnDispose
import kotlin.time.Duration

/**
 * A class that supports the starting of timers which are automatically disposed
 * when [onDispose] has been disposed.
 */
class TimerSupport(private val onDispose: OnDispose) {

  /**
   * Used for [throttleLast]
   */
  private val async: Async = Async().also {
    onDispose.onDispose(it)
  }

  /**
   * Executes the given callback with a delay on the main thread.
   *
   * Returns a [Disposable] which may be used to cancel the timer
   * @return a disposable that can be used to cancel the timer.
   */
  fun delay(delay: Duration, callback: () -> Unit): Disposable {
    return it.neckar.open.time.delay(delay, callback).also {
      onDispose.onDispose(it)
    }
  }

  /**
   * Repeats the given lambda every [delay] on the main thread
   */
  fun repeat(delay: Duration, callback: () -> Unit): Disposable {
    return it.neckar.open.time.repeat(delay, callback).also {
      onDispose.onDispose(it)
    }
  }

  /**
   * Executes the given runnable - but only the last one for a key after each given window.
   * This method can be called multiple times but is only executed once per [window].
   *
   * The *last* scheduled runnable is executed within each time window.
   */
  fun throttleLast(window: Duration, key: Any, runnable: () -> Unit) {
    async.throttleLast(window, key, runnable)
  }
}

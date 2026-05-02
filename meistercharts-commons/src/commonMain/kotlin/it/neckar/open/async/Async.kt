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
import it.neckar.open.dispose.DisposeSupport
import it.neckar.open.time.delay
import kotlin.time.Duration

/**
 * Type-Alias for lambdas that can be executed
 */
typealias Runnable = () -> Unit


/**
 * This class can be used to for async method calls
 *
 */
class Async : Disposable {
  private val scheduledRunnables = HashMap<Any, Runnable>()

  /**
   * Executes the given runnable - but only the last one for a key after each given window.
   * This method can be called multiple times but is only executed once per [window].
   *
   * The *last* scheduled runnable is executed within each time window.
   */
  fun throttleLast(window: Duration, key: Any, runnable: Runnable) {
    if (scheduledRunnables.put(key, runnable) != null) {
      //There is another job scheduled, so we do not have to reschedule it
      return
    }

    //There has no other event been scheduled
    runDelayed(window) {
      getAndRemove(key)?.invoke()
    }.also {
      disposeSupport.onDispose(it)
    }
  }

  /**
   * Removes all runnables scheduled for execution with the given [key]
   */
  fun remove(key: Any) {
    scheduledRunnables -= key
  }

  /**
   * Returns the runnable for the given key
   */
  private fun getAndRemove(key: Any): Runnable? {
    return scheduledRunnables.remove(key)
  }

  /**
   * Run in target thread
   */
  private fun runDelayed(delay: Duration, runnable: Runnable): Disposable {
    return delay(delay) {
      runnable()
    }
  }

  private val disposeSupport = DisposeSupport()

  override fun dispose() {
    scheduledRunnables.clear()
    disposeSupport.dispose()
  }
}

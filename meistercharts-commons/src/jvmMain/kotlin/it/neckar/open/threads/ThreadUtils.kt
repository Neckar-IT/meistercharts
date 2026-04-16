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
package it.neckar.open.threads

import it.neckar.open.annotations.Blocking
import java.util.concurrent.Callable
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executors
import javax.annotation.Nonnull
import javax.swing.SwingUtilities

/**
 */
fun isEventDispatchThread(): Boolean {
  return SwingUtilities.isEventDispatchThread()
}


/**
 *
 * assertEventDispatchThread
 *
 * @throws IllegalThreadStateException if any.
 */

@Throws(IllegalThreadStateException::class)
fun assertEventDispatchThread() {
  if (!isEventDispatchThread()) {
    throw IllegalThreadStateException("Not in EDT")
  }
}

/**
 *
 * assertNotEventDispatchThread
 *
 * @throws IllegalThreadStateException if any.
 */

@Throws(IllegalThreadStateException::class)
fun assertNotEventDispatchThread() {
  if (isEventDispatchThread()) {
    throw IllegalThreadStateException("Is EDT")
  }
}

/**
 *
 * inokeInOtherThread
 *
 * @param callable a Callable object.
 * @return a T object.
 *
 * @throws ExecutionException   if any.
 * @throws InterruptedException if any.
 */

/**
 * Returns the result of [callable], or `null` if [callable] is null. Callers that use this
 * helper only for the side effect (typical in tests that place assertions inside the
 * callable) may ignore the return value.
 */
@IgnorableReturnValue
@Throws(ExecutionException::class, InterruptedException::class)
fun <T> invokeInOtherThread(@Nonnull callable: Callable<T>?): T? {
  val executor = Executors.newSingleThreadExecutor()
  return try {
    val future = executor.submit(callable)
    future.get()
  } finally {
    executor.shutdown()
  }
}

/**
 * Invokes the runnable within the EDT
 *
 * @param runnable a Runnable object.
 */

@Blocking
fun invokeInEventDispatchThread(@Nonnull runnable: Runnable) {
  if (isEventDispatchThread()) {
    runnable.run()
  } else {
    SwingUtilities.invokeAndWait(runnable)
  }
}


fun waitForEventDispatchThread() {
  invokeInEventDispatchThread { }
}

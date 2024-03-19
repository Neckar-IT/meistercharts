/**
 * Copyright (C) cedarsoft GmbH.
 *
 * Licensed under the GNU General Public License version 3 (the "License")
 * with Classpath Exception; you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.cedarsoft.org/gpl3ce
 * (GPL 3 with Classpath Exception)
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 3 only, as
 * published by the Free Software Foundation. cedarsoft GmbH designates this
 * particular file as subject to the "Classpath" exception as provided
 * by cedarsoft GmbH in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 3 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 3 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact cedarsoft GmbH, 72810 Gomaringen, Germany,
 * or visit www.cedarsoft.com if you need additional information or
 * have any questions.
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

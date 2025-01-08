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
package it.neckar.open.test.utils

import assertk.*
import assertk.assertions.*
import java.awt.EventQueue
import java.lang.ref.WeakReference
import java.lang.reflect.InvocationTargetException

/**
 * A simple utility class that can verify that an object has been successfully garbage collected.
 */
class SwingMemoryLeakVerifier<T> {
  val reference: WeakReference<T>

  constructor(objectUnderTest: T) {
    reference = WeakReference(objectUnderTest)
  }

  constructor(reference: WeakReference<T>) {
    this.reference = reference
  }

  val objectUnderTest: Any?
    get() = reference.get()

  /**
   * Attempts to perform a full garbage collection so that all weak references will be removed. Usually only
   * a single GC is required, but there have been situations where some unused memory is not cleared up on the
   * first pass. This method performs a full garbage collection and then validates that the weak reference
   * now has been cleared. If it hasn't then the thread will sleep for 50 milliseconds and then retry up to
   * 10 more times. If after this the object still has not been collected then the assertion will fail.
   *
   *
   * Based upon the method described in: http://www.javaworld.com/javaworld/javatips/jw-javatip130.html
   */
  fun assertGarbageCollected() {
    val runtime = Runtime.getRuntime()
    for (ignored in 0 until MAX_GC_ITERATIONS) {
      runtime.runFinalization()
      runtime.gc()
      if (objectUnderTest == null) {
        break
      }

      // Pause for a while and then go back around the loop to try again...
      try {
        EventQueue.invokeAndWait(NoOp()) // Wait for the AWT event queue to have completed processing
        Thread.sleep(GC_SLEEP_TIME.toLong())
      } catch (_: InterruptedException) {
        // Ignore any interrupts and just try again...
      } catch (_: InvocationTargetException) {
      }
    }
    assertThat(objectUnderTest, "object should not exist after $MAX_GC_ITERATIONS collections").isNull()
  }

  private class NoOp : Runnable {
    override fun run() {}
  }

  companion object {
    private const val MAX_GC_ITERATIONS = 50
    private const val GC_SLEEP_TIME = 100
  }
}

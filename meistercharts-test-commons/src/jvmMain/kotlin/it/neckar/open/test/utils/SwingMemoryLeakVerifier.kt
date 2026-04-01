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

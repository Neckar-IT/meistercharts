/**
 * Copyright 2023 Neckar IT GmbH, Mössingen, Germany
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.meistercharts.fx.debug

import it.neckar.open.annotations.Blocking
import com.meistercharts.fx.MeisterChartFX
import it.neckar.open.kotlin.lang.fastFor
import it.neckar.open.javafx.JavaFxTimer
import it.neckar.open.dispose.Disposable
import it.neckar.open.memory.HeapDumpSupport
import it.neckar.open.unit.si.ms
import javafx.application.Platform
import javafx.scene.control.Alert
import java.lang.ref.WeakReference
import java.lang.reflect.InvocationTargetException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

/**
 * A utility class that checks for memory leaks
 */
object MeisterChartsMemoryLeakDetector : Disposable {
  /**
   * The weak references that have to be checked
   */
  private val refsToCheck = mutableListOf<WeakReference<MeisterChartFX>>()

  /**
   * Monitors the given chart object
   */
  fun monitor(meisterChart: MeisterChartFX) {
    //Register a checker that is invoked *after* the chart object has been disposed
    meisterChart.onDispose {
      scheduleCheck(meisterChart)
    }
  }

  private fun scheduleCheck(meisterChart: MeisterChartFX) {
    refsToCheck.add(WeakReference(meisterChart))
  }

  private val scheduledExecutor: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()

  init {
    scheduledExecutor.scheduleWithFixedDelay(
      {
        try {
          val ref = refsToCheck.removeFirstOrNull() ?: return@scheduleWithFixedDelay

          val memoryLeakVerifier = MemoryLeakVerifier(ref)
          memoryLeakVerifier.assertGarbageCollected {
            System.err.println("---------------------------------------------------------------")
            System.err.println("------------------- MEMORY LEAK DETECTED ----------------------")
            System.err.println("------------------- $it - (${it.description})")
            System.err.println("---------------------------------------------------------------")

            val dumpFileName = "/tmp/${LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME)}.hprof"

            Platform.runLater {
              Alert(Alert.AlertType.ERROR).apply {
                title = "Memory Leak"
                headerText = "Memory Leak detected"
                contentText = "A memory leak has been detected.\nCreating heap dump: $dumpFileName"
              }.show()
            }

            System.err.println("Creating heap dump:")
            System.err.println("\t${dumpFileName}")

            HeapDumpSupport.dumpHeap(dumpFileName)
          }
        } catch (e: Exception) {
          Thread.getDefaultUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), e)
        }
      }, 5000, 5000, TimeUnit.MILLISECONDS
    )
  }

  override fun dispose() {
    scheduledExecutor.shutdown()
  }
}

/**
 * Checks whether a memory leak has been detected
 */
class MemoryLeakVerifier<T> {
  val reference: WeakReference<T>

  constructor(objectUnderTest: T) {
    reference = WeakReference(objectUnderTest)
  }

  constructor(reference: WeakReference<T>) {
    this.reference = reference
  }

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
  @Blocking
  fun assertGarbageCollected(
    /**
     * The failure action is executed if the garbage collection assertion fails.
     * Can be used to create a heap dump
     */
    failureAction: (referencedObject: T) -> Unit = { referencedObject ->
      throw IllegalStateException("object <$referencedObject> should not exist after $MaxGcIterations collections")
    }
  ) {
    val runtime = Runtime.getRuntime()
    MaxGcIterations.fastFor {
      runtime.runFinalization()
      runtime.gc()
      if (reference.get() == null) {
        return
      }

      // Pause for a while and then go back around the loop to try again...
      try {
        JavaFxTimer.runAndWait {
          //Wait until the UiThread has been completed
        }

        Thread.sleep(GcSleepTime)
      } catch (ignore: InterruptedException) {
        // Ignore any interrupts and just try again...
      } catch (ignore: InvocationTargetException) {
      }
    }

    reference.get()?.let {
      failureAction(it)
    }
  }

  companion object {
    private const val MaxGcIterations = 50

    @ms
    private const val GcSleepTime = 100L
  }
}

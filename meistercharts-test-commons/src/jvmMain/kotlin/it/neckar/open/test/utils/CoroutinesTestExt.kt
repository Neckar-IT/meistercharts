package it.neckar.open.test.utils

import kotlinx.coroutines.*
import kotlinx.coroutines.debug.*
import java.io.PrintStream
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 * Prints the coroutines stack traces every [delay] milliseconds.
 */
fun dumpCoroutinesStacktrace(out: PrintStream = System.out, delay: Duration = 2000.milliseconds): Job {
  DebugProbes.install()
  return CoroutineScope(Dispatchers.Default).launch {
    while (isActive) {
      delay(delay)
      DebugProbes.dumpCoroutines(out)
    }
  }
}

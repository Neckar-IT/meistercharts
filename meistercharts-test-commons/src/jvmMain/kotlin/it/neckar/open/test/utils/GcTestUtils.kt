package it.neckar.open.test.utils

import it.neckar.open.kotlin.lang.fastFor
import kotlin.system.measureTimeMillis

/**
 * Forces gc until the action returns true.
 * Throws an exception if GC has never been successful
 */
fun forceGc(gcSuccessful: (() -> Boolean)) {
  measureTimeMillis {
    20.fastFor {
      System.gc()

      if (gcSuccessful.invoke()) {
        return
      }
    }
  }.also { println("Took $it ms") }

  throw IllegalStateException("GC not successful")
}


/**
 * Executes the GC
 */
fun gc() {
  measureTimeMillis {
    20.fastFor {
      System.gc()
    }
  }.also { println("Took $it ms") }
}

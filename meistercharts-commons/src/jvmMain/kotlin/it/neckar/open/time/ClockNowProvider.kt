package it.neckar.open.time

import it.neckar.open.unit.number.IsFinite
import it.neckar.open.unit.si.ms

/**
 * JVM specific implementation for the clock now provider
 */
actual object ClockNowProvider : NowProvider {
  actual override fun nowMillis(): @ms @IsFinite Double {
    return System.currentTimeMillis().toDouble()
  }
}

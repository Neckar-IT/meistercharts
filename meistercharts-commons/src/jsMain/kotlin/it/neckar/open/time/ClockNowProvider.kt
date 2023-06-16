package it.neckar.open.time

import it.neckar.open.unit.number.IsFinite
import it.neckar.open.unit.si.ms
import kotlin.js.Date

/**
 * Provides now using the clock
 */
actual object ClockNowProvider : NowProvider {
  actual override fun nowMillis(): @ms @IsFinite Double {
    return Date.now()
  }
}

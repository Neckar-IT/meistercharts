package it.neckar.open.time

import kotlin.js.Date

/**
 * Provides now using the clock
 */
actual object ClockNowProvider : NowProvider {
  actual override fun nowMillis(): Double {
    return Date.now()
  }
}

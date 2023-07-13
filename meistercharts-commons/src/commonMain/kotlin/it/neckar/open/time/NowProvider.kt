package it.neckar.open.time

import it.neckar.open.unit.number.IsFinite
import it.neckar.open.unit.si.ms

/**
 * Provider that returns now()
 */
interface NowProvider {
  /**
   * Provides the current time in millis
   */
  fun nowMillis(): @ms @IsFinite Double
}

/**
 * Provides now using the clock.
 * This is the default implementation for [NowProvider] that should be used in most cases
 */
expect object ClockNowProvider : NowProvider {
  override fun nowMillis(): @ms @IsFinite Double
}


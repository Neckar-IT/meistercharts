package it.neckar.open.time

import it.neckar.open.unit.si.ms
import it.neckar.open.unit.si.ns

/**
 * Returns the current time in millis.
 *
 * Attention: When using the current time in painting operations, use `currentFrameTimestamp` instead
 *
 * @returns the number of milliseconds elapsed since January 1, 1970 00:00:00 UTC.
 */
fun nowMillis(): @ms Double {
  return nowProvider.nowMillis()
}

/**
 * The provider that is used to return now
 */
var nowProvider: NowProvider = ClockNowProvider

/**
 * Resets the now provider to [ClockNowProvider].
 * This method should be called to revert the changes after unit tests
 */
fun resetNowProvider() {
  nowProvider = ClockNowProvider
}

/**
 * Converts this millis value to nanos (Long)
 */
@ns
fun Double.millis2nanos(): Long {
  return (this * 1_000_000).toLong()
}

/**
 * Converts this nano value to millis (Double)
 */
@ms
fun Long.nanos2millis(): Double {
  return this / 1_000_000.0
}


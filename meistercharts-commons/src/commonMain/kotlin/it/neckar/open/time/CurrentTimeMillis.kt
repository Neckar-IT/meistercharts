package it.neckar.open.time

import it.neckar.open.unit.number.IsFinite
import it.neckar.open.unit.si.ms

/**
 * Returns the current time in millis.
 *
 * Attention: When using the current time in painting operations, use `currentFrameTimestamp` instead
 *
 * @returns the number of milliseconds elapsed since January 1, 1970 00:00:00 UTC.
 */
fun nowMillis(): @ms @IsFinite Double {
  return nowProvider.nowMillis()
}

/**
 * Returns the current time in millis.
 */
fun nowMillisLong(): @ms @IsFinite Long {
  return nowMillis().toLong()
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

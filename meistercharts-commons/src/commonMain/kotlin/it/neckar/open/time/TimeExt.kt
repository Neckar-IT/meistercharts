package it.neckar.open.time

import it.neckar.open.unit.si.ms
import it.neckar.open.unit.si.ns
import kotlin.time.Instant


/**
 * Converts this millis value to nanos (Long)
 */
fun Double.millis2nanos(): @ns Long {
  return (this * 1_000_000).toLong()
}

/**
 * Converts this nano value to millis (Double)
 */
fun Long.nanos2millis(): @ms Double {
  return this.toDouble().nanos2millis()
}

/**
 * Converts this nano value to millis (Double)
 */
fun Double.nanos2millis(): @ms Double {
  return this / 1_000_000.0
}

/**
 * Converts this double value (in milliseconds) to an Instant
 */
fun @ms Double.toInstant(): Instant {
  val epochSeconds = (this / 1000.0)
  val nanoAdjustment = ((this % 1000.0) * 1_000_000.0).toInt()

  return Instant.fromEpochSeconds(epochSeconds.toLong(), nanoAdjustment)
}

/**
 * Converts this instant to milliseconds (Double)
 */
fun Instant.toMillis(): @ms Double {
  val epochSeconds = this.epochSeconds
  val nanoAdjustment = this.nanosecondsOfSecond

  return epochSeconds.toDouble() * 1000.0 + (nanoAdjustment.toDouble() / 1_000_000.0)
}

/**
 * Formats this instant as a UTC string.
 * This method must only be used for debugging purposes.
 *
 * Use the `it.neckar.open.formatting.DateTimeFormatKt#formatUtc` method from the project `i18n` if possible.
 */
fun Instant.formatUtcForDebug(): String {
  return this.toEpochMilliseconds().toDouble().formatUtcForDebug()
}

/**
 * Formats a double value (in milliseconds) as UTC string.
 * This method must only be used for debugging purposes.
 *
 * Use the `it.neckar.open.formatting.DateTimeFormatKt#formatUtc` method from the project `i18n` if possible.
 */
expect fun @ms Double.formatUtcForDebug(): String

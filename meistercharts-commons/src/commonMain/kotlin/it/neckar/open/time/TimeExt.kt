package it.neckar.open.time

import it.neckar.open.unit.si.ms
import it.neckar.open.unit.si.ns


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
 * Formats a double value (in milliseconds) as UTC string.
 * This method must only be used for debugging purposes.
 *
 * Use the `it.neckar.open.formatting.DateTimeFormatKt#formatUtc` method from the project `i18n` if possible.
 */
expect fun @ms Double.formatUtcForDebug(): String

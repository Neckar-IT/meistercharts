package it.neckar.open.time

import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.util.Date


/**
 * Formats a double value (in milliseconds) as UTC string.
 * This method must only be used for debugging purposes.
 */
actual fun Double.formatUtcForDebug(): String {
  if (this.isNaN()) {
    return "NaN"
  }

  if (this.isInfinite()) {
    return "∞"
  }

  return try {
    Instant.ofEpochMilli(this.toLong()).atOffset(ZoneOffset.UTC).format(utcDateTimeFormat)
  } catch (e: Throwable) {
    "---${this}---[$e]"
  }
}

/**
 * Special date format that does *not* append the time zone - to be compatible with JS - adds a 'Z' at the end
 */
val utcDateTimeFormat: DateTimeFormatter = DateTimeFormatterBuilder()
  .append(DateTimeFormatter.ISO_LOCAL_DATE)
  .appendLiteral('T')
  .appendPattern("HH")
  .appendLiteral(":")
  .appendPattern("mm")
  .appendLiteral(":")
  .appendPattern("ss")
  .appendLiteral(".")
  .appendPattern("SSS")
  .appendLiteral('Z')
  .toFormatter()


/**
 * Converts an instant to a java.util.Date.
 *
 * Use only if (really, really) necessary.
 */
fun Instant.toDate(): Date {
  return Date(this.toEpochMilli())
}

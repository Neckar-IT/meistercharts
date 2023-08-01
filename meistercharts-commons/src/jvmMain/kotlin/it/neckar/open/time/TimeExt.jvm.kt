package it.neckar.open.time

import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder


/**
 * Formats a double value (in milliseconds) as UTC string.
 * This method must only be used for debugging purposes.
 */
actual fun Double.formatUtcForDebug(): String {
  return Instant.ofEpochMilli(this.toLong()).atOffset(ZoneOffset.UTC).format(utcDateTimeFormat)
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

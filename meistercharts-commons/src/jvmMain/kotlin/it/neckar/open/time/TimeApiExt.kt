package it.neckar.open.time

import it.neckar.open.unit.si.ms
import it.neckar.open.unit.si.ns
import it.neckar.open.unit.si.s
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.chrono.ChronoZonedDateTime
import java.time.format.DateTimeFormatter

/**
 * Returns the double millis from the instant
 */
fun Instant.toDoubleMillis(): @ms Double {
  @s val secondsPart = epochSecond * 1_000.0
  @ns val nanosPart = nano.toLong().nanos2millis()

  return secondsPart + nanosPart
}

/**
 * Returns the double millis from a zoned date time
 */
fun ChronoZonedDateTime<*>.toDoubleMillis(): @ms Double {
  return toInstant().toDoubleMillis()
}

/**
 * Returns the milliseconds as long
 */
fun ChronoZonedDateTime<*>.toEpochMillis(): @ms Long {
  return this.toInstant().toEpochMilli()
}

fun millis2Instant(@ms millis: Double): Instant {
  @s val secondsPart = (millis / 1000.0).toLong()
  @ms val remainingMillis = millis - secondsPart * 1_000

  @s val nanosPart = remainingMillis.millis2nanos()
  return Instant.ofEpochSecond(secondsPart, nanosPart)
}

fun millisToUtc(millisInDouble: Double): OffsetDateTime {
  return millis2Instant(millisInDouble).toUtc()
}

/**
 * Converts the instant to the offset date time in UTC
 */
fun Instant.toUtc(): @ms OffsetDateTime {
  return OffsetDateTime.ofInstant(this, ZoneOffset.UTC)
}

/**
 * Returns the milliseconds for the entire offset date time
 */
fun OffsetDateTime.toMillis(): @ms Double {
  return this.toInstant().toDoubleMillis()
}

/**
 * Returns the milliseconds of the offset date time
 */
val OffsetDateTime.millis: Int
  get() {
    return this.nano / 1000000
  }


fun ZonedDateTime.toISOString(): String {
  return DateTimeFormatter.ISO_DATE_TIME.format(this)
}

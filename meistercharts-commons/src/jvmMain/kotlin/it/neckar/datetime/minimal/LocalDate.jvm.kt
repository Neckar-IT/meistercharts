package it.neckar.datetime.minimal

import it.neckar.open.time.millis2Instant
import it.neckar.open.time.toDoubleMillis
import it.neckar.open.unit.si.ms
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime

/**
 *
 */
actual fun it.neckar.datetime.minimal.LocalDate.Companion.fromMillisCurrentTimeZone(millis: @ms Double, expectedTimeZone: TimeZone): it.neckar.datetime.minimal.LocalDate {
  verifySystemTimeZone(expectedTimeZone)

  val instant = millis2Instant(millis)
  val zonedDateTime = instant.atZone(ZoneId.systemDefault())
  return zonedDateTime.toLocalDate().toNeckarItLocalDate()
}

/**
 * Converts a JVM [LocalDate] to a Neckar IT commons [it.neckar.datetime.minimal.LocalDate]
 */
fun java.time.LocalDate.toNeckarItLocalDate(): it.neckar.datetime.minimal.LocalDate {
  return LocalDate(this.year, this.monthValue, this.dayOfMonth)
}

fun it.neckar.datetime.minimal.LocalDate.toJava(): LocalDate {
  val dayOfMonthUnbound = dayOfMonth.value
  return LocalDate.of(year.value, month.value, 1).plusDays(dayOfMonthUnbound.toLong() - 1)
}

/**
 * Calculates the UTC timestamp at the start of the day
 */
actual fun it.neckar.datetime.minimal.LocalDate.toMillisAtStartOfDay(timeZone: TimeZone): Double {
  return toZonedDateTimeAtStartOfDay(timeZone).toDoubleMillis()
}

/**
 * Converts the local date to a zoned date time at start of day
 */
fun it.neckar.datetime.minimal.LocalDate.toZonedDateTimeAtStartOfDay(timeZone: TimeZone): ZonedDateTime {
  return toJava().atStartOfDay(timeZone.toZoneId())
}

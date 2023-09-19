package it.neckar.datetime.minimal

import it.neckar.open.time.millis2Instant
import it.neckar.open.time.toDoubleMillis
import it.neckar.open.unit.si.ms
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit

actual fun LocalDateTime.Companion.fromMillisCurrentTimeZone(millis: @ms Double, expectedTimeZone: TimeZone): LocalDateTime {
  verifySystemTimeZone(expectedTimeZone)

  val instant = millis2Instant(millis)
  val zonedDateTime = instant.atZone(ZoneId.systemDefault())

  return LocalDateTime(
    date = zonedDateTime.toLocalDate().toNeckarItLocalDate(),
    time = zonedDateTime.toLocalTime().toNeckarItLocalTime()
  )
}

fun verifySystemTimeZone(expectedTimeZone: TimeZone) {
  require(ZoneId.systemDefault().id == expectedTimeZone.zoneId) {
    "System time zone [${ZoneId.systemDefault().id}] does not match expected time zone [${expectedTimeZone.zoneId}]"
  }
}


actual fun LocalDateTime.toMillis(timeZone: TimeZone): Double {
  val zonedDateTime = toZonedDateTime(timeZone)
  return zonedDateTime.toDoubleMillis()
}

/**
 * Converts the local date to a zoned date time at start of day
 */
fun it.neckar.datetime.minimal.LocalDateTime.toZonedDateTime(timeZone: TimeZone): ZonedDateTime {
  return toJava().atZone(timeZone.toZoneId())
}

fun it.neckar.datetime.minimal.LocalDateTime.toJava(): java.time.LocalDateTime {
  return java.time.LocalDateTime.of(
    year.value, month.value, dayOfMonth.value,
    hour, minute, second, TimeUnit.MILLISECONDS.toNanos(millis.toLong()).toInt()
  )
}

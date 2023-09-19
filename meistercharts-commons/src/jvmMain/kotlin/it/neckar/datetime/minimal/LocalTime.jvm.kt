package it.neckar.datetime.minimal

import it.neckar.open.time.millis2Instant
import it.neckar.open.unit.si.ms
import java.time.ZoneId

actual fun LocalTime.Companion.fromMillisCurrentTimeZone(millis: @ms Double, expectedTimeZone: TimeZone): LocalTime {
  verifySystemTimeZone(expectedTimeZone)
  val instant = millis2Instant(millis)
  val zonedDateTime = instant.atZone(ZoneId.systemDefault())
  return zonedDateTime.toLocalTime().toNeckarItLocalTime()

}

/**
 * Converts a JVM [LocalTime] to a Neckar IT commons [it.neckar.datetime.minimal.LocalTime]
 */
fun java.time.LocalTime.toNeckarItLocalTime(): it.neckar.datetime.minimal.LocalTime {
  return LocalTime(this.hour, this.minute, this.second, (this.nano / 1_000_000.0).toInt())
}

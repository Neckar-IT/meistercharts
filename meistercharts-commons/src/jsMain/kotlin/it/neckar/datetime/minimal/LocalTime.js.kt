package it.neckar.datetime.minimal

import it.neckar.open.unit.si.ms
import kotlin.js.Date

actual fun LocalTime.Companion.fromMillisCurrentTimeZone(millis: @ms Double, expectedTimeZone: TimeZone): LocalTime {
  verifySystemTimeZone(expectedTimeZone)
  return Date(millis).toLocalTime()
}

/**
 * Converts the date to a *local* [LocalTime]
 */
fun Date.toLocalTime(): LocalTime {
  return LocalTime(
    hour = this.getHours(),
    minute = this.getMinutes(),
    second = this.getSeconds(),
    millis = this.getMilliseconds(),
  )
}

package it.neckar.datetime.minimal

import it.neckar.open.unit.si.ms
import kotlin.js.Date

actual fun LocalDateTime.Companion.fromMillisCurrentTimeZone(millis: @ms Double, expectedTimeZone: TimeZone): LocalDateTime {
  val date = Date(millis)
  val localDate = date.toLocalDate()
  val localTime = date.toLocalTime()

  return LocalDateTime(
    date = localDate,
    time = localTime
  )
}

actual fun LocalDateTime.toMillis(timeZone: TimeZone): Double {
  return toJsDateCurrentTimeZone().getTime()
}

fun LocalDateTime.toJsDateCurrentTimeZone(): Date {
  return Date(year.value, month.value - 1, dayOfMonth.value, hour, minute, second, millis)
}

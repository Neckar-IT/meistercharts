package it.neckar.datetime.minimal

import it.neckar.open.unit.si.ms
import kotlin.js.Date

/**
 * Extracts the local date from the current time zone
 */
actual fun LocalDate.Companion.fromMillisCurrentTimeZone(millis: @ms Double, expectedTimeZone: TimeZone): LocalDate {
  verifySystemTimeZone(expectedTimeZone)
  return Date(millis).toLocalDate()
}

fun verifySystemTimeZone(expectedTimeZone: TimeZone) {
  //TODO implement me somehow
}

/**
 * Creates a new JS date using the local timezone
 */
fun LocalDate.toJsDateCurrentTimeZone(): Date {
  return Date(year.value, month.value - 1, dayOfMonth.value)
}

/**
 * Converts the date to a *local* [LocalDate]
 */
fun Date.toLocalDate(): LocalDate {
  return LocalDate(
    this.getFullYear(),
    this.getMonth() + 1,
    this.getDate(), //this is correct! getDay() returns the day of the week
  )
}

/**
 * Calculates the UTC timestamp
 * If no [timeZone] is provided, the system time zone is used.
 *
 *
 * Attention: At the moment the time zone is ignored on JS
 */
actual fun LocalDate.toMillisAtStartOfDay(timeZone: TimeZone): Double {
  return toJsDateCurrentTimeZone().getTime()
}

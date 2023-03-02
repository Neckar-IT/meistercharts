package it.neckar.open.time

import com.meistercharts.algorithms.TimeRange
import java.time.Instant
import java.time.chrono.ChronoZonedDateTime

/**
 * Creates a time range
 */
fun ChronoZonedDateTime<*>.timeRangeTo(to: ChronoZonedDateTime<*>): TimeRange {
  val fromMillis = toDoubleMillis()
  val toMillis = to.toDoubleMillis()

  return TimeRange(fromMillis, toMillis)
}

fun Instant.timeRangeTo(to: Instant): TimeRange {
  val fromMillis = toDoubleMillis()
  val toMillis = to.toDoubleMillis()

  return TimeRange(fromMillis, toMillis)
}


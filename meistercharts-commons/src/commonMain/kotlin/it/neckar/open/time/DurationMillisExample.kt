package it.neckar.open.time

import it.neckar.open.unit.si.ms

/**
 *
 */
object DurationMillisExample {
  /**
   * Milli seconds per second
   */
  const val millisPerSecond: @ms Double = 1000.0

  /**
   * Number of milliseconds in a standard minute.
   */
  const val millisPerMinute: @ms Double = 60 * millisPerSecond

  /**
   * Number of milliseconds in a standard hour.
   */
  const val millisPerHour: @ms Double = 60 * millisPerMinute

  /**
   * Number of milliseconds in a standard day.
   */
  const val millisPerDay: @ms Double = 24 * millisPerHour

}

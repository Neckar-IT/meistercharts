package it.neckar.datetime.minimal

import it.neckar.open.unit.other.Approximation
import it.neckar.open.unit.si.ms

object TimeConstants {
  /**
   * A timestamp that may serve as a reference point in time.
   *
   * 2001-09-09T01:46:40.000
   */
  const val referenceTimestamp: @ms Double = 1.7040672E12 //Beware that changing this constant may break pixel-related regression tests!

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

  const val millisPerYear: @ms Double = 365 * millisPerDay

  @Approximation
  val millisPerDecade: @ms Double = 10 * millisPerYear

  /**
   * Number of milliseconds in a century - approximation.
   */

  @Approximation
  val millisPerCentury: @ms Double = 10 * millisPerDecade
}

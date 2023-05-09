package it.neckar.open.i18n

import it.neckar.open.time.TimeZone

/**
 * Provides the default timeZone
 */
expect class SystemTimeZoneProvider() {
  /**
   * Returns the system timeZone (from the browser or os)
   */
  val systemTimeZone: TimeZone
}

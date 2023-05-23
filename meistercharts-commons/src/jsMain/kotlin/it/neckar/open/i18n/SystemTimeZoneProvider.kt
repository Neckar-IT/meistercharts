package it.neckar.open.i18n

import it.neckar.open.time.TimeZone

/**
 * Provides the default timeZone
 */
actual class SystemTimeZoneProvider actual constructor() {
  /**
   * Returns the default timeZone (from the browser or os)
   */
  actual val systemTimeZone: TimeZone
    get() {
      return try {
        getBrowserTimeZone()
      } catch (e: Exception) {
        console.warn("Could not get browser time zone due to : ${e.message}")
        TimeZone.UTC
      }
    }


  /**
   * Returns the timeZone from the browser
   */
  fun getBrowserTimeZone(): TimeZone {
    val timeZoneString = js("Intl.DateTimeFormat().resolvedOptions().timeZone")
    return TimeZone(timeZoneString as String)
  }
}

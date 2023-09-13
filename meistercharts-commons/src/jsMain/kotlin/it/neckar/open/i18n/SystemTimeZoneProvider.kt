package it.neckar.open.i18n

import it.neckar.logging.Logger
import it.neckar.logging.LoggerFactory
import it.neckar.datetime.minimal.TimeZone

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
    logger.debug("timeZoneString: $timeZoneString")

    if (timeZoneString == TimeZone.Unknown.zoneId) {
      logger.info("Falling back to UTC since browser does not provide a timeZone")
      return TimeZone.UTC
    }

    return TimeZone(timeZoneString as String)
  }

  private val logger: Logger = LoggerFactory.getLogger("it.neckar.open.i18n.SystemTimeZoneProvider")
}

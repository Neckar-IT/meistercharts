/*
 * Copyright (C) 2013-2026 Neckar IT GmbH, Mössingen, Germany
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Linking this library statically or dynamically with other modules is
 * making a combined work based on this library. Thus, the terms and
 * conditions of the GNU General Public License cover the whole combination.
 *
 * As a special exception, the copyright holders of this library give you
 * permission to link this library with independent modules, regardless of
 * the license terms of these independent modules, and to copy and distribute
 * the resulting combined work under terms of your choice, provided that every
 * copy of the combined work is accompanied by a complete copy of the source
 * code of this library.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
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

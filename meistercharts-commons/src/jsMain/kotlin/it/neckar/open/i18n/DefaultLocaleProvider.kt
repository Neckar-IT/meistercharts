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
import it.neckar.logging.debug
import it.neckar.open.formatting.decimalFormat
import it.neckar.datetime.minimal.TimeZone
import kotlinx.browser.window
import org.w3c.dom.Navigator

/**
 * Provides the default locale
 */
actual class DefaultLocaleProvider {
  /**
   * Returns the default locale (from the browser or os)
   */
  actual val defaultLocale: Locale
    get() = getBrowserLocale()

  /**
   * Returns the locale from the browser
   */
  fun getBrowserLocale(): Locale {
    val navigator: Navigator = window.navigator

    if (navigator.language == "C") {
      //Fallback to ensure the locale is supported
      return Locale.US
    }

    //The locale that has been calculated using the browser language
    val locale = Locale(navigator.language)

    return try {
      val i18nConfiguration = I18nConfiguration(textLocale = locale, formatLocale = locale, timeZone = TimeZone.UTC) //timezone doesn't matter here
      //Do *not* access [DefaultI18nConfiguration] - has not yet been initialized!

      val probeResult = decimalFormat.format(0.0, i18nConfiguration)
      logger.trace("Locale probe succeeded for <${locale.locale}>: '$probeResult'")

      //The locale can be used to format a string, use it
      locale
    } catch (e: Throwable) {
      //Formatting with the given locale does not work. Fallback to US
      logger.info("Locale <${locale.locale}> not supported. Falling back to ${Locale.US}")
      logger.debug { "Exception when formatting with locale <${locale.locale}>: ${e.stackTraceToString()}" }
      Locale.US
    }
  }

  companion object {
    private val logger: Logger = LoggerFactory.getLogger("it.neckar.open.i18n.DefaultLocaleProvider")
  }
}

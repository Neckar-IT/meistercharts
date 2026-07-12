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

import it.neckar.datetime.minimal.TimeZone
import it.neckar.open.annotations.JsExportForTs
import kotlin.jvm.JvmStatic

/**
 * Contains the configuration for I18n
 */
@JsExportForTs
data class I18nConfiguration(
  /**
   * The locale that is used to resolve texts
   */
  val textLocale: Locale,

  /**
   * The locale that is used to format numbers and dates.
   */
  val formatLocale: Locale,

  /**
   * The time zone
   */
  var timeZone: TimeZone,
) {

  override fun toString(): String {
    return "I18nConfiguration(textLocale=$textLocale, formatLocale=$formatLocale, timeZone=$timeZone)"
  }

  companion object {
    /**
     * Convenience constructor emulation - useful when refactoring
     */
    @JvmStatic
    operator fun invoke(timeZone: TimeZone, locale: Locale): I18nConfiguration {
      return I18nConfiguration(locale, locale, timeZone)
    }

    /**
     * I18n configuration for Germany
     */
    @JvmStatic
    val Germany: I18nConfiguration = I18nConfiguration(
      textLocale = Locale.Germany,
      formatLocale = Locale.Germany,
      timeZone = TimeZone.Berlin
    )

    /**
     * German locales with time zone set to UTC
     */
    @JvmStatic
    val GermanyUTC: I18nConfiguration = I18nConfiguration(
      textLocale = Locale.Germany,
      formatLocale = Locale.Germany,
      timeZone = TimeZone.UTC
    )

    @JvmStatic
    val US: I18nConfiguration = I18nConfiguration(
      textLocale = Locale.US,
      formatLocale = Locale.US,
      timeZone = TimeZone.NewYork
    )

    @JvmStatic
    val US_UTC: I18nConfiguration = I18nConfiguration(
      textLocale = Locale.US,
      formatLocale = Locale.US,
      timeZone = TimeZone.UTC
    )
  }
}

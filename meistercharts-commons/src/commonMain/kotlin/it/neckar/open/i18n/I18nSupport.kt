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
import it.neckar.open.context.Context

/**
 * I18n support.
 *
 * For each canvas support there exists one [I18nSupport]
 *
 * The locales may be changed at runtime, but there is no
 * guarantee that the new locale will be picked up by existing instances.
 * It is strongly suggested to set the locale *first* and instantiate any objects later
 */
class I18nSupport {
  /**
   * The current i18n configuration.
   * If no configuration is set, the value is null.
   */
  var selectedConfiguration: I18nConfiguration? = null

  /**
   * Returns the configured i18n configuration or [CurrentI18nConfiguration] if no configuration is set
   */
  val configuration: I18nConfiguration
    get() {
      return selectedConfiguration ?: CurrentI18nConfiguration
    }

  /**
   * The time zone
   */
  var timeZone: TimeZone
    get() {
      return configuration.timeZone
    }
    set(value) {
      selectedConfiguration = configuration.copy(timeZone = value)
    }

  /**
   * The locale that is used to resolve texts
   */
  var textLocale: Locale
    get() = configuration.textLocale
    set(value) {
      selectedConfiguration = configuration.copy(textLocale = value)
    }

  /**
   * The locale that is used to format numbers / dates
   */
  var formatLocale: Locale
    get() = configuration.formatLocale
    set(value) {
      selectedConfiguration = configuration.copy(formatLocale = value)
    }
}

/**
 * The system locale - provided by the system (e.g., browser language).
 * This value cannot be changed.
 */
val SystemLocale: Locale = DefaultLocaleProvider().defaultLocale

/**
 * Contains the default timeZone for this system.
 * Cannot be changed set since it is provided by the system.
 *
 * The default time zone can be set for each component in [it.neckar.open.i18n.I18nSupport]
 *
 * A custom default time zone can be configured when initializing the platform
 */
val SystemTimeZone: TimeZone = SystemTimeZoneProvider().systemTimeZone

/**
 * The system i18n configuration. Is used as fallback.
 */
val SystemI18nConfiguration: I18nConfiguration = I18nConfiguration(
  textLocale = SystemLocale,
  formatLocale = SystemLocale,
  timeZone = SystemTimeZone
)


/**
 * The I18n context.
 */
val I18nContext: Context<I18nConfiguration> = Context(SystemI18nConfiguration)

/**
 * The (current) default i18n configuration.
 * Uses the [I18nContext] to store/retrieve the value.
 */
val CurrentI18nConfiguration: I18nConfiguration
  get() = I18nContext.current


@Deprecated("Use DefaultI18nConfiguration instead", ReplaceWith("CurrentI18nConfiguration"))
val DefaultI18nConfiguration: I18nConfiguration
  get() = CurrentI18nConfiguration

/**
 * Updates the [CurrentI18nConfiguration]. Use with care!
 * It is possible to set the locale for a component itself ([I18nSupport]).
 */
fun setDefaultI18nConfiguration(i18nConfiguration: I18nConfiguration) {
  I18nContext.defaultValue = i18nConfiguration
}

/**
 * Sets this configuration as default
 */
fun I18nConfiguration.setAsDefault() {
  setDefaultI18nConfiguration(this)
}

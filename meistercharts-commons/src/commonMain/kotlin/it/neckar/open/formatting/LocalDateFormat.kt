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
package it.neckar.open.formatting

import it.neckar.datetime.minimal.LocalDate
import it.neckar.open.i18n.I18nConfiguration
import it.neckar.open.kotlin.lang.WhitespaceConfig
import it.neckar.open.unit.si.ms

/**
 * Formats a local date
 */
interface LocalDateFormat {
  /**
   * Formats a date to a string
   */
  fun format(@ms localDate: LocalDate, i18nConfiguration: I18nConfiguration, whitespaceConfig: WhitespaceConfig = WhitespaceConfig.NonBreaking): String

  /**
   * Format the provided timestamp. If the timestamp is null, the [fallback] is returned.
   */
  fun formatNullable(@ms localDate: LocalDate?, i18nConfiguration: I18nConfiguration, whitespaceConfig: WhitespaceConfig = WhitespaceConfig.NonBreaking, fallback: String): String {
    if (localDate == null) {
      return fallback
    }

    return format(localDate, i18nConfiguration, whitespaceConfig)
  }
}

/**
 * Formats a date according to ISO 8601
 */
val localDateFormatIso8601: CachedLocalDateFormat = LocalDateFormatIso8601().cached()

val localDateFormat: CachedLocalDateFormat = DefaultLocalDateFormat().cached()


class LocalDateFormatIso8601 : LocalDateFormat {
  override fun format(@ms localDate: LocalDate, i18nConfiguration: I18nConfiguration, whitespaceConfig: WhitespaceConfig): String {
    return localDate.format()
  }
}

/**
 * Formats a date according to the locale
 */
expect class DefaultLocalDateFormat() : LocalDateFormat {
  override fun format(localDate: LocalDate, i18nConfiguration: I18nConfiguration, whitespaceConfig: WhitespaceConfig): String
}

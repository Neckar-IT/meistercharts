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

import it.neckar.open.collections.cache

private val fx2localeCache = cache<java.util.Locale, it.neckar.open.i18n.Locale>("fx2localeCache", 50)
private val locale2FxCache = cache<it.neckar.open.i18n.Locale, java.util.Locale>("locale2FxCache", 50)

/**
 * Converts the given java.util.Locale
 */
fun java.util.Locale.convert(): it.neckar.open.i18n.Locale {
  return fx2localeCache.getOrStore(this) {
    it.neckar.open.i18n.Locale(this.toLanguageTag())
  }
}

/**
 * Converts the locale to a java.util.Locale
 */
fun it.neckar.open.i18n.Locale.convert(): java.util.Locale {
  return locale2FxCache.getOrStore(this) {
    java.util.Locale.forLanguageTag(this.locale)
  }
}

/**
 * Provides the default locale
 */
actual class DefaultLocaleProvider {
  /**
   * Returns the default locale (from the browser or os)
   */
  actual val defaultLocale: Locale
    get() = java.util.Locale.getDefault().convert()

}

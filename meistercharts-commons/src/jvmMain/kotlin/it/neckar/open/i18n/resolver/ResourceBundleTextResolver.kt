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
package it.neckar.open.i18n.resolver

import it.neckar.open.i18n.I18nConfiguration
import it.neckar.open.i18n.Locale
import it.neckar.open.i18n.TextKey
import it.neckar.open.i18n.TextResolver
import java.util.MissingResourceException
import java.util.ResourceBundle

/**
 * Type alias to simplify conversion from deprecated messages class
 */
typealias Messages = ResourceBundleTextResolver

/**
 * Uses [java.util.ResourceBundle] to resolve a text
 */
class ResourceBundleTextResolver(
  val bundleName: String
) : TextResolver {

  override fun resolve(key: TextKey, i18nConfiguration: I18nConfiguration): String? {
    val bundle = ResourceBundle.getBundle(bundleName, i18nConfiguration.textLocale.toJvmLocale())
    return try {
      bundle.getString(key.key)
    } catch (ignored: MissingResourceException) {
      // Honour the TextResolver contract (returns String?) so callers can fall through to
      // other resolvers instead of getting an exception. ResourceBundle.getString throws
      // MissingResourceException when the key is absent; SimpleMapBasedTextResolver and
      // friends return null in the same situation.
      null
    }
  }
}

private fun Locale.toJvmLocale(): java.util.Locale {
  return java.util.Locale.forLanguageTag(this.locale)
}

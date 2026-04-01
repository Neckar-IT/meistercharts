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

import java.text.MessageFormat
import java.util.Locale
import java.util.ResourceBundle
import javax.annotation.concurrent.Immutable

/**
 * Offers access to resource bundle entries
 */
@Deprecated("Use TextService and/or TextResolver instead")
@Immutable
class Messages(val bundleName: String) {
  operator fun get(key: String, locale: Locale, vararg messageArguments: Any): String {
    val bundle = ResourceBundle.getBundle(bundleName, locale)

    return if (messageArguments.isEmpty()) {
      bundle.getString(key)
    } else {
      MessageFormat.format(bundle.getString(key), *messageArguments)
    }
  }

  operator fun get(enumValue: Enum<*>, locale: Locale, vararg messageArguments: Any): String {
    return get(enumValue, null, locale, *messageArguments)
  }

  operator fun get(enumValue: Enum<*>, category: String?, locale: Locale, vararg messageArguments: Any): String {
    val baseKey = enumValue.name

    val key: String = if (category == null) {
      baseKey
    } else {
      "$baseKey.$category"
    }

    return get(key, locale, *messageArguments)
  }
}

/**
 * Copyright (C) cedarsoft GmbH.
 *
 * Licensed under the GNU General Public License version 3 (the "License")
 * with Classpath Exception; you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.cedarsoft.org/gpl3ce
 * (GPL 3 with Classpath Exception)
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 3 only, as
 * published by the Free Software Foundation. cedarsoft GmbH designates this
 * particular file as subject to the "Classpath" exception as provided
 * by cedarsoft GmbH in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 3 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 3 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact cedarsoft GmbH, 72810 Gomaringen, Germany,
 * or visit www.cedarsoft.com if you need additional information or
 * have any questions.
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

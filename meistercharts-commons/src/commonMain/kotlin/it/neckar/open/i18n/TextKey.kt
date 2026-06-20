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

import kotlinx.serialization.Serializable
import kotlin.contracts.contract
import kotlin.jvm.JvmField
import kotlin.jvm.JvmStatic

/**
 * A unique key to identify a certain piece of text
 */
@Serializable
class TextKey(
  /**
   * The key
   */
  val key: String,
  /**
   * The fallback text that may be used if no resolved text is available
   */
  val fallbackText: String
) {

  override fun toString(): String {
    return "$key [$fallbackText]"
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is TextKey) return false

    if (key != other.key) return false

    return true
  }

  override fun hashCode(): Int {
    return key.hashCode()
  }

  /**
   * Returns true if the key is an empty string
   */
  fun isEmpty(): Boolean {
    return this.key.isEmpty()
  }

  companion object {
    /**
     * Creates a new text key with the given default text that is also used as key
     */
    @JvmStatic
    fun simple(keyAndFallbackText: String): TextKey {
      return TextKey(keyAndFallbackText, keyAndFallbackText)
    }

    /**
     * Creates a text key where the key and fallback text are set to the provided value
     */
    operator fun invoke(keyAndFallbackText: String): TextKey {
      return simple(keyAndFallbackText)
    }

    /**
     * A [TextKey] for the empty string
     */
    @JvmField
    val empty: TextKey = simple("")
  }
}

/**
 * Returns false, if this is null
 */
inline fun TextKey?.isEmpty(): Boolean {
  contract {
    returns(false) implies (this@isEmpty != null)
  }
  return this?.isEmpty() ?: true
}

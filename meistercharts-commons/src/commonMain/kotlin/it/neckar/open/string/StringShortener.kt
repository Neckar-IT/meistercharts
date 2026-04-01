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
package it.neckar.open.string

import it.neckar.open.kotlin.lang.truncateCenterToLength
import it.neckar.open.kotlin.lang.truncateToLength

/**
 * Contains extension tools for texts
 */

/**
 * Shortens a string if necessary
 */
interface StringShortener {
  /***
   * Shortens the given text.
   * If the truncation symbol is longer than [maxCharacters] a "!" will be returned to avoid confusion
   *
   * If shortening is not possible (e.g. the max characters are 0) null is returned.
   */
  fun shorten(text: String, maxCharacters: Int, truncationSymbol: String = "…"): String?

  /**
   * Shortens the string to max length in characters. Uses the truncation symbol if provided.
   * Truncates the end
   */
  object TruncateToLength : StringShortener {
    override fun shorten(text: String, maxCharacters: Int, truncationSymbol: String): String? {
      return text.truncateToLength(maxCharacters, truncationSymbol)
    }

    override fun toString(): String {
      return "Truncate to length"
    }
  }

  /**
   * Shortens the string to max length in characters. Uses the truncation symbol.
   * Truncates the center
   */
  object TruncateCenterToLength : StringShortener {
    override fun shorten(text: String, maxCharacters: Int, truncationSymbol: String): String? {
      return text.truncateCenterToLength(maxCharacters, truncationSymbol)
    }

    override fun toString(): String {
      return "Truncate center length"
    }
  }

  data object NoOp : StringShortener {
    override fun shorten(text: String, maxCharacters: Int, truncationSymbol: String): String {
      return text
    }
  }
}

/**
 * Shortens the given string
 */
fun String.shorten(length: Int, stringShortener: StringShortener = StringShortener.TruncateToLength): String? {
  return stringShortener.shorten(this, length)
}


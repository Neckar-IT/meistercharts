/**
 * Copyright 2023 Neckar IT GmbH, Mössingen, Germany
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.meistercharts.canvas.text

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


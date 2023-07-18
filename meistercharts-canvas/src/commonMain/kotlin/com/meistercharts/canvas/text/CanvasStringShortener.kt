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

import com.meistercharts.canvas.CanvasRenderingContext
import it.neckar.open.collections.genericBinarySearchLeft
import it.neckar.open.unit.other.px

/**
 * A string shortener that is aware of the size of a font on the canvas
 */
fun interface CanvasStringShortener {

  /**
   * Shortens the given string to fix the max length.
   * Returns null if the string can not be shortened to the max width (e.g. when the max width is too small for the current font)
   */
  fun shorten(text: String, maxWidth: @px Double, gc: CanvasRenderingContext): String?

  object NoOp : CanvasStringShortener {
    override fun shorten(text: String, maxWidth: @px Double, gc: CanvasRenderingContext): String {
      return text
    }
  }

  /**
   * Calculates the exact width.
   * ATTENTION: Is very slow
   */
  class ExactButVerySlow(
    /**
     * The string shortener that is used to shortened the string
     */
    val stringShortener: StringShortener
  ) : CanvasStringShortener {
    override fun shorten(text: String, maxWidth: @px Double, gc: CanvasRenderingContext): String? {
      //If it fits, return the text
      if (gc.calculateTextWidth(text) <= maxWidth) {
        return text
      }

      //Shorten until it fits
      for (i in 1..text.length) {
        val truncatedText = stringShortener.shorten(text, text.length - i) ?: return null

        if (gc.calculateTextWidth(truncatedText) <= maxWidth) {
          return truncatedText
        }
      }

      //We can not shorten the text any further
      return null
    }
  }

  /**
   * Calculates the exact width
   * ATTENTION: Is not very fast!
   */
  class ExactButSlow(
    /**
     * The string shortener that is used to shorten the string
     */
    val stringShortener: StringShortener,
  ) : CanvasStringShortener {
    override fun shorten(text: String, maxWidth: @px Double, gc: CanvasRenderingContext): String? {
      //If it fits, return the text
      if (gc.calculateTextWidth(text) <= maxWidth) {
        return text
      }

      //We can not shorten texts with 1 character that do not fully match
      if (text.length <= 1) {
        return null
      }

      //Guess the width
      val targetLength = genericBinarySearchLeft(0, text.length) { value ->
        val truncatedText = stringShortener.shorten(text, value) ?: return@genericBinarySearchLeft -1

        val textWidth = gc.calculateTextWidth(truncatedText)
        textWidth.compareTo(maxWidth)
      }

      if (targetLength < 1) {
        //We can not shorten the text any further
        return null
      }

      return stringShortener.shorten(text, targetLength)
    }
  }

  /**
   * Shows the complete text - or no text at all.
   *
   * Useful for formatted numbers that must not be displayed partially
   */
  object AllOrNothing : CanvasStringShortener {
    override fun shorten(text: String, maxWidth: Double, gc: CanvasRenderingContext): String? {
      //Too wide
      if (gc.calculateTextWidth(text) > maxWidth) {
        return null
      }

      return text
    }
  }

  companion object {
    /**
     * Slowly calculates the text width and shortens the text at its end until it fits the given maximum width
     */
    val exactButSlowTruncateToLength: ExactButSlow = ExactButSlow(StringShortener.TruncateToLength)
  }
}


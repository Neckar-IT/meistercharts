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
package com.meistercharts.algorithms.painter

import it.neckar.open.formatting.CachedNumberFormat
import it.neckar.open.formatting.decimalFormat
import it.neckar.open.i18n.I18nConfiguration
import it.neckar.open.unit.other.pct

/**
 * Functions related to colors
 */
object Colors {
  /**
   * The format to be used for the opacity
   */
  private val opacityFormat: CachedNumberFormat = decimalFormat(2, 0, 1, false)

  /**
   * Creates the web string.
   *
   * If the color is opaque, a hex string is returned, otherwise the rgba(...) representation
   */
  fun toWebString(red: Int, green: Int, blue: Int, opacity: @pct Double = 1.0): String {
    return when (opacity) {
      1.0 -> {
        toHexString(red, green, blue)
      }

      else -> toRgbaString(red, green, blue, opacity)
    }
  }

  /**
   * Converts the provided color to a rgba string
   */
  fun toRgbaString(red: Int, green: Int, blue: Int, opacity: @pct Double): String {
    return "rgba(${(red)}, ${(green)}, ${(blue)}, ${opacityFormat.format(opacity, I18nConfiguration.US)})"
  }

  /**
   * Creates a 6 char hex string representation for the given color
   */
  fun toHexString(red: Int, green: Int, blue: Int): String {
    return "#${to2DigitHexString(red)}${to2DigitHexString(green)}${to2DigitHexString(blue)}"
  }

  /**
   * Converts an int value (0..255) to a two digit hex value
   */
  fun to2DigitHexString(value: Int): String {
    val hex = value.toString(16)
    return hex.padStart(2, '0').uppercase()
  }

  /**
   * Returns the int value for a two digit hex value
   */
  internal fun String.parse2DigitHex(): Int {
    return toInt(16)
  }
}

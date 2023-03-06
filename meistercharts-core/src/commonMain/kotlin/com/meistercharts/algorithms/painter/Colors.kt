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


/**
 * The format to be used for the opacity
 */
private val opacityFormat: CachedNumberFormat = decimalFormat(2, 0, 1, false)

/**
 * Creates the web string with the rgba(...) representation
 * @param red 0..255
 * @param green 0..255
 * @param blue 0..255
 * @param opacity 0..1.0
 */
fun toWebString(red: Int, green: Int, blue: Int, opacity: @pct Double?): String {
  if (opacity == null) {
    return toHex(red, green, blue, null)
  }

  return toRgbaString(red, green, blue, opacity)
}

/**
 * Returns an rgb(a) string
 */
fun toRgbaString(red: Int, green: Int, blue: Int, opacity: @pct Double?): String {
  if (opacity == null) {
    return "rgb(${(red)},${(green)},${(blue)})"
  } else {
    return toRgbaString(red, green, blue, opacity)
  }
}

fun toRgbaString(red: Int, green: Int, blue: Int, opacity: @pct Double): String {
  return "rgba(${(red)},${(green)},${(blue)},${opacityFormat.format(opacity, I18nConfiguration.US)})"
}

/**
 * Creates a hex string representation for the given color
 */
fun toHex(red: Int, green: Int, blue: Int, opacity: Int?): String {
  val opacityHex = opacity?.let { to2DigitHex(it) }.orEmpty()
  return "#${to2DigitHex(red)}${to2DigitHex(green)}${to2DigitHex(blue)}$opacityHex"
}

/**
 * Converts an int value (0..255) to a two digit hex value
 */
fun to2DigitHex(value: Int): String {
  val hex = value.toString(16)
  return hex.padStart(2, '0').toUpperCase()
}

/**
 * Returns the int value for a two digit hex value
 */
fun String.parse2DigitHex(): Int {
  return toInt(16)
}


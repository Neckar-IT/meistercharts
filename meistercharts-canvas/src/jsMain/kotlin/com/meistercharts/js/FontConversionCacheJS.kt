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
package com.meistercharts.js

import com.meistercharts.font.FontDescriptor
import com.meistercharts.font.FontStyle
import com.meistercharts.font.FontVariant
import com.meistercharts.font.FontWeight
import it.neckar.open.collections.cache

/**
 * Helper that converts from FontDescriptor to font strings
 */
object FontConversionCacheJS {
  /**
   * Reverse map for lookups from currently set font to font descriptor
   */
  internal val fromHtmlCache = cache<String, FontDescriptor>("HtmlFontConversionCache", 500)

  /**
   * Stores the pair in the cache
   */
  fun store(value: FontDescriptor, htmlFontString: String) {
    fromHtmlCache[htmlFontString] = value
  }

  /**
   * Returns the font descriptor for a given font.
   *
   * This methods just does a lookup!
   */
  fun reverse(htmlFontString: String): FontDescriptor {
    return fromHtmlCache[htmlFontString] ?: throw IllegalArgumentException("No entry available for <$htmlFontString>")
  }
}

/**
 * Returns a string that represents this font descriptor
 */
fun FontDescriptor.convertToHtmlFontString(): String {
  val htmlFontWeight = weight.toHtmlFontWeightString()
  val htmlFontStyle = style.toHtmlFontStyleString()
  val htmlFontVariant = variant.toHtmlFontVariantString()
  val htmlFontSize = "${size.size}px"
  val htmlFontFamily = family.family

  //https://developer.mozilla.org/en-US/docs/Web/CSS/font
  return "$htmlFontStyle $htmlFontVariant $htmlFontWeight $htmlFontSize $htmlFontFamily"
}

private fun FontWeight.toHtmlFontWeightString(): String {
  return weight.toString()
}

private fun FontStyle.toHtmlFontStyleString(): String {
  return when (this) {
    FontStyle.Normal  -> ""
    FontStyle.Italic  -> "italic"
    FontStyle.Oblique -> "oblique"
  }
}

private fun FontVariant.toHtmlFontVariantString(): String {
  return when (this) {
    FontVariant.Normal    -> ""
    FontVariant.SmallCaps -> "small-caps"
  }
}

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
package com.meistercharts.font

/**
 * Represents a font configuration consisting of a font family and a generic family.
 */
data class FontFamilyConfiguration(
  /**
   * The (optional) font family.
   */
  override val family: FontFamily? = null,
  /**
   * The *required* generic family. Is used as fallback
   */
  override val genericFamily: GenericFontFamily = GenericFontFamily.SansSerif,
) : FontDescriptorFragment() {
  companion object {
    /**
     * Creates a new font family configuration with the given generic family.
     * The font family is set to null.
     */
    fun onlyGeneric(genericFamily: GenericFontFamily): FontFamilyConfiguration {
      return FontFamilyConfiguration(null, genericFamily)
    }

    /**
     * A configuration that only contains the generic family "sans-serif".
     * Does *not* contain a font family.
     */
    val SansSerif: FontFamilyConfiguration = FontFamilyConfiguration(null, GenericFontFamily.SansSerif)
  }
}

@Suppress("NOTHING_TO_INLINE")
inline fun FontFamily.withSansSerif(): FontFamilyConfiguration {
  return withGenericFontFamily(GenericFontFamily.SansSerif)
}

/**
 * Appends a generic family to the font family.
 */
fun FontFamily.withGenericFontFamily(genericFamily: GenericFontFamily): FontFamilyConfiguration {
  return FontFamilyConfiguration(this, genericFamily)
}

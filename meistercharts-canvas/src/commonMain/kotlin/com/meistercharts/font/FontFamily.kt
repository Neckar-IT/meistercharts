/*
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
@file:Suppress("SpellCheckingInspection")

package com.meistercharts.font

import it.neckar.open.annotations.TestOnly

/**
 * Represents a font family.
 *
 * Attention: In CSS the complete list of font families and generic font families is called "font family"
 * [https://developer.mozilla.org/en-US/docs/Web/CSS/font-family]
 */
data class FontFamily(
  /**
   * The (complete) font family string. Does not support multiple families.
   */
  val family: String,
) {
  init {
    require(family.isNotEmpty()) { "family must not be blank" }
    require(family.trim() == family) { "Trim the family string" }
  }

  override fun toString(): String {
    return family
  }

  /**
   * Returns true if this font familiy represents (possibly!) the default serif font familiy of the browser.
   * Firefox does not hardcode a specific serif font; it uses a "serif" generic family.
   * The default html canvas and body font are documented in Readme-DefaultFonts.md.
   */
  fun isProbablyDefaultSerifFamily(): Boolean {
    return family == "serif" || //Firefox
      family == TimesNewRoman.family || //Chrome
      family == "-webkit-standard" || // Safari (works like serif generic font for firefox)
      family == "Times" // MacOS Chrome
  }

  companion object {
    //Sans Serif families
    val Arial: FontFamily = FontFamily("Arial")
    val Verdana: FontFamily = FontFamily("Verdana")
    val Tahoma: FontFamily = FontFamily("Tahoma")
    val TrebuchetMS: FontFamily = FontFamily("Trebuchet MS")

    //Serif families
    val TimesNewRoman: FontFamily = FontFamily("Times New Roman")
    val Palatino: FontFamily = FontFamily("Palatino")
    val Georgia: FontFamily = FontFamily("Georgia")
    val Garamond: FontFamily = FontFamily("Garamond")


    //Monospaced
    val CourierNew: FontFamily = FontFamily("Courier New")
    val LucidaConsole: FontFamily = FontFamily("Lucida Console")

    //Cursive
    val BrushScriptMT: FontFamily = FontFamily("Brush Script MT")


    val FontAwesome6Free: FontFamily = FontFamily("Font Awesome 6 Free")

    val FontAwesome6Brands: FontFamily = FontFamily("Font Awesome 6 Brands")

    @Deprecated("use FontAwesome6Free or FontAwesome6Brands instead")
    val FontAwesome: FontFamily = FontFamily("FontAwesome")

    val Oswald: FontFamily = FontFamily("Oswald")
    val OpenSans: FontFamily = FontFamily("Open Sans")
    val MaterialIcons: FontFamily = FontFamily("Material Icons")


    /**
     * Contains some of the most common font families
     */
    @TestOnly
    val entries: List<FontFamily> = listOf(
      Arial,
      Verdana,
      Tahoma,
      TrebuchetMS,
      TimesNewRoman,
      Palatino,
      Georgia,
      Garamond,
      CourierNew,
      LucidaConsole,
      BrushScriptMT,
      FontAwesome6Free,
      FontAwesome6Brands,
      Oswald,
      OpenSans,
    )
  }
}

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
package com.meistercharts.fx.font

import com.meistercharts.font.FontFamily
import com.meistercharts.font.FontWeight
import com.meistercharts.font.GenericFontFamily

/**
 * Maps our font families to JavaFX specific font families.
 *
 * This is at least necessary for FontAwesome
 */
object FontFamilyMapping {

  fun toJavaFxString(family: FontFamily?, genericFamily: GenericFontFamily, weight: FontWeight): String {
    if (family == null) {
      return genericFamily.keyword
    }

    val convertedFamilyName = when (val originalFamily = family.family) {
      "Font Awesome 6 Free" -> {
        when {
          weight.weight <= 400 -> "Font Awesome 6 Free Regular"
          else -> "Font Awesome 6 Free Solid"
        }
      }

      "FontAwesome" -> {
        "Font Awesome 6 Free Solid"
      }

      "Font Awesome 6 Brands" -> {
        "Font Awesome 6 Brands Regular"
      }

      else -> {
        originalFamily
      }
    }
    return convertedFamilyName

  }
}

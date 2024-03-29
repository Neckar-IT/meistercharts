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

import kotlin.jvm.JvmField

/**
 * Represents the weight (or boldness) of a font.
 *
 * The weight must be a value between 1 and 1000, inclusive.
 *
 * [CSS font-weight](https://developer.mozilla.org/de/docs/Web/CSS/font-weight)
 */
data class FontWeight(val weight: Int) {

  init {
    require(weight in 1..1000) { "weight must be a value between 1 and 1000, inclusive, but was <$weight>" }
  }

  companion object {
    /**
     * represents Thin font weight (100).
     */
    @JvmField
    val Thin: FontWeight = FontWeight(100)

    /**
     * represents 'Extra Light' font weight (200).
     */
    @JvmField
    val ExtraLight: FontWeight = FontWeight(200)

    /**
     * represents Light font weight (300).
     */
    @JvmField
    val Light: FontWeight = FontWeight(300)

    /**
     * represents Normal font weight (400).
     */
    @JvmField
    val Regular: FontWeight = FontWeight(400)

    /**
     * represents Normal font weight (400).
     */
    @JvmField
    val Normal: FontWeight = FontWeight(400)

    /**
     * represents Medium font weight (500).
     */
    @JvmField
    val Medium: FontWeight = FontWeight(500)

    /**
     * represents 'Demi Bold' font weight (600).
     */
    @JvmField
    val SemiBold: FontWeight = FontWeight(600)

    /**
     * represents Bold font weight (700).
     */
    @JvmField
    val Bold: FontWeight = FontWeight(700)

    /**
     * represents 'Extra Bold' font weight (800).
     */
    @JvmField
    val ExtraBold: FontWeight = FontWeight(800)

    /**
     * represents Black font weight (900).
     */
    @JvmField
    val Black: FontWeight = FontWeight(900)
  }
}

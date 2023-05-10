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
package com.meistercharts.canvas

import it.neckar.open.kotlin.lang.round
import it.neckar.open.unit.other.px

/**
 * The font size
 */
data class FontSize(val size: Double) : Comparable<FontSize> {
  init {
    require(size in 0.0..maxSize) {
      "Font size out of range: <$size>"
    }
  }

  /**
   * Creates a [FontSize] that is [offset] smaller than this [FontSize]
   */
  fun smaller(offset: Double): FontSize = FontSize(size - offset)

  /**
   * Creates a [FontSize] that is [offset] greater than this [FontSize]
   */
  fun greater(offset: Double): FontSize = FontSize(size + offset)

  override operator fun compareTo(other: FontSize): Int {
    return size.compareTo(other.size)
  }

  companion object {
    /**
     * The max size that is supported
     */
    private const val maxSize = 256.0

    val XXS: FontSize = FontSize(8.0)
    val XS: FontSize = FontSize(10.0)
    val S: FontSize = FontSize(12.0)
    val Default: FontSize = FontSize(16.0)
    val L: FontSize = FontSize(20.0)
    val XL: FontSize = FontSize(24.0)

    /**
     * The maximum supported font size
     */
    val Max: FontSize = FontSize(maxSize)


    /**
     * Conversion of points to pixels
     */
    fun points2pixels(points: Double): @px Double {
      return (points * 1.333).round()
    }
  }
}

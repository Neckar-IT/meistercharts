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

import it.neckar.geometry.Size
import it.neckar.open.collections.fastForEach
import it.neckar.open.unit.other.Exclusive

/**
 * Describes the size classification of a chart
 */
data class ChartSizeClassification(
  /**
   * The width classification
   */
  val width: SizeClassification,
  /**
   * The height classification
   */
  val height: SizeClassification,
) {

  companion object {
    val zero: ChartSizeClassification = ChartSizeClassification(SizeClassification.Zero, SizeClassification.Zero)

    fun get(size: Size): ChartSizeClassification {
      return ChartSizeClassification(SizeClassification.get(size.width), SizeClassification.get(size.height))
    }
  }
}

/**
 * The classification of sizes
 */
enum class SizeClassification(
  /**
   * The min size for this classification
   */
  val min: Int,
  /**
   * The max size for this classification
   */
  val max: @Exclusive Int,
) {
  Zero(0, 1),
  XS(1, 576),
  S(576, 768),
  M(768, 992),
  L(992, 1200),
  XL(1200, Int.MAX_VALUE);


  companion object {
    /**
     * Returns the classification for the given size
     */
    fun get(size: Double): SizeClassification {
      values().fastForEach {
        if (it.min <= size && it.max > size) {
          return it
        }
      }

      throw IllegalArgumentException("No size classification found for <$size>")
    }
  }
}

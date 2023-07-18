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

import it.neckar.open.unit.other.pct
import it.neckar.open.unit.other.px

/**
 * Contains some font metrics for one font
 */
data class FontMetrics(
  /**
   * Positive number (ascent)
   */
  val xLine: @px Double,
  /**
   * Positive number (ascent)
   */
  val capitalHLine: @px Double,

  /**
   * Positive number (ascent)
   */
  val accentLine: @px Double,

  /**
   * Negative number (descent)
   */
  val pLine: @px Double,

  /**
   * The alignment correction information
   */
  val alignmentCorrectionInformation: AlignmentCorrectionInformation = AlignmentCorrectionInformation.empty
) {

  /**
   * Returns the percentage of the ascent of the [totalHeight]
   */
  @Deprecated("Use accentLine instead somehow")
  val ascentPercentage: @pct Double
    get() = 1.0 / totalHeight * accentLine

  /**
   * Returns the total height - from [pLine] to [accentLine]
   */
  val totalHeight: @px Double
    get() = pLine + accentLine


  companion object {
    /**
     * Empty font metrics object with a total height of 0.0
     */
    val empty: FontMetrics = FontMetrics(0.0, 0.0, 0.0, 0.0, AlignmentCorrectionInformation.empty)

    /**
     * Returns the font metrics for a given font descriptor.
     * Uses the [FontMetricsCache]
     */
    operator fun get(font: FontDescriptor): FontMetrics {
      return FontMetricsCache.get()[font]
    }
  }
}

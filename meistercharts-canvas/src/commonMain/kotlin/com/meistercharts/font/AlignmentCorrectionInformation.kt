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

import com.meistercharts.model.VerticalAlignment
import it.neckar.open.unit.other.px

/**
 * Contains correction values for different vertical alignments.
 *
 * A correction value is defined as follows:
 *
 * The correction value must be *added* to the raw y position. The newly calculated (corrected) base line
 * is then used to paint the text using the native methods.
 *
 * This result looks like text has been painted on the *raw* y position correctly.
 */
data class AlignmentCorrectionInformation(
  /**
   * A correction value that should be used for vertically top aligned text.
   */
  @px val top: Double = 0.0,
  /**
   * A correction value that should be used with vertically centered aligned text.
   */
  @px val center: Double = 0.0,
  /**
   * A correction value that should be used with vertically bottom aligned text.
   */
  @px val bottom: Double = 0.0,
) {

  /**
   * Returns the alignment correction for the given vertical alignment
   */
  operator fun get(verticalAlignment: VerticalAlignment): Double {
    return when (verticalAlignment) {
      VerticalAlignment.Top -> top
      VerticalAlignment.Center -> center
      VerticalAlignment.Bottom -> bottom
      else -> 0.0
    }
  }

  companion object {
    val empty: AlignmentCorrectionInformation = AlignmentCorrectionInformation()
  }
}

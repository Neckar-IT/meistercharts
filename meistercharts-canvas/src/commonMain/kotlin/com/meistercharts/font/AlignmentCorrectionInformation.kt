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

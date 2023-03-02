package com.meistercharts.canvas

import com.meistercharts.model.VerticalAlignment
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
  @px val bottom: Double = 0.0
) {

  /**
   * Returns the alignment correction for the given vertical alignment
   */
  operator fun get(verticalAlignment: VerticalAlignment): Double {
    return when (verticalAlignment) {
      VerticalAlignment.Top    -> top
      VerticalAlignment.Center -> center
      VerticalAlignment.Bottom -> bottom
      else -> 0.0
    }
  }

  companion object {
    val empty: AlignmentCorrectionInformation = AlignmentCorrectionInformation()
  }
}

/**
 * Offers a way to receive the [FontMetricsCache].
 */
object FontMetricsCacheAccess {
  /**
   * The current font metrics cache.
   * The value is initialized in the Platform.init method
   */
  var fontMetricsCache: FontMetricsCache? = null
}

/**
 * Cache for [FontMetrics].
 */
interface FontMetricsCache {
  /**
   * Returns the font metrics for the given font descriptor
   */
  operator fun get(font: FontDescriptor): FontMetrics

  companion object {
    /**
     * Provides access to the font metrics cache.
     * The value is initialized in the Platform.init() method
     */
    fun get(): FontMetricsCache {
      return FontMetricsCacheAccess.fontMetricsCache ?: throw IllegalStateException("No fontMetricsCache set - please call MeisterChartPlatform.init()")
    }
  }
}

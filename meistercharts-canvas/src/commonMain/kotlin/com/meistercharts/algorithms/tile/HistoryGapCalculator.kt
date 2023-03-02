package com.meistercharts.algorithms.tile

import com.meistercharts.history.SamplingPeriod
import it.neckar.open.unit.si.ms

/**
 * Calculates the min distance for gaps
 */
fun interface HistoryGapCalculator {
  /**
   * Returns the minimum distance between two data points that shall be interpreted as gap (e.g. no connecting line is drawn)
   */
  fun calculateMinGapDistance(renderedSamplingPeriod: SamplingPeriod): @ms Double

  companion object {
    /**
     * Calculates the gap using a factor with the rendered sampling period
     */
    fun factor(factor: Double): DefaultHistoryGapCalculator {
      return DefaultHistoryGapCalculator(factor)
    }
  }
}

/**
 * Default implementation that uses a factor to calculate the distance
 */
data class DefaultHistoryGapCalculator(
  val factor: Double = 5.0
) : HistoryGapCalculator {
  override fun calculateMinGapDistance(renderedSamplingPeriod: SamplingPeriod): @ms Double {
    return renderedSamplingPeriod.distance * factor
  }
}

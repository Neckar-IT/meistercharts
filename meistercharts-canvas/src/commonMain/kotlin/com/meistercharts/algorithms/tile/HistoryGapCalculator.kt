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

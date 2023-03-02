package com.meistercharts.algorithms.tile

import com.meistercharts.history.SamplingPeriod

/**
 * Contains the properties that are relevant to render the history
 */
data class HistoryRenderProperties(
  /**
   * The sampling period that is used to paint the history (usually when painting the tiles)
   */
  val samplingPeriod: SamplingPeriod
) {

}

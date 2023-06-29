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

import com.meistercharts.time.TimeRange
import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.PaintingPropertyKey
import com.meistercharts.annotations.ContentArea
import com.meistercharts.canvas.paintingProperties
import it.neckar.logging.Logger
import it.neckar.logging.LoggerFactory
import it.neckar.logging.debug
import it.neckar.open.unit.si.ms

/**
 * A special layer that (just) calculates the history related properties
 */
class HistoryRenderPropertiesCalculatorLayer(
  /**
   * Is used to calculate the sampling period
   */
  var samplingPeriodCalculator: SamplingPeriodCalculator,
  /**
   * Calculates the gap size
   */
  val historyGapCalculator: HistoryGapCalculator,
  /**
   * Provides the content area time range
   */
  val contentAreaTimeRange: () -> @ContentArea TimeRange,

  ) : AbstractLayer() {
  override val type: LayerType = LayerType.Calculations

  override fun layout(paintingContext: LayerPaintingContext) {
    super.layout(paintingContext)

    val chartSupport = paintingContext.chartSupport
    val chartCalculator = paintingContext.chartCalculator

    //Calculate the ideal values for history
    val windowSize = chartSupport.currentChartState.windowSize

    val visibleTimeRange = chartCalculator.visibleTimeRangeXinWindow(contentAreaTimeRange())
    paintingContext.chartSupport.paintingProperties.store(PaintingPropertyKey.VisibleTimeRangeX, visibleTimeRange)

    val samplingPeriod = samplingPeriodCalculator.calculateSamplingPeriod(visibleTimeRange, windowSize)
    paintingContext.chartSupport.paintingProperties.store(PaintingPropertyKey.SamplingPeriod, samplingPeriod)

    @ms val calculateMinGapDistance = historyGapCalculator.calculateMinGapDistance(samplingPeriod)
    paintingContext.chartSupport.paintingProperties.store(PaintingPropertyKey.MinGapDistance, calculateMinGapDistance)

    if (false) {
      logger.debug { "Calculated samplingPeriod: $samplingPeriod" }
      logger.debug { "Calculated min gap distance: $calculateMinGapDistance" }
    }
  }

  override fun paint(paintingContext: LayerPaintingContext) {
    //do nothing
  }

  companion object {
    private val logger: Logger = LoggerFactory.getLogger("com.meistercharts.algorithms.tile.HistoryRenderPropertiesCalculatorLayer")
  }
}

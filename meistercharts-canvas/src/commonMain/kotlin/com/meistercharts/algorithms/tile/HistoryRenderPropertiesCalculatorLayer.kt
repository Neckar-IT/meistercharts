package com.meistercharts.algorithms.tile

import com.meistercharts.algorithms.TimeRange
import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.PaintingPropertyKey
import com.meistercharts.annotations.ContentArea
import com.meistercharts.canvas.paintingProperties
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

    //Calculate the ideal
    val windowSize = chartSupport.currentChartState.windowSize

    val visibleTimeRange = chartCalculator.visibleTimeRangeXinWindow(contentAreaTimeRange())
    paintingContext.chartSupport.paintingProperties.store(PaintingPropertyKey.VisibleTimeRangeX, visibleTimeRange)

    val samplingPeriod = samplingPeriodCalculator.calculateSamplingPeriod(visibleTimeRange, windowSize)
    paintingContext.chartSupport.paintingProperties.store(PaintingPropertyKey.SamplingPeriod, samplingPeriod)

    @ms val calculateMinGapDistance = historyGapCalculator.calculateMinGapDistance(samplingPeriod)
    paintingContext.chartSupport.paintingProperties.store(PaintingPropertyKey.MinGapDistance, calculateMinGapDistance)
  }

  override fun paint(paintingContext: LayerPaintingContext) {
    //do nothing
  }
}

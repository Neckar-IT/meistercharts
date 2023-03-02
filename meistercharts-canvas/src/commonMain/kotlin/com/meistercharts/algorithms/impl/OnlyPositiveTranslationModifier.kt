package com.meistercharts.algorithms.impl

import com.meistercharts.algorithms.ChartCalculator
import com.meistercharts.algorithms.ZoomAndTranslationModifier
import com.meistercharts.algorithms.axis.AxisOrientationX
import com.meistercharts.algorithms.axis.AxisOrientationY
import com.meistercharts.annotations.ContentArea
import com.meistercharts.model.Distance
import com.meistercharts.model.Zoom
import it.neckar.open.unit.other.px

/**
 * Limiter that ensures only positive values are visible
 * [Visualization](src/main/doc/translation/OnlyPositivePanLimiter.svg)
 *
 */
class OnlyPositiveTranslationModifier(val delegate: ZoomAndTranslationModifier) : ZoomAndTranslationModifier {
  /**
   * Modifies the min/max panning.
   * A visualization that describes the limits can be found in "DefaultPanLimiter.svg"
   */
  @ContentArea
  @px
  override fun modifyTranslation(@ContentArea @px translation: Distance, calculator: ChartCalculator): Distance {
    val minY = when (calculator.chartState.axisOrientationY) {
      AxisOrientationY.OriginAtTop -> calculator.contentAreaRelative2zoomedY(-1.0)
      AxisOrientationY.OriginAtBottom -> calculator.chartState.contentAreaHeight - calculator.contentAreaRelative2zoomedY(1.0)
    }

    val maxY = when (calculator.chartState.axisOrientationY) {
      AxisOrientationY.OriginAtTop -> 0.0
      AxisOrientationY.OriginAtBottom -> calculator.chartState.contentAreaHeight
    }


    val minX = when (calculator.chartState.axisOrientationX) {
      AxisOrientationX.OriginAtLeft  -> calculator.contentAreaRelative2zoomedX(-1.0)
      AxisOrientationX.OriginAtRight -> calculator.chartState.contentAreaWidth - calculator.contentAreaRelative2zoomedX(1.0)
    }

    val maxX = when (calculator.chartState.axisOrientationX) {
      AxisOrientationX.OriginAtLeft  -> 0.0
      AxisOrientationX.OriginAtRight -> calculator.chartState.contentAreaWidth
    }

    return delegate.modifyTranslation(translation, calculator)
      .withMin(minX, minY)
      .withMax(maxX, maxY)
  }

  override fun modifyZoom(zoom: Zoom, calculator: ChartCalculator): Zoom {
    return delegate.modifyZoom(zoom, calculator)
  }
}

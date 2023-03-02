package com.meistercharts.algorithms.impl

import com.meistercharts.algorithms.ChartCalculator
import com.meistercharts.algorithms.ZoomAndTranslationModifier
import com.meistercharts.annotations.ContentArea
import com.meistercharts.model.Distance
import com.meistercharts.model.Zoom

/**
 * Ensures a max zoom level
 *
 */
class MinZoomModifier(
  private val minZoomFactorX: Double,
  private val minZoomFactorY: Double,
  private val delegate: ZoomAndTranslationModifier
) :
  ZoomAndTranslationModifier {
  @ContentArea
  override fun modifyTranslation(@ContentArea translation: Distance, calculator: ChartCalculator): Distance {
    return delegate.modifyTranslation(translation, calculator)
  }

  override fun modifyZoom(zoom: Zoom, calculator: ChartCalculator): Zoom {
    return delegate.modifyZoom(zoom, calculator)
      .withMin(minZoomFactorX, minZoomFactorY)
  }

}

package com.meistercharts.algorithms.impl

import com.meistercharts.algorithms.ChartCalculator
import com.meistercharts.algorithms.ZoomAndTranslationModifier
import com.meistercharts.annotations.ContentArea
import com.meistercharts.model.Distance
import com.meistercharts.model.Zoom
import it.neckar.open.provider.DoubleProvider

/**
 * Ensures a max zoom level
 *
 */
class MaxZoomModifier(
  private var maxZoomFactorX: DoubleProvider,
  private var maxZoomFactorY: DoubleProvider,
  private val delegate: ZoomAndTranslationModifier,
) : ZoomAndTranslationModifier {

  constructor(
    maxZoomFactorX: Double,
    maxZoomFactorY: Double,
    delegate: ZoomAndTranslationModifier
  ) : this({ maxZoomFactorX }, { maxZoomFactorY }, delegate)

  @ContentArea
  override fun modifyTranslation(@ContentArea translation: Distance, calculator: ChartCalculator): Distance {
    return delegate.modifyTranslation(translation, calculator)
  }

  override fun modifyZoom(zoom: Zoom, calculator: ChartCalculator): Zoom {
    return delegate.modifyZoom(zoom, calculator)
      .withMax(maxZoomFactorX(), maxZoomFactorY())
  }
}

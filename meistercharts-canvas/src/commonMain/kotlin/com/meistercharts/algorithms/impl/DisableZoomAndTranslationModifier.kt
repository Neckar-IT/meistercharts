package com.meistercharts.algorithms.impl

import com.meistercharts.algorithms.ChartCalculator
import com.meistercharts.algorithms.ZoomAndTranslationModifier
import com.meistercharts.annotations.ContentArea
import com.meistercharts.model.Distance
import com.meistercharts.model.Zoom

/**
 * Disables panning and zooming
 */
class DisableZoomAndTranslationModifier(private val delegate: ZoomAndTranslationModifier) : ZoomAndTranslationModifier {
  @ContentArea
  override fun modifyTranslation(@ContentArea translation: Distance, calculator: ChartCalculator): Distance {
    return delegate.modifyTranslation(translation, calculator).withX(0.0).withY(0.0)
  }

  override fun modifyZoom(zoom: Zoom, calculator: ChartCalculator): Zoom {
    return delegate.modifyZoom(zoom, calculator).withY(1.0).withX(1.0)
  }
}

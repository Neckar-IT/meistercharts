package com.meistercharts.algorithms.impl

import com.meistercharts.algorithms.ChartCalculator
import com.meistercharts.algorithms.ZoomAndTranslationModifier
import com.meistercharts.annotations.ContentArea
import com.meistercharts.model.Distance
import com.meistercharts.model.Zoom

/**
 * Disables zooming
 */
class DisableZoomModifier(private val delegate: ZoomAndTranslationModifier) : ZoomAndTranslationModifier {
  override fun modifyTranslation(translation: Distance, calculator: ChartCalculator): Distance {
    return delegate.modifyTranslation(translation, calculator)
  }

  override fun modifyZoom(zoom: Zoom, calculator: ChartCalculator): Zoom {
    return delegate.modifyZoom(zoom, calculator).withY(1.0).withX(1.0)
  }
}

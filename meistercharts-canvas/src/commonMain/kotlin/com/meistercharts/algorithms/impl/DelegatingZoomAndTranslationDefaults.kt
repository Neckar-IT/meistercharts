package com.meistercharts.algorithms.impl

import com.meistercharts.algorithms.ChartCalculator
import com.meistercharts.model.Distance
import com.meistercharts.model.Zoom

/**
 * Delegates each axis to a delegate
 */
class DelegatingZoomAndTranslationDefaults(
  /**
   * the delegate that is used to calculate the defaults for the x axis
   */
  val xAxisDelegate: ZoomAndTranslationDefaults,
  /**
   * the delegate that is used to calculate the defaults for the y axis
   */
  val yAxisDelegate: ZoomAndTranslationDefaults
) : ZoomAndTranslationDefaults {

  override fun defaultZoom(chartCalculator: ChartCalculator): Zoom {
    return Zoom.of(
      xAxisDelegate.defaultZoom(chartCalculator).scaleX,
      yAxisDelegate.defaultZoom(chartCalculator).scaleY
    )
  }

  override fun defaultTranslation(chartCalculator: ChartCalculator): Distance {
    return Distance.of(
      xAxisDelegate.defaultTranslation(chartCalculator).x,
      yAxisDelegate.defaultTranslation(chartCalculator).y
    )
  }
}

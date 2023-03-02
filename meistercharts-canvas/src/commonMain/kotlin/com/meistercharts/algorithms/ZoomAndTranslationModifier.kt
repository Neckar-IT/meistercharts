package com.meistercharts.algorithms

import com.meistercharts.annotations.ContentArea
import com.meistercharts.model.Distance
import com.meistercharts.model.Zoom
import it.neckar.open.unit.other.px

/**
 * Modifies/limits the panning and zooming.
 *
 * The implementation can be used to keep the user within a reasonable area.
 *
 * It is suggested to use a [com.meistercharts.algorithms.impl.ZoomAndTranslationModifiersBuilder]
 * to instantiate a new ZoomAndPanModifier.
 *
 */
interface ZoomAndTranslationModifier {
  /**
   * Returns the updated translation
   */
  fun modifyTranslation(@ContentArea @px translation: Distance, calculator: ChartCalculator): @ContentArea Distance

  /**
   * Modifies the zoom.
   */
  fun modifyZoom(zoom: Zoom, calculator: ChartCalculator): Zoom

  companion object {
    /**
     * Default implementation that doesn't modify anything
     */
    val none: ZoomAndTranslationModifier = object : ZoomAndTranslationModifier {
      override fun modifyTranslation(@ContentArea @px translation: Distance, calculator: ChartCalculator): @ContentArea Distance {
        return translation
      }

      override fun modifyZoom(zoom: Zoom, calculator: ChartCalculator): Zoom {
        return zoom
      }
    }
  }
}

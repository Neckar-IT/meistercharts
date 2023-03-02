package com.meistercharts.algorithms.impl

import com.meistercharts.algorithms.ChartCalculator
import com.meistercharts.algorithms.ZoomAndTranslationModifier
import com.meistercharts.annotations.ContentArea
import com.meistercharts.annotations.Zoomed
import com.meistercharts.model.Distance
import com.meistercharts.model.Zoom
import it.neckar.open.unit.other.px

/**
 * Default implementations that limits the panning depending on the zoom level.
 * This modifier ensures the content area is always barely visible when panned.
 *
 * See "[internal/closed/charting/meistercharts-canvas/doc/translation/ContentAlwaysBarelyVisible.svg] for a visualization how this class works.
 *
 */
class ContentAreaAlwaysBarelyVisibleTranslationModifier(
  val delegate: ZoomAndTranslationModifier
) : ZoomAndTranslationModifier {
  /**
   * Modifies the min/max panning.
   * A visualization that describes the limits can be found in "DefaultPanLimiter.svg"
   */
  @ContentArea
  @px
  override fun modifyTranslation(@ContentArea @px translation: Distance, calculator: ChartCalculator): Distance {
    @Zoomed val minX = calculator.contentAreaRelative2zoomedX(-1.0)
    @Zoomed val minY = calculator.contentAreaRelative2zoomedY(-1.0)

    @ContentArea val maxX = calculator.chartState.contentAreaWidth
    @ContentArea val maxY = calculator.chartState.contentAreaHeight

    return delegate.modifyTranslation(translation, calculator)
      .withMin(minX, minY)
      .withMax(maxX, maxY)
  }

  override fun modifyZoom(zoom: Zoom, calculator: ChartCalculator): Zoom {
    return delegate.modifyZoom(zoom, calculator)
  }
}

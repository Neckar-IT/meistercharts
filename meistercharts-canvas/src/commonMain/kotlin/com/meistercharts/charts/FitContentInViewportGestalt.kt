package com.meistercharts.charts

import com.meistercharts.annotations.Zoomed
import com.meistercharts.canvas.BindContentAreaSize2ContentViewport
import com.meistercharts.canvas.MeisterChartBuilder
import com.meistercharts.model.Insets

/**
 * Binds the content area to the window size
 */
open class FitContentInViewportGestalt(contentViewportMargin: @Zoomed Insets = Insets.empty) : ContentViewportGestalt(contentViewportMargin) {
  override fun configure(meisterChartBuilder: MeisterChartBuilder) {
    super.configure(meisterChartBuilder)

    meisterChartBuilder.apply {
      contentAreaSizingStrategy = BindContentAreaSize2ContentViewport()
    }
  }
}

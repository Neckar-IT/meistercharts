package com.meistercharts.charts

import com.meistercharts.algorithms.ResetToDefaultsOnWindowResize
import com.meistercharts.annotations.Zoomed
import com.meistercharts.canvas.MeisterChartBuilder
import com.meistercharts.model.Insets

/**
 * Configures a chart to have a fixed size and zoom
 */
class FixedChartGestalt(contentViewportMargin: @Zoomed Insets = Insets.empty) : FitContentInViewportGestalt(contentViewportMargin) {
  override fun configure(meisterChartBuilder: MeisterChartBuilder) {
    super.configure(meisterChartBuilder)

    meisterChartBuilder.apply {
      enableZoomAndTranslation = false

      configure {
        chartSupport.windowResizeBehavior = ResetToDefaultsOnWindowResize
      }
    }
  }
}

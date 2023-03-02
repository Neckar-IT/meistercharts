package com.meistercharts.charts

import com.meistercharts.algorithms.layers.debug.addVersionNumberHidden
import com.meistercharts.canvas.MeisterChartBuilder

/**
 * Adds a version number layer
 */
class VersionNumberGestalt : ChartGestalt {
  override fun configure(meisterChartBuilder: MeisterChartBuilder) {
    meisterChartBuilder.configure {
      layers.addVersionNumberHidden()
    }
  }
}

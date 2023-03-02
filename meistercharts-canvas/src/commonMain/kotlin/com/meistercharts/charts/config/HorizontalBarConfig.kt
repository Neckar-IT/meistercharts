package com.meistercharts.charts.config

import com.meistercharts.algorithms.painter.Color
import com.meistercharts.charts.BarChartStackedGestalt

/**
 * Configuration for a horizontal bar
 */
class HorizontalBarConfig {
  fun apply(gestalt: BarChartStackedGestalt) {
    gestalt.style.applyHorizontalConfiguration()
    gestalt.stackedBarsPainter.stackedBarPaintable.style.backgroundColor = Color.gray
  }

}

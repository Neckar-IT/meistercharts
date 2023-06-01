package com.meistercharts.js

import com.meistercharts.charts.ChartId

/**
 * Contains the CSS classes used in meistercharts
 */
object MeisterChartClasses {
  const val canvas: String = "meistercharts-canvas"
  const val nativeComponentsHolder: String = "meistercharts-native-components"

  /**
   * Class name for the holder of the chart
   */
  const val holder: String = "meistercharts"

  /**
   * Used for image loader elements
   */
  const val imageLoader: String = "meistercharts-image-loader"

  fun chartId(chartId: ChartId): String {
    return "meistercharts-id-${chartId.id}"
  }
}

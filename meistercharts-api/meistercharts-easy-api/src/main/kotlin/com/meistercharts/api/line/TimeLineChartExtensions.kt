package com.meistercharts.api.line

import com.meistercharts.algorithms.layers.ValueAxisHudLayer
import com.meistercharts.charts.timeline.TimeLineChartGestalt
import com.meistercharts.history.DecimalDataSeriesIndex
import it.neckar.open.provider.MultiDoublesProvider

/**
 * Applies the default configuration for SICK
 */
fun TimeLineChartGestalt.applySickDefaults() {
  this.style.applyValueAxisTitleOnTop()
  thresholdsSupport.configuration.hudLayerConfiguration = { _: DecimalDataSeriesIndex, axis: ValueAxisHudLayer ->
  }
}

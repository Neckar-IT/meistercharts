package com.meistercharts.charts

import com.meistercharts.canvas.MeisterChartBuilder

/**
 * Each implementation contains a set of options/configurations that can be applied to a [com.meistercharts.canvas.MeisterChartBuilder].
 *
 * Each Gestalt combines one or more features (e.g. layers) and offers the API to the end user.
 *
 */
interface ChartGestalt {

  /**
   * Applies this configuration to the given builder
   */
  @ChartGestaltConfiguration
  fun configure(meisterChartBuilder: MeisterChartBuilder)
}

@DslMarker
annotation class ChartGestaltConfiguration

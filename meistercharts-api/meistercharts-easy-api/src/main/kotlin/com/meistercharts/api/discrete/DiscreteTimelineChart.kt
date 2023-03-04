package com.meistercharts.api.discrete

import com.meistercharts.api.MeisterChartsApi
import com.meistercharts.charts.refs.DiscreteTimelineChartGestalt
import com.meistercharts.js.MeisterChartJS

/**
 * Timeline chart that visualizes discrete timelines.
 */
@JsExport
class DiscreteTimelineChart(
  internal val gestalt: DiscreteTimelineChartGestalt,

  /**
   * The meister charts object. Can be used to call markAsDirty and dispose
   */
  meisterChart: MeisterChartJS,
) : MeisterChartsApi<DiscreteTimelinechartConfiguration>(meisterChart) {

  init {
    gestalt.applySickDefaults()
  }

  override fun setConfiguration(jsConfiguration: DiscreteTimelinechartConfiguration) {
    gestalt.applyConfiguration(jsConfiguration)
    markAsDirty()
  }
}

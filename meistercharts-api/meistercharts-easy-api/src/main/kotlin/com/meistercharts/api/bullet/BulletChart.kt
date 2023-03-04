package com.meistercharts.api.bullet

import com.meistercharts.api.MeisterChartsApi
import com.meistercharts.charts.bullet.BulletChartGestalt
import com.meistercharts.js.MeisterChartJS

/**
 * The api towards the browser that supports the creation and manipulation of a bullet chart
 */
@JsExport
class BulletChart internal constructor(
  /**
   * The gestalt that is configured
   */
  internal val gestalt: BulletChartGestalt,

  meisterChart: MeisterChartJS,
) : MeisterChartsApi<BulletChartConfiguration>(meisterChart) {

  init {
    gestalt.applySickDefaults()
  }

  override fun setConfiguration(jsConfiguration: BulletChartConfiguration) {
    gestalt.applyConfiguration(jsConfiguration)
    markAsDirty()
  }
}

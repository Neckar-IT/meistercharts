package com.meistercharts.api.map

import com.meistercharts.api.MeisterChartsApiLegacy
import com.meistercharts.charts.MapWithPaintablesGestalt
import com.meistercharts.js.MeisterChartJS
import it.neckar.logging.LoggerFactory

/**
 * A map with stacked bars
 */
@JsExport
class MapWithStackedBars internal constructor(
  internal val gestalt: MapWithPaintablesGestalt,
  meisterChart: MeisterChartJS,
) : MeisterChartsApiLegacy<MapWithBarsData, MapWithBarsStyle>(meisterChart) {

  init {
    gestalt.applySickDefaults()
  }

  override fun setData(jsData: MapWithBarsData) {
    // TODO
    logger.warn("setData is not implemented yet")
  }

  override fun setStyle(jsStyle: MapWithBarsStyle) {
    // TODO
    logger.warn("setStyle is not implemented yet")

  }

  companion object {
    private val logger = LoggerFactory.getLogger("com.meistercharts.api.map.MapWithStackedBars")
  }
}

package com.meistercharts.api.compass

import com.meistercharts.api.MeisterChartsApiLegacy
import com.meistercharts.charts.PuristicCompassGestalt
import com.meistercharts.js.MeisterChartJS
import it.neckar.open.provider.DoubleProvider

/**
 * The api towards the browser that supports the creation and manipulation of a compass
 */
@JsExport
class Compass internal constructor(
  internal val gestalt: PuristicCompassGestalt,

  meisterChart: MeisterChartJS,
) : MeisterChartsApiLegacy<CompassData, CompassStyle>(meisterChart) {

  init {
    gestalt.applySickDefaults()
  }

  override fun setData(jsData: CompassData) {
    gestalt.data.currentValueProvider = DoubleProvider { jsData.currentValue ?: 0.0 }
    gestalt.subValueLayer.data.linesProvider = { _, _ ->
      listOf(
        jsData.labelLatitude.orEmpty(),
        jsData.labelLongitude.orEmpty()
      )
    }

    markAsDirty()
  }

  override fun setStyle(jsStyle: CompassStyle) {
    // TODO
    markAsDirty()
  }
}

/**
 * External configuration that can be used to configure the data
 */
external interface CompassData {
  // This is the interface towards the browser. There is no guaranty that we will receive non-null and well-defined values. Hence, all types are nullable.
  /**
   * The current value in degrees
   */
  val currentValue: Double?

  /**
   * The text for the latitude
   */
  val labelLatitude: String?

  /**
   * The text for the longitude
   */
  val labelLongitude: String?
}

/**
 * External configuration that can be used to configure the style
 */
external interface CompassStyle {

}

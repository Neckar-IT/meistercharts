package com.meistercharts.api

import com.meistercharts.js.MeisterChartJS

/**
 * The main API for MeisterCharts.
 * Call dispose when the component is no longer used.
 */
@JsExport
abstract class MeisterChartsApiLegacy<DataType, StyleType>
internal constructor(
  /**
   * The meister charts object. Can be used to call markAsDirty and dispose
   */
  meisterChart: MeisterChartJS,

  ) : MeisterChartsApi<Nothing>(meisterChart) {

  /**
   * Sets the data that is displayed
   */
  @JsName("setData")
  abstract fun setData(jsData: DataType)

  /**
   * Sets the style for the component
   */
  @JsName("setStyle")
  abstract fun setStyle(jsStyle: StyleType)

  @Deprecated("Not supported by MeisterChartsApiLegacy. Extend MeisterChartsApi directly", level = DeprecationLevel.ERROR)
  override fun setConfiguration(jsConfiguration: Nothing) {
    throw UnsupportedOperationException("")
  }
}

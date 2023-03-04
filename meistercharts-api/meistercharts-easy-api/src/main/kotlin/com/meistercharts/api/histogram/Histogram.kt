package com.meistercharts.api.histogram

import com.meistercharts.api.MeisterChartsApiLegacy
import com.meistercharts.api.bar.applyStyle
import com.meistercharts.api.category.CategoryConverter
import com.meistercharts.charts.HistogramGestalt
import com.meistercharts.js.MeisterChartJS

/**
 * The api towards the browser that supports the creation and manipulation of a histogram
 */
@JsExport
class Histogram internal constructor(
  internal val gestalt: HistogramGestalt,
  meisterChart: MeisterChartJS,
) : MeisterChartsApiLegacy<HistogramData, HistogramStyle>(meisterChart) {

  init {
    gestalt.applySickDefaults()
  }

  override fun setData(jsData: HistogramData) {
    CategoryConverter.toCategoryModel(jsData)?.let {
      gestalt.configuration.categorySeriesModel = it
    }

    markAsDirty()
  }

  override fun setStyle(jsStyle: HistogramStyle) {
    gestalt.barChartGroupedGestalt.applyStyle(jsStyle)

    if (jsStyle.barSize != null) {
      console.warn("HistogramStyle does not support barSize")
    }

    //bar size is calculated in [HistogramGestalt]
    gestalt.barChartGroupedGestalt.groupedBarsPainter.configuration.setBarSizeRange(1.0, 200.0)

    markAsDirty()
  }

}

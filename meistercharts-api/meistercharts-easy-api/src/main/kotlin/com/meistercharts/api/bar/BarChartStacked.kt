package com.meistercharts.api.bar

import com.meistercharts.charts.BarChartStackedGestalt
import com.meistercharts.js.MeisterChartJS
import it.neckar.open.provider.MultiProvider
import com.meistercharts.api.MeisterChartsApiLegacy
import com.meistercharts.api.category.CategoryConverter
import com.meistercharts.api.setImagesProvider

/**
 * The api towards the browser that supports the creation and manipulation of a bar chart with stacked bars
 */
@JsExport
class BarChartStacked internal constructor(
  /**
   * The gestalt that is configured
   */
  private val gestalt: BarChartStackedGestalt,

  meisterChart: MeisterChartJS,
) : MeisterChartsApiLegacy<BarChartData, BarChartStackedStyle>(meisterChart) {

  init {
    gestalt.applySickDefaults()
  }

  /**
   * Sets and replaces the data of the bar chart model
   */
  override fun setData(jsData: BarChartData) {
    CategoryConverter.toCategoryModel(jsData)?.let {
      gestalt.data.categorySeriesModel = it
    }

    CategoryConverter.toCategoryImages(jsData)?.let { images ->
      gestalt.categoryAxisLayer.style.axisLabelPainter.setImagesProvider(MultiProvider.forListOrNull(images))
    }

    markAsDirty()
  }

  /**
   * Sets the style
   */
  override fun setStyle(jsStyle: BarChartStackedStyle) {
    gestalt.applyStyle(jsStyle)

    markAsDirty()
  }
}



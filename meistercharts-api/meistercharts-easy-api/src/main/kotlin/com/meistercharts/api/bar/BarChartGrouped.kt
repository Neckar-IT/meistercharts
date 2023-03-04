package com.meistercharts.api.bar

import com.meistercharts.api.MeisterChartsApiLegacy
import com.meistercharts.api.category.CategoryConverter
import com.meistercharts.api.setImagesProvider
import com.meistercharts.charts.BarChartGroupedGestalt
import com.meistercharts.js.MeisterChartJS
import it.neckar.open.provider.MultiProvider
import it.neckar.commons.kotlin.js.debug
import it.neckar.logging.Logger
import it.neckar.logging.LoggerFactory
import it.neckar.logging.ifDebug

/**
 * The api towards the browser that supports the creation and manipulation of a bar chart with grouped bars
 */
@JsExport
class BarChartGrouped internal constructor(
  /**
   * The gestalt that is configured
   */
  internal val gestalt: BarChartGroupedGestalt,

  meisterChart: MeisterChartJS,
) : MeisterChartsApiLegacy<BarChartGroupedData, BarChartGroupedStyle>(meisterChart) {

  init {
    gestalt.applySickDefaults()
  }

  /**
   * Sets and replaces the data of the bar chart model
   */
  override fun setData(jsData: BarChartGroupedData) {
    logger.ifDebug {
      console.debug("BarChartGrouped.setData", jsData)
    }

    CategoryConverter.toCategoryModel(jsData)?.let {
      gestalt.configuration.categorySeriesModel = it
    }

    CategoryConverter.toCategoryImages(jsData)?.let { images ->
      gestalt.categoryAxisLayer.style.axisLabelPainter.setImagesProvider(MultiProvider.forListOrNull(images))
    }

    markAsDirty()
  }

  /**
   * Sets the style
   */
  override fun setStyle(jsStyle: BarChartGroupedStyle) {
    gestalt.applyStyle(jsStyle)
    markAsDirty()
  }

  companion object {
    val logger: Logger = LoggerFactory.getLogger("com.meistercharts.api.bar.BarChartGrouped")
  }
}



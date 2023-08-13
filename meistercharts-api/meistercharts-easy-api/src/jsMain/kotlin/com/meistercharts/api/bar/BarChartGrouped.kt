/**
 * Copyright 2023 Neckar IT GmbH, Mössingen, Germany
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.meistercharts.api.bar

import com.meistercharts.api.MeisterChartsApiLegacy
import com.meistercharts.api.category.CategoryConverter
import com.meistercharts.api.setImagesProvider
import com.meistercharts.charts.BarChartGroupedGestalt
import com.meistercharts.js.MeisterchartJS
import it.neckar.logging.Logger
import it.neckar.logging.LoggerFactory
import it.neckar.open.provider.MultiProvider

/**
 * The api towards the browser that supports the creation and manipulation of a bar chart with grouped bars
 */
@JsExport
class BarChartGrouped internal constructor(
  /**
   * The gestalt that is configured
   */
  internal val gestalt: BarChartGroupedGestalt,

  meisterChart: MeisterchartJS,
) : MeisterChartsApiLegacy<BarChartGroupedData, BarChartGroupedStyle>(meisterChart) {

  init {
    gestalt.applyEasyApiDefaults()
  }

  /**
   * Sets and replaces the data of the bar chart model
   */
  override fun setData(jsData: BarChartGroupedData) {
    logger.debug("BarChartGrouped.setData", jsData)

    CategoryConverter.toCategoryModel(jsData)?.let {
      gestalt.configuration.categorySeriesModel = it
    }

    CategoryConverter.toCategoryImages(jsData)?.let { images ->
      gestalt.categoryAxisLayer.axisConfiguration.axisLabelPainter.setImagesProvider(MultiProvider.forListOrNull(images))
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
    internal val logger: Logger = LoggerFactory.getLogger("com.meistercharts.api.bar.BarChartGrouped")
  }
}



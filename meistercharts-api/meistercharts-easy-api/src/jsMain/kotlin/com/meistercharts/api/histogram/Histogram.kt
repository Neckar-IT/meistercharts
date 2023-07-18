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
package com.meistercharts.api.histogram

import com.meistercharts.api.MeisterChartsApiLegacy
import com.meistercharts.api.bar.applyStyle
import com.meistercharts.api.category.CategoryConverter
import com.meistercharts.charts.HistogramGestalt
import com.meistercharts.js.MeisterchartJS

/**
 * The api towards the browser that supports the creation and manipulation of a histogram
 */
@JsExport
class Histogram internal constructor(
  internal val gestalt: HistogramGestalt,
  meisterChart: MeisterchartJS,
) : MeisterChartsApiLegacy<HistogramData, HistogramStyle>(meisterChart) {

  init {
    gestalt.applyEasyApiDefaults()
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

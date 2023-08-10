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
package com.meistercharts.api.line

import com.meistercharts.api.MeisterChartsApiLegacy
import com.meistercharts.api.category.CategoryConverter
import com.meistercharts.api.category.applyEasyApiDefaults
import com.meistercharts.api.category.applyStyle
import com.meistercharts.api.setImagesProvider
import com.meistercharts.charts.CategoryLineChartGestalt
import com.meistercharts.js.MeisterchartJS
import it.neckar.open.provider.MultiProvider

/**
 * Simple line chart
 */
@JsExport
class LineChartSimple internal constructor(
  internal val gestalt: CategoryLineChartGestalt,
  meisterChart: MeisterchartJS,
) : MeisterChartsApiLegacy<LineChartSimpleData, LineChartSimpleStyle>(meisterChart) {

  init {
    gestalt.applyEasyApiDefaults()
  }

  override fun setData(jsData: LineChartSimpleData) {
    CategoryConverter.toCategoryModel(jsData)?.let {
      gestalt.configuration.categorySeriesModel = it
    }

    CategoryConverter.toCategoryImages(jsData)?.let { images ->
      gestalt.categoryAxisLayer.axisConfiguration.axisLabelPainter.setImagesProvider(MultiProvider.forListOrNull(images))
    }

    markAsDirty()
  }

  override fun setStyle(jsStyle: LineChartSimpleStyle) {
    gestalt.applyStyle(jsStyle)

    markAsDirty()
  }
}

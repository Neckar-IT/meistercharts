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
package com.meistercharts.charts

import com.meistercharts.algorithms.layers.ValueAxisLayer
import com.meistercharts.algorithms.layers.AxisTopTopTitleLayer
import com.meistercharts.algorithms.layers.barchart.CategoryAxisLayer
import com.meistercharts.algorithms.layers.barchart.CategoryLayer
import com.meistercharts.algorithms.layers.barchart.GreedyCategoryAxisLabelPainter
import com.meistercharts.algorithms.layers.barchart.GroupedBarsPainter
import com.meistercharts.algorithms.layout.EquisizedBoxLayout
import com.meistercharts.algorithms.layout.LayoutDirection
import com.meistercharts.model.category.CategorySeriesModel
import com.meistercharts.canvas.MeisterchartBuilder
import com.meistercharts.canvas.SnapConfiguration
import com.meistercharts.canvas.pixelSnapSupport
import com.meistercharts.model.Insets
import it.neckar.open.unit.other.px

/**
 * Paints a histogram
 */
class HistogramGestalt(
  /**
   * The current category model for the stacked bar chart
   */
  categorySeriesModel: CategorySeriesModel = BarChartGroupedGestalt.createDefaultCategoryModel(),
  /**
   * The tooltip type
   */
  toolTipType: ToolTipType = ToolTipType.Balloon,

  styleConfiguration: BarChartGroupedGestalt.Style.() -> Unit = {},
) : ChartGestalt {

  val barChartGroupedGestalt: BarChartGroupedGestalt = BarChartGroupedGestalt(categorySeriesModel, toolTipType, styleConfiguration).also {
    it.style.activeCategoryBackgroundMargin = Insets.all2
  }

  val configuration: BarChartGroupedGestalt.Configuration by barChartGroupedGestalt::configuration

  var contentViewportMargin: Insets by barChartGroupedGestalt::contentViewportMargin

  init {
    barChartGroupedGestalt.groupedBarsPainter.configuration.layoutProvider = { categorySize: @px Double, model, _ ->
      require(model.numberOfSeries <= 1) {
        "Invalid number of series. Was <${model.numberOfSeries}> but expected <1>"
      }

      @px val boxSize = when {
        categorySize < 6 -> {
          categorySize.coerceAtLeast(1.0)
        }

        else -> {
          categorySize - 2.0
        }
      }

      EquisizedBoxLayout(categorySize, 1, boxSize, 0.0, LayoutDirection.CenterHorizontal)
    }

    //usually there are many, many values - therefore the greedy category axis label painter is better suited
    barChartGroupedGestalt.categoryAxisLayer.style.axisLabelPainter = GreedyCategoryAxisLabelPainter()

    //Disable the overflow indicator painter - not enough room
    barChartGroupedGestalt.groupedBarsPainter.configuration.overflowIndicatorPainter = null
  }

  override fun configure(meisterChartBuilder: MeisterchartBuilder) {
    barChartGroupedGestalt.configure(meisterChartBuilder)

    //Additional configuration for the histogram
    meisterChartBuilder.configure {
      //Enable snap configuration
      chartSupport.pixelSnapSupport.snapConfiguration = SnapConfiguration.None
    }
  }

  val valueAxisLayer: ValueAxisLayer
    get() {
      return barChartGroupedGestalt.valueAxisLayer
    }

  val valueAxisTopTitleLayer: AxisTopTopTitleLayer
    get() {
      return barChartGroupedGestalt.valueAxisTopTitleLayer
    }

  val categoryLayer: CategoryLayer<CategorySeriesModel>
    get() {
      return barChartGroupedGestalt.categoryLayer
    }

  val categoryAxisLayer: CategoryAxisLayer
    get() {
      return barChartGroupedGestalt.categoryAxisLayer
    }

  val groupedBarsPainter: GroupedBarsPainter
    get() {
      return barChartGroupedGestalt.groupedBarsPainter
    }

  /**
   * Returns the style of the bar chart grouped gestalt
   */
  val style: BarChartGroupedGestalt.Style
    get() {
      return barChartGroupedGestalt.style
    }
}

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
package com.meistercharts.demo.descriptors

import com.meistercharts.algorithms.layers.linechart.LineStyle
import com.meistercharts.algorithms.model.Category
import com.meistercharts.algorithms.model.DefaultCategorySeriesModel
import com.meistercharts.algorithms.model.DefaultSeries
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.charts.CategoryLineChartGestalt
import com.meistercharts.charts.ToolTipType
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableBoolean
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.configurableFont
import com.meistercharts.painter.DotCategoryPointPainter
import it.neckar.open.collections.fastForEachIndexed
import it.neckar.open.kotlin.lang.fastMap
import it.neckar.open.provider.MultiProvider
import it.neckar.open.i18n.TextKey
import com.meistercharts.style.BoxStyle

class CategoryLineChartBalloonTooltipDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Category Line Chart Balloon Tooltips"
  override val category: DemoCategory = DemoCategory.Gestalt

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      val gestalt = CategoryLineChartGestalt(
        categorySeriesModel = DefaultCategorySeriesModel(
          listOf(
            Category(TextKey.simple("Monkeys")),
            Category(TextKey.simple("Giraffes")),
            Category(TextKey.simple("Parrots")),
            Category(TextKey.simple("")),
            Category(TextKey.simple("Tigers")),
          ),
          listOf(
            DefaultSeries("Chicago", listOf(10.0, 20.0, 30.0, 40.0, 50.0)),
            DefaultSeries("Paris", listOf(50.0, 10.0, 30.0, 80.0, 0.0)),
            DefaultSeries("Berlin", listOf(50.0, 100.0, 10.0, 10.0, 40.0)),
            DefaultSeries("Oslo", listOf(20.0, 50.0, 0.0, 100.0, 80.0)),
            DefaultSeries("Warsow", listOf(70.0, 60.0, 50.0, 40.0, 60.0)),
          )
        ),

        toolTipType = ToolTipType.Balloon
      )

      val colors = listOf(Color.orangered, Color.blueviolet, Color.greenyellow, Color.limegreen, Color.chocolate)
      gestalt.categoryLinesLayer.style.lineStyles = MultiProvider.forListModulo(colors.map { color -> LineStyle(color) })
      gestalt.crossWireLabelsLayer.style.valueLabelBoxStyle = MultiProvider.forListModulo(colors.map { color -> BoxStyle(color) })
      gestalt.categoryLinesLayer.style.pointPainters = MultiProvider.forListModulo(colors.map { color -> DotCategoryPointPainter(snapXValues = false, snapYValues = false).apply { pointStylePainter.color = color } })

      meistercharts {
        gestalt.configure(this)

        configure {
          configurableDouble("Min category size", gestalt.configuration::minCategorySize) {
            max = 200.0
          }

          configurableDouble("Max category size", gestalt.configuration::maxCategorySize, 150.0) {
            max = 200.0
          }

          val visibleLineIndices = 5.fastMap { lineIndex -> gestalt.configuration.lineIsVisible.valueAt(lineIndex) }.toMutableList()
          gestalt.configuration.lineIsVisible = MultiProvider.forListModulo(visibleLineIndices)
          visibleLineIndices.fastForEachIndexed { index, isVisible ->
            configurableBoolean("$index. line is visible") {
              value = isVisible
              onChange {
                visibleLineIndices[index] = it
                markAsDirty()
              }
            }
          }

          configurableDouble("Category gap", gestalt.configuration::categoryGap) {
            max = 200.0
          }

          configurableFont("Axis tick font", gestalt.valueAxisLayer.style.tickFont) {
            onChange {
              gestalt.applyAxisTickFont(it)
              markAsDirty()
            }
          }

          configurableFont("Axis title font", gestalt.valueAxisLayer.style.titleFont) {
            onChange {
              gestalt.applyAxisTitleFont(it)
              markAsDirty()
            }
          }

          configurableFont("Cross wire label font", gestalt.crossWireLabelsLayer.style::valueLabelFont) {
          }
        }
      }
    }
  }

}



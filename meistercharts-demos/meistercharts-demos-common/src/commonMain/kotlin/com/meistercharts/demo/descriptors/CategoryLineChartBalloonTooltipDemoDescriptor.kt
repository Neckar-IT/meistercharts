/*
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

import com.meistercharts.demo.configurable
import kotlin.uuid.Uuid
import com.meistercharts.algorithms.layers.linechart.LineStyle
import com.meistercharts.charts.CategoryLineChartGestalt
import com.meistercharts.charts.ToolTipType
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.VariabilityType
import com.meistercharts.demo.configurableBoolean
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.configurableFont
import com.meistercharts.demo.configurableFontProvider
import com.meistercharts.design.Theme
import com.meistercharts.model.category.Category
import com.meistercharts.model.category.DefaultCategorySeriesModel
import com.meistercharts.model.category.DefaultSeries
import com.meistercharts.painter.DotCategoryPointPainter
import com.meistercharts.style.BoxStyle
import it.neckar.open.collections.fastForEachIndexed
import it.neckar.open.i18n.TextKey
import it.neckar.open.kotlin.lang.asProvider
import it.neckar.open.kotlin.lang.fastMap
import it.neckar.open.provider.MultiProvider
import it.neckar.open.provider.mapped

class CategoryLineChartBalloonTooltipDemoDescriptor : MeisterchartsDemoDescriptor<Nothing> {
  override val uuid: Uuid = Uuid.parse("5ff1cc52-059f-4082-8a53-5c74b1b039c9")
  override val name: String = "Category Line Chart Balloon Tooltips"
  override val category: DemoCategory = DemoCategory.Gestalt

  override val quality: DemoQuality = DemoQuality.High
  override val variabilityType: VariabilityType = VariabilityType.Stable

  override fun prepareDemo(configuration: PredefinedConfiguration<Nothing>?): MeisterchartsDemo {
    return MeisterchartsDemo(this) {
      val gestalt = CategoryLineChartGestalt(
        initialCategorySeriesModel = DefaultCategorySeriesModel(
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

        initialToolTipType = ToolTipType.Balloon
      )

      val colors = Theme.chartColors.resolve()
      gestalt.categoryLinesLayer.configuration.lineStyles = colors.mapped { LineStyle(it.asProvider()) } //TODO cache?
      gestalt.crossWireLabelsLayer.configuration.valueLabelBoxStyle = colors.mapped { BoxStyle(it.asProvider()) } //TODO cache?
      gestalt.categoryLinesLayer.configuration.pointPainters = colors.mapped { DotCategoryPointPainter(snapXValues = false, snapYValues = false).apply { pointStylePainter.color = it.asProvider() } } //TODO cache?

      meistercharts {
        gestalt.configure(this)

        configure {
          configurable("Min category size", gestalt.configuration::minCategorySize) {
            max = 200.0
          }

          configurable("Max category size", gestalt.configuration::maxCategorySize, 150.0) {
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

          configurable("Category gap", gestalt.configuration::categoryGap) {
            max = 200.0
          }

          configurable("Axis tick font", gestalt.valueAxisLayer.configuration::tickFont) {
            onChange {
              gestalt.applyAxisTickFont(it)
              markAsDirty()
            }
          }

          configurable("Axis title font", gestalt.valueAxisLayer.configuration::titleFont) {
            onChange {
              gestalt.applyAxisTitleFont(it)
              markAsDirty()
            }
          }

          configurable("Cross wire label font", gestalt.crossWireLabelsLayer.configuration::valueLabelFont) {
          }
        }
      }
    }
  }

}



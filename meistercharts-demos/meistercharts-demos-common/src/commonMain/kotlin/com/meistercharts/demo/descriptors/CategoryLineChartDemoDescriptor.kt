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
import com.meistercharts.demo.configurableSeparate
import kotlin.uuid.Uuid
import com.meistercharts.algorithms.layers.barchart.GreedyCategoryAxisLabelPainter
import com.meistercharts.algorithms.layers.linechart.LineStyle
import com.meistercharts.charts.CategoryLineChartGestalt
import com.meistercharts.color.Color
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.VariabilityType
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.configurableFont
import com.meistercharts.demo.configurableFontProvider
import com.meistercharts.demo.configurableInsetsSeparate
import com.meistercharts.demo.configurableInt
import com.meistercharts.font.FontDescriptorFragment
import com.meistercharts.font.FontFamily
import com.meistercharts.font.FontSize
import com.meistercharts.font.FontWeight
import com.meistercharts.model.category.Category
import com.meistercharts.model.category.DefaultCategorySeriesModel
import com.meistercharts.model.category.DefaultSeries
import com.meistercharts.model.category.Series
import com.meistercharts.painter.DotCategoryPointPainter
import com.meistercharts.range.ValueRange
import com.meistercharts.style.BoxStyle
import it.neckar.open.i18n.TextKey
import it.neckar.open.kotlin.lang.asProvider
import it.neckar.open.kotlin.lang.fastFor
import it.neckar.open.provider.MultiProvider

class CategoryLineChartDemoDescriptor : MeisterchartsDemoDescriptor<Nothing> {
  override val uuid: Uuid = Uuid.parse("71408fb9-90b1-4d5a-85e2-cd9a8e5c8fa2")
  override val name: String = "Category Line Chart with many categories"
  override val category: DemoCategory = DemoCategory.ShowCase
  override val description: String = "A category line chart that supports many categories because the minimum category size is set to 0."

  override val quality: DemoQuality = DemoQuality.High
  override val variabilityType: VariabilityType = VariabilityType.Stable

  override fun prepareDemo(configuration: PredefinedConfiguration<Nothing>?): MeisterchartsDemo {
    return MeisterchartsDemo(this) {
      val gestalt = CategoryLineChartGestalt()

      //The following values are taken from a real-life example of a well-known sensor vendor
      //
      gestalt.configuration.apply {
        valueRange = ValueRange.linear(-50.0, 50.0)
        minCategorySize = 0.0
      }
      gestalt.contentViewportMargin = gestalt.contentViewportMargin.withBottom(36.8)

      gestalt.categoryAxisLayer.configuration.apply {
        axisLineWidth = 0.0
        tickLabelGap = 7.0
        tickLength = 0.0
        setTitle("(Stations)")
        titleGap = 3.5
        tickFont = FontDescriptorFragment(
          family = FontFamily("Open Sans"),
          size = FontSize(11.0),
          weight = FontWeight(400)
        ).asProvider()
        titleFont = FontDescriptorFragment(
          family = FontFamily("Open Sans"),
          size = FontSize(11.0),
          weight = FontWeight(600)
        ).asProvider()
      }
      gestalt.categoryAxisLayer.configuration.axisLabelPainter = GreedyCategoryAxisLabelPainter { categoryLabelGap = 10.5 }
      //
      //The preceding values are taken from a real-life example of a well-known sensor vendor

      gestalt.categoryLinesLayer.configuration.lineStyles = MultiProvider.always(LineStyle(Color.orangered, 1.0))
      gestalt.crossWireLabelsLayer.configuration.valueLabelBoxStyle = MultiProvider.always(BoxStyle(fill = Color.orangered))

      gestalt.categoryLinesLayer.configuration.pointPainters = MultiProvider.always(DotCategoryPointPainter(snapXValues = false, snapYValues = false).apply {
        pointStylePainter.color = Color.orangered
        pointStylePainter.pointSize = 3.0
      })

      meistercharts {
        gestalt.configure(this)

        configure {

          var valuesCount = 100
          var linesCount = 1

          fun updateCategoriesModel() {
            val categories = mutableListOf<Category>()
            valuesCount.fastFor { index ->
              categories.add(Category(TextKey.simple((index + 1).toString())))
            }

            val series = mutableListOf<Series>()
            linesCount.fastFor { lineIndex ->
              val values = mutableListOf<Double>()
              valuesCount.fastFor { categoryIndex ->
                values.add((categoryIndex + lineIndex * 5) % 100.0 - 50.0)
              }
              series.add(DefaultSeries("Series $lineIndex", values))
            }

            gestalt.configuration.categorySeriesModel = DefaultCategorySeriesModel(categories, series)
            markAsDirty()
          }

          configurableInt("Categories count") {
            min = 0
            max = 10_000
            step = 1
            value = valuesCount
            onChange {
              valuesCount = it
              updateCategoriesModel()
            }
          }

          configurableInt("Lines count") {
            min = 0
            max = 10
            step = 1
            value = linesCount
            onChange {
              linesCount = it
              updateCategoriesModel()
            }
          }

          configurableSeparate("Content Viewport Margin", gestalt::contentViewportMargin)

          configurable("Min category size", gestalt.configuration::minCategorySize) {
            max = 200.0
          }

          configurable("Max category size", gestalt.configuration::maxCategorySize, 150.0) {
            max = 200.0
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

          updateCategoriesModel()
        }
      }
    }
  }

}



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
import com.meistercharts.algorithms.layers.axis.withMaxNumberOfTicks
import com.meistercharts.algorithms.layers.linechart.LineStyle
import com.meistercharts.algorithms.painter.SplineLinePainter
import com.meistercharts.annotations.Domain
import com.meistercharts.charts.CategoryLineChartGestalt
import com.meistercharts.color.Color
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.VariabilityType
import com.meistercharts.demo.configurableBoolean
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.configurableEnum
import com.meistercharts.demo.configurableFont
import com.meistercharts.demo.configurableFontProvider
import com.meistercharts.demo.configurableInsetsSeparate
import com.meistercharts.demo.configurableInt
import com.meistercharts.model.category.Category
import com.meistercharts.model.category.CategoryIndex
import com.meistercharts.model.category.DefaultCategorySeriesModel
import com.meistercharts.model.category.DefaultSeries
import com.meistercharts.model.category.Series
import com.meistercharts.model.category.SeriesIndex
import com.meistercharts.painter.DotCategoryPointPainter
import com.meistercharts.painter.XyCategoryLinePainter
import com.meistercharts.range.LinearValueRange
import com.meistercharts.range.ValueRange
import com.meistercharts.style.BoxStyle
import it.neckar.open.collections.fastForEachIndexed
import it.neckar.open.i18n.TextKey
import it.neckar.open.kotlin.lang.fastMap
import it.neckar.open.kotlin.lang.randomNormal
import it.neckar.open.provider.DoublesProvider
import it.neckar.open.provider.MultiProvider
import it.neckar.open.provider.MultiProvider1

class CategoryLineChartGestaltDemoDescriptor : MeisterchartsDemoDescriptor<(gestalt: CategoryLineChartGestalt) -> Unit> {
  override val uuid: Uuid = Uuid.parse("0edd9563-d84a-4fad-a377-34018735affc")
  override val name: String = "Category Line Chart"
  override val category: DemoCategory = DemoCategory.Gestalt

  override val quality: DemoQuality = DemoQuality.High
  override val variabilityType: VariabilityType = VariabilityType.Stable

  override val predefinedConfigurations: List<PredefinedConfiguration<(gestalt: CategoryLineChartGestalt) -> Unit>> = listOf(
    PredefinedConfiguration(
      Uuid.parse("213f18ce-aa76-4966-aa1d-5fbd59ada0d3"),
      { gestalt ->
        gestalt.configuration.thresholdValues = DoublesProvider.forValues(18.0, 40.0, 41.1)
        gestalt.configuration.valueRange = ValueRange.linear(0.0, 110.0)
      },
      "linear"
    ),
    PredefinedConfiguration(
      Uuid.parse("9f2e7ee2-a830-4b47-b391-c77624b44520"),
      { gestalt ->
        gestalt.configuration.applyValueAxisTitleOnTop()
        gestalt.valueAxisLayer.configuration.titleProvider = { _, _ -> "MyValue Axis Title" }
        gestalt.configuration.valueRange = ValueRange.linear(0.0, 110.0)
      },
      "linear - Title on top"
    ),
    PredefinedConfiguration(
      Uuid.parse("5ef23647-319d-4e64-8be3-eb7080a0b74c"),
      { gestalt ->

        gestalt.configuration.valueRange = ValueRange.linear(0.0, 20.0)

        gestalt.configuration.categorySeriesModel = DefaultCategorySeriesModel(
          listOf(
            Category(TextKey.simple("Jan")),
            Category(TextKey.simple("Feb")),
            Category(TextKey.simple("Mar")),
            Category(TextKey.simple("Apr")),
            Category(TextKey.simple("May")),
            Category(TextKey.simple("Jun")),
            Category(TextKey.simple("Jul")),
            Category(TextKey.simple("Aug")),
            Category(TextKey.simple("Sep")),
            Category(TextKey.simple("Oct")),
            Category(TextKey.simple("Nov")),
            Category(TextKey.simple("Dec"))
          ),
          listOf(
            DefaultSeries("1", listOf(11.0, 0.1, 0.0, -0.1, 0.0001, 0.5, 19.9, 19.99999999, 20.0, 20.1, 19.5, 12.0))
          )
        )
      },
      "linear - min/max values"
    ),
    PredefinedConfiguration(
      Uuid.parse("9ff813cf-1e6e-4986-a854-cb6b0923185a"),
      { gestalt ->
        gestalt.configuration.categorySeriesModel = DefaultCategorySeriesModel(
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
        )

        gestalt.configuration.valueRange = ValueRange.linear(0.0, 110.0)
        val colors = listOf(Color.orangered, Color.blueviolet, Color.greenyellow, Color.limegreen, Color.chocolate)
        gestalt.categoryLinesLayer.configuration.lineStyles = MultiProvider.forListModulo(colors.map { color -> LineStyle(color) })
        gestalt.crossWireLabelsLayer.configuration.valueLabelBoxStyle = MultiProvider.forListModulo(colors.map { color -> BoxStyle(color) })
        gestalt.categoryLinesLayer.configuration.pointPainters = MultiProvider.forListModulo(colors.map { color -> DotCategoryPointPainter(snapXValues = false, snapYValues = false).apply { pointStylePainter.color = color } })
      },
      "multiple series"
    ),
    PredefinedConfiguration(
      Uuid.parse("10101c6c-110d-4288-af00-a818ab46a828"),
      { gestalt ->
        gestalt.categoryLinesLayer.configuration.linePainters = MultiProvider.always(XyCategoryLinePainter(snapXValues = false, snapYValues = false, SplineLinePainter(false, false)))
        gestalt.configuration.valueRange = ValueRange.linear(0.0, 110.0)
      },
      "splines"
    ),
    PredefinedConfiguration(
      Uuid.parse("dba52289-a137-4c78-b124-e9022e4575e7"),
      { gestalt ->
        gestalt.configuration.valueRange = gestalt.configuration.valueRange.let {
          ValueRange.logarithmic(LogarithmicRangeStart, it.end)
        }
        gestalt.valueAxisLayer.configuration.applyLogarithmicScale()
      },
      "logarithmic"
    ),
    PredefinedConfiguration(
      Uuid.parse("2046b8d7-d0ca-455a-ab33-8bdf0b88ec53"),
      { gestalt ->
        gestalt.configuration.categorySeriesModel = DefaultCategorySeriesModel(
          listOf(
            Category(TextKey.simple("1")),
            Category(TextKey.simple("2")),
            Category(TextKey.simple("3")),
            Category(TextKey.simple("4")),
            Category(TextKey.simple("5")),
            Category(TextKey.simple("6")),
            Category(TextKey.simple("7")),
            Category(TextKey.simple("8")),
            Category(TextKey.simple("9")),
          ),
          listOf(
            DefaultSeries("1", listOf(3000.0, 100.0, -10.0, 0.3, 0.0, 0.3, -10.0, 100.0, 3000.0))
          )
        )
        gestalt.configuration.thresholdValues = DoublesProvider.forValues(1900.0, 0.8)
        gestalt.configuration.thresholdLabels = MultiProvider1 { index, param1 ->
          when (index) {
            0 -> listOf("Max", "1.9K")
            1 -> listOf("Min", "0.8")
            else -> listOf("Unknown")
          }
        }
        gestalt.configuration.valueRange = ValueRange.logarithmic(0.1, 10000.0)
        gestalt.valueAxisLayer.configuration.applyLogarithmicScale()
      },
      "logarithmic with invalid values"
    ),
  )

  override fun prepareDemo(configuration: PredefinedConfiguration<(gestalt: CategoryLineChartGestalt) -> Unit>?): MeisterchartsDemo {
    return MeisterchartsDemo(this) {
      val gestalt = CategoryLineChartGestalt()

      configuration?.payload?.invoke(gestalt)
      val defaultValueRange = gestalt.configuration.valueRange

      meistercharts {
        gestalt.configure(this)

        configure {

          configurable("Show values grid", gestalt.configuration::showValuesGrid)
          configurable("Show categories grid", gestalt.configuration::showCategoriesGrid)

          declare {
            button("Add line") {

              val categories = mutableListOf<Category>()
              val categoryModel = gestalt.configuration.categorySeriesModel as DefaultCategorySeriesModel

              for (categoryIndex in 0 until categoryModel.numberOfCategories) {
                categories.add(categoryModel.categoryAt(CategoryIndex(categoryIndex)))
              }
              val series = mutableListOf<Series>()
              for (seriesIndex in 0 until categoryModel.numberOfSeries) {
                series.add(categoryModel.seriesAt(SeriesIndex(seriesIndex)))
              }
              val values = mutableListOf<Double>()
              for (categoryIndex in 0 until categoryModel.numberOfCategories) {
                values.add(randomNormal((gestalt.configuration.valueRange as LinearValueRange).center(), (series.size) * 5.0))
              }
              series.add(DefaultSeries(series.size.toString(), values))
              gestalt.configuration.categorySeriesModel = DefaultCategorySeriesModel(categories, series)
              markAsDirty()
            }
          }

          configurable("Min category size", gestalt.configuration::minCategorySize) {
            max = 200.0
          }

          configurable("Max category size", gestalt.configuration::maxCategorySize, 150.0) {
            max = 200.0
          }

          configurable("Category gap", gestalt.configuration::categoryGap) {
            max = 200.0
          }

          configurableEnum("Axis scale", if (defaultValueRange is LinearValueRange) ValueAxisScale.Linear else ValueAxisScale.Logarithmic, kotlin.enums.enumEntries<ValueAxisScale>().toList()) {
            onChange {
              when (it) {
                ValueAxisScale.Linear -> gestalt.configuration.valueRange = ValueRange.linear(defaultValueRange.start, defaultValueRange.end)
                ValueAxisScale.Logarithmic -> gestalt.configuration.valueRange = ValueRange.logarithmic(LogarithmicRangeStart, defaultValueRange.end)
              }
              markAsDirty()
            }
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

          configurableInt("Max tick count") {
            min = 0
            max = 20
            value = 20
            onChange {
              gestalt.valueAxisLayer.configuration.ticks = gestalt.valueAxisLayer.configuration.ticks.withMaxNumberOfTicks(it)
              markAsDirty()
            }
          }

          configurableSeparate("Content Viewport Margin", gestalt::contentViewportMargin)

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

  private enum class ValueAxisScale {
    Linear,
    Logarithmic
  }

  companion object {
    private const val LogarithmicRangeStart: @Domain Double = 0.1
  }

}



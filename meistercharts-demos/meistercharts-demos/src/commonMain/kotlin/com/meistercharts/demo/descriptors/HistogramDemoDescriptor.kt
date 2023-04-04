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

import com.meistercharts.algorithms.ValueRange
import com.meistercharts.algorithms.layout.Exact
import com.meistercharts.algorithms.layout.Rounded
import com.meistercharts.algorithms.model.Category
import com.meistercharts.algorithms.model.CategorySeriesModel
import com.meistercharts.algorithms.model.DefaultCategorySeriesModel
import com.meistercharts.algorithms.model.DefaultSeries
import com.meistercharts.canvas.pixelSnapSupport
import com.meistercharts.charts.HistogramGestalt
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableBoolean
import com.meistercharts.demo.configurableColorPickerNullable
import com.meistercharts.demo.configurableDecimals
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.configurableEnum
import com.meistercharts.demo.configurableFont
import com.meistercharts.demo.configurableInsetsSeparate
import com.meistercharts.demo.configurableList
import com.meistercharts.demo.configurableListWithProperty
import com.meistercharts.demo.configurableValueRange
import com.meistercharts.demo.section
import com.meistercharts.demo.style
import com.meistercharts.model.Side
import it.neckar.open.kotlin.lang.randomNormal
import it.neckar.open.provider.DoublesProvider
import it.neckar.open.formatting.decimalFormat1digit
import it.neckar.open.formatting.format
import it.neckar.open.i18n.TextKey
import it.neckar.open.observable.ObservableObject

class HistogramDemoDescriptor : ChartingDemoDescriptor<HistogramDemoDescriptor.HistogramDemoDescriptorConfiguration> {

  override val name: String = "Histogram"

  //language=HTML
  override val description: String = "A histogram implemented with a BarChartGroupedGestalt"
  override val category: DemoCategory = DemoCategory.Gestalt

  private val histogramValueRange = ValueRange.linear(0.0, 1000.0)

  private fun createModel(histogramCategoriesCount: Int): CategorySeriesModel {
    require(histogramCategoriesCount > 0) { "need at least one category but was <$histogramCategoriesCount>" }
    val valueRangeCenter = histogramValueRange.center()
    val categories = IntRange(0, histogramCategoriesCount - 1).map { i -> Category(TextKey.simple((i + 1).toString())) }
    val series = IntRange(0, histogramCategoriesCount - 1).map { randomNormal(valueRangeCenter, 150.0) }
    return DefaultCategorySeriesModel(
      categories, listOf(DefaultSeries("A", series))
    )
  }

  /**
   * Creates a model with alternating values between 10% and 90% of the [histogramValueRange]
   */
  private fun createAlternatingModel(histogramCategoriesCount: Int): CategorySeriesModel {
    require(histogramCategoriesCount > 0) { "need at least one category but was <$histogramCategoriesCount>" }

    val topValue = histogramValueRange.end * 0.9
    val bottomValue = histogramValueRange.end * 0.1

    val categories = IntRange(0, histogramCategoriesCount - 1).map { i -> Category(TextKey.simple((i + 1).toString())) }

    val series = IntRange(0, histogramCategoriesCount - 1).map {
      when (it % 2) {
        0 -> topValue
        else -> bottomValue
      }
    }

    return DefaultCategorySeriesModel(
      categories, listOf(DefaultSeries("A", series))
    )
  }

  override val predefinedConfigurations: List<PredefinedConfiguration<HistogramDemoDescriptorConfiguration>> = listOf(
    PredefinedConfiguration(
      HistogramDemoDescriptorConfiguration(
        ModelType.Random
      ) { gestalt ->
        gestalt.configuration.categorySeriesModel = createModel(50)
        gestalt.configuration.thresholdValues = DoublesProvider.forValues(17.38, 550.0)
        gestalt.style.valueRange = histogramValueRange
        gestalt.valueAxisLayer.style.titleProvider = { _, _ -> "MyValue Axis Title" }
      },
      "default (linear, vertical)"
    ),
    PredefinedConfiguration(
      HistogramDemoDescriptorConfiguration(
        ModelType.Random
      ) { gestalt ->
        gestalt.configuration.categorySeriesModel = createModel(150)
        gestalt.style.valueRange = ValueRange.linear(0.0, 490.0)
        gestalt.valueAxisLayer.style.titleProvider = { _, _ -> "MyValue Axis Title" }
      },
      "default (linear, vertical), values outside"
    ),
    PredefinedConfiguration(
      HistogramDemoDescriptorConfiguration(
        ModelType.Random
      ) { gestalt ->
        gestalt.configuration.categorySeriesModel = createModel(550)
        gestalt.style.valueRange = ValueRange.linear(0.0, 490.0)
        gestalt.valueAxisLayer.style.titleProvider = { _, _ -> "MyValue Axis Title" }
      },
      "default (linear, vertical), many values outside "
    ),
    PredefinedConfiguration(
      HistogramDemoDescriptorConfiguration(
        ModelType.Random
      ) { gestalt ->
        gestalt.configuration.categorySeriesModel = createModel(50)
        gestalt.style.valueRange = histogramValueRange
        gestalt.style.applyAxisTitleOnTop()
        gestalt.valueAxisLayer.style.titleProvider = { _, _ -> "MyValue Axis Title" }
      },
      "default (linear, vertical) - top title"
    ),
    PredefinedConfiguration(
      HistogramDemoDescriptorConfiguration(ModelType.Random) { gestalt ->
        gestalt.configuration.categorySeriesModel = createModel(500)
        gestalt.style.valueRange = histogramValueRange
      },
      "linear, vertical, large"
    ),
    PredefinedConfiguration(
      HistogramDemoDescriptorConfiguration(ModelType.Alternating) { gestalt ->
        gestalt.configuration.categorySeriesModel = createAlternatingModel(500)
        gestalt.style.valueRange = histogramValueRange
      },
      "Linear, vertical, alternating"
    ),
    PredefinedConfiguration(
      HistogramDemoDescriptorConfiguration(ModelType.Random) { gestalt ->
        gestalt.configuration.categorySeriesModel = createModel(50)
        gestalt.style.valueRange = ValueRange.logarithmic(0.1, histogramValueRange.end)
        gestalt.valueAxisLayer.style.applyLogarithmicScale()
        gestalt.valueAxisLayer.style.ticksFormat = decimalFormat1digit
      },
      "logarithmic, vertical"
    ),
    PredefinedConfiguration(
      HistogramDemoDescriptorConfiguration(ModelType.Random) { gestalt ->
        gestalt.configuration.categorySeriesModel = createModel(50)
        gestalt.style.valueRange = histogramValueRange
        gestalt.style.applyHorizontalConfiguration()
      },
      "linear, horizontal"
    ),
    PredefinedConfiguration(
      HistogramDemoDescriptorConfiguration(ModelType.Random) { gestalt ->
        gestalt.configuration.categorySeriesModel = createModel(50)
        gestalt.style.valueRange = ValueRange.logarithmic(0.1, histogramValueRange.end)
        gestalt.valueAxisLayer.style.applyLogarithmicScale()
        gestalt.valueAxisLayer.style.ticksFormat = decimalFormat1digit
        gestalt.style.applyHorizontalConfiguration()
      },
      "logarithmic, horizontal"
    ),
    PredefinedConfiguration(
      HistogramDemoDescriptorConfiguration(ModelType.Random) { gestalt ->
        gestalt.configuration.categorySeriesModel = createModel(250)
        gestalt.style.valueRange = histogramValueRange
        gestalt.style.applyHorizontalConfiguration()
      },
      "horizontal, many"
    ),
  )

  override fun createDemo(configuration: PredefinedConfiguration<HistogramDemoDescriptorConfiguration>?): ChartingDemo {
    require(configuration != null) { "configuration must not be null" }

    return ChartingDemo {

      val gestalt = HistogramGestalt()
      configuration.payload.gestaltConfiguration(gestalt)

      meistercharts {
        gestalt.configure(this)

        gestalt.style.minGapBetweenGroups = 0.0
        gestalt.barChartGroupedGestalt.groupedBarsPainter.configuration.setBarSizeRange(1.0, 150.0)

        configure {
          configurableListWithProperty("Layout Mode", gestalt.barChartGroupedGestalt.categoryLayer.style.layoutCalculator.style::layoutMode, listOf(Exact, Rounded)) {
            converter {
              when (it) {
                Rounded -> "Rounded"
                Exact -> "Exact"
              }
            }
          }

          configurableEnum("Snap", chartSupport.pixelSnapSupport::snapConfiguration)

          val modelType = ObservableObject(configuration.payload.modelType)
          configurableEnum("Model Type", modelType)

          configurableList("Number of bars", gestalt.configuration.categorySeriesModel.numberOfCategories, listOf(1, 10, 25, 50, 100, 500, 2500, 10_000, 50_000, 100_000, 500_000, 1_000_000, 10_000_000)) {
            converter {
              it.format()
            }
            onChange {
              gestalt.configuration.categorySeriesModel = when (modelType.get()) {
                ModelType.Random -> createModel(it)
                ModelType.Alternating -> createAlternatingModel(it)
              }

              markAsDirty()
            }
          }

          configurableDecimals("Decimal places", gestalt.valueAxisLayer.style::ticksFormat) {
            value = 2 //hard coded value from
            max = 7
          }

          configurableInsetsSeparate("Content viewport margin", gestalt::contentViewportMargin) {}

          declare {
            button("Horizontal orientation") {
              gestalt.style.applyHorizontalConfiguration()
              markAsDirty()
            }
          }

          declare {
            button("Vertical orientation") {
              gestalt.style.applyVerticalConfiguration()
              markAsDirty()
            }
          }

          //Update the defaults
          gestalt.categoryLayer.style.layoutCalculator.style.minCategorySize = 1.0

          configurableDouble("Min category size", gestalt.categoryLayer.style.layoutCalculator.style::minCategorySize) {
            max = 50.0
          }

          configurableDouble("Max category size", gestalt.categoryLayer.style.layoutCalculator.style::maxCategorySize, 150.0) {
            max = 250.0
          }

          configurableEnum("Value axis side", gestalt.valueAxisLayer.style.side, Side.entries) {
            onChange {
              gestalt.valueAxisLayer.style.side = it
              markAsDirty()
            }
          }

          configurableEnum("Category axis side", gestalt.categoryAxisLayer.style.side, Side.entries) {
            onChange {
              gestalt.categoryAxisLayer.style.side = it
              markAsDirty()
            }
          }

          configurableValueRange("Value Range", gestalt.style::valueRange) {
            min = histogramValueRange.start
            max = histogramValueRange.end
          }

          section("Group")

          configurableDouble("Min bar size", gestalt.groupedBarsPainter.configuration.minBarSize) {
            min = 1.0
            max = 20.0
            onChange {
              gestalt.groupedBarsPainter.configuration.setBarSizeRange(it, gestalt.groupedBarsPainter.configuration.maxBarSize)
              markAsDirty()
            }
          }

          configurableDouble("Max bar size", gestalt.groupedBarsPainter.configuration.maxBarSize ?: 50.0) {
            min = 1.0
            max = 50.0
            onChange {
              gestalt.groupedBarsPainter.configuration.setBarSizeRange(gestalt.groupedBarsPainter.configuration.minBarSize, it)
              markAsDirty()
            }
          }

          configurableDouble("Bar gap", gestalt.groupedBarsPainter.configuration::barGap) {
            max = 50.0
          }

          configurableFont("Axis tick font", gestalt.valueAxisLayer.style.tickFont) {
            onChange {
              gestalt.style.applyAxisTickFont(it)
              markAsDirty()
            }
          }

          configurableFont("Axis title font", gestalt.valueAxisLayer.style.titleFont) {
            onChange {
              gestalt.style.applyAxisTitleFont(it)
              markAsDirty()
            }
          }

          configurableBoolean("Show grid", gestalt.style::showGrid)

          configurableBoolean("Show value labels", gestalt.groupedBarsPainter.configuration::showValueLabel)

          configurableColorPickerNullable("Value label color", gestalt.groupedBarsPainter.configuration::valueLabelColor)

          configurableColorPickerNullable("Value label stroke color", gestalt.groupedBarsPainter.configuration::valueLabelStrokeColor)

          configurableFont("Value label font", gestalt.groupedBarsPainter.configuration::valueLabelFont)
        }
      }
    }
  }

  enum class ModelType {
    Random,
    Alternating
  }

  data class HistogramDemoDescriptorConfiguration(
    val modelType: ModelType,
    val gestaltConfiguration: (gestalt: HistogramGestalt) -> Unit,
  )
}


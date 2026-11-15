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
import com.meistercharts.algorithms.layout.Exact
import com.meistercharts.algorithms.layout.Rounded
import com.meistercharts.canvas.pixelSnapSupport
import com.meistercharts.charts.HistogramGestalt
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.VariabilityType
import com.meistercharts.demo.configurableBoolean
import com.meistercharts.demo.configurableColorPickerProviderNullable
import com.meistercharts.demo.configurableDecimals
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.configurableEnum
import com.meistercharts.demo.configurableFont
import com.meistercharts.demo.configurableFontProvider
import com.meistercharts.demo.configurableInsetsSeparate
import com.meistercharts.demo.configurableList
import com.meistercharts.demo.configurableListWithProperty
import com.meistercharts.demo.configurableValueRange
import com.meistercharts.demo.section
import com.meistercharts.demo.style
import com.meistercharts.model.category.Category
import com.meistercharts.model.category.CategorySeriesModel
import com.meistercharts.model.category.DefaultCategorySeriesModel
import com.meistercharts.model.category.DefaultSeries
import com.meistercharts.range.ValueRange
import it.neckar.geometry.Side
import it.neckar.open.formatting.decimalFormat1digit
import it.neckar.open.formatting.format
import it.neckar.open.i18n.TextKey
import it.neckar.open.kotlin.lang.randomNormal
import it.neckar.open.observable.ObservableObject
import it.neckar.open.provider.DoublesProvider

class HistogramDemoDescriptor : MeisterchartsDemoDescriptor<HistogramDemoDescriptor.HistogramDemoDescriptorConfiguration> {
  override val uuid: Uuid = Uuid.parse("07ad81a1-3934-4e29-ad94-b4ff0e7e6513")

  override val name: String = "Histogram"
  override val quality: DemoQuality = DemoQuality.High
  override val variabilityType: VariabilityType = VariabilityType.Stable

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
      Uuid.parse("bd7ff5cb-20e4-483c-b718-ff0bf2675286"),
      HistogramDemoDescriptorConfiguration(
        ModelType.Random
      ) { gestalt ->
        gestalt.configuration.categorySeriesModel = createModel(50)
        gestalt.configuration.thresholdValues = DoublesProvider.forValues(17.38, 550.0)
        gestalt.configuration.valueRange = histogramValueRange
        gestalt.valueAxisLayer.configuration.titleProvider = { _, _ -> "MyValue Axis Title" }
      },
      "default (linear, vertical)"
    ),
    PredefinedConfiguration(
      Uuid.parse("7b9dcae4-5ce0-40a6-adcb-3d6bd49ac60b"),
      HistogramDemoDescriptorConfiguration(
        ModelType.Random
      ) { gestalt ->
        gestalt.configuration.categorySeriesModel = createModel(150)
        gestalt.configuration.valueRange = ValueRange.linear(0.0, 490.0)
        gestalt.valueAxisLayer.configuration.titleProvider = { _, _ -> "MyValue Axis Title" }
      },
      "default (linear, vertical), values outside"
    ),
    PredefinedConfiguration(
      Uuid.parse("2dfdc02d-f83a-4c03-b2d8-cf2ce5af6584"),
      HistogramDemoDescriptorConfiguration(
        ModelType.Random
      ) { gestalt ->
        gestalt.configuration.categorySeriesModel = createModel(550)
        gestalt.configuration.valueRange = ValueRange.linear(0.0, 490.0)
        gestalt.valueAxisLayer.configuration.titleProvider = { _, _ -> "MyValue Axis Title" }
      },
      "default (linear, vertical), many values outside "
    ),
    PredefinedConfiguration(
      Uuid.parse("47c24600-e20b-47c8-bcfc-a36a97f6832d"),
      HistogramDemoDescriptorConfiguration(
        ModelType.Random
      ) { gestalt ->
        gestalt.configuration.categorySeriesModel = createModel(50)
        gestalt.configuration.valueRange = histogramValueRange
        gestalt.configuration.applyAxisTitleOnTop()
        gestalt.valueAxisLayer.configuration.titleProvider = { _, _ -> "MyValue Axis Title" }
      },
      "default (linear, vertical) - top title"
    ),
    PredefinedConfiguration(
      Uuid.parse("5ed4326c-cb25-4d5c-b501-89ebd6f6d04f"),
      HistogramDemoDescriptorConfiguration(ModelType.Random) { gestalt ->
        gestalt.configuration.categorySeriesModel = createModel(500)
        gestalt.configuration.valueRange = histogramValueRange
      },
      "linear, vertical, large"
    ),
    PredefinedConfiguration(
      Uuid.parse("4303e720-a97a-4e9c-92b9-6c1e19627c35"),
      HistogramDemoDescriptorConfiguration(ModelType.Alternating) { gestalt ->
        gestalt.configuration.categorySeriesModel = createAlternatingModel(500)
        gestalt.configuration.valueRange = histogramValueRange
      },
      "Linear, vertical, alternating"
    ),
    PredefinedConfiguration(
      Uuid.parse("a7f74a26-8ea7-4629-8424-69ef6f2776b0"),
      HistogramDemoDescriptorConfiguration(ModelType.Random) { gestalt ->
        gestalt.configuration.categorySeriesModel = createModel(50)
        gestalt.configuration.valueRange = ValueRange.logarithmic(0.1, histogramValueRange.end)
        gestalt.valueAxisLayer.configuration.applyLogarithmicScale()
        gestalt.valueAxisLayer.configuration.ticksFormat = decimalFormat1digit
      },
      "logarithmic, vertical"
    ),
    PredefinedConfiguration(
      Uuid.parse("acd5f505-04bd-4fcd-a482-79debacce99f"),
      HistogramDemoDescriptorConfiguration(ModelType.Random) { gestalt ->
        gestalt.configuration.categorySeriesModel = createModel(50)
        gestalt.configuration.valueRange = histogramValueRange
        gestalt.configuration.applyHorizontalConfiguration()
      },
      "linear, horizontal"
    ),
    PredefinedConfiguration(
      Uuid.parse("4e9954d0-ed7e-4438-832e-42c6e72ef27d"),
      HistogramDemoDescriptorConfiguration(ModelType.Random) { gestalt ->
        gestalt.configuration.categorySeriesModel = createModel(50)
        gestalt.configuration.valueRange = ValueRange.logarithmic(0.1, histogramValueRange.end)
        gestalt.valueAxisLayer.configuration.applyLogarithmicScale()
        gestalt.valueAxisLayer.configuration.ticksFormat = decimalFormat1digit
        gestalt.configuration.applyHorizontalConfiguration()
      },
      "logarithmic, horizontal"
    ),
    PredefinedConfiguration(
      Uuid.parse("338a83d9-c04b-42e2-9170-0449876b7889"),
      HistogramDemoDescriptorConfiguration(ModelType.Random) { gestalt ->
        gestalt.configuration.categorySeriesModel = createModel(250)
        gestalt.configuration.valueRange = histogramValueRange
        gestalt.configuration.applyHorizontalConfiguration()
      },
      "horizontal, many"
    ),
  )

  override fun prepareDemo(configuration: PredefinedConfiguration<HistogramDemoDescriptorConfiguration>?): MeisterchartsDemo {
    require(configuration != null) { "configuration must not be null" }

    return MeisterchartsDemo(this) {

      val gestalt = HistogramGestalt()
      configuration.payload.gestaltConfiguration(gestalt)

      meistercharts {
        gestalt.configure(this)

        gestalt.configuration.minGapBetweenGroups = 0.0
        gestalt.barChartGroupedGestalt.groupedBarsPainter.configuration.setBarSizeRange(1.0, 150.0)

        configure {
          configurableListWithProperty("Layout Mode", gestalt.barChartGroupedGestalt.categoryLayer.configuration.layoutCalculator.style::layoutMode, listOf(Exact, Rounded)) {
            converter {
              when (it) {
                Rounded -> "Rounded"
                Exact -> "Exact"
              }
            }
          }

          configurable("Snap", chartSupport.pixelSnapSupport::snapConfiguration)

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

          configurable("Decimal places", gestalt.valueAxisLayer.configuration::ticksFormat) {
            value = 2 //hard coded value from
            max = 7
          }

          configurableSeparate("Content viewport margin", gestalt::contentViewportMargin) {}

          declare {
            button("Horizontal orientation") {
              gestalt.configuration.applyHorizontalConfiguration()
              markAsDirty()
            }
          }

          declare {
            button("Vertical orientation") {
              gestalt.configuration.applyVerticalConfiguration()
              markAsDirty()
            }
          }

          gestalt.categoryLayer.configuration.layoutCalculator.style.minCategorySize = 1.0

          configurable("Min category size", gestalt.categoryLayer.configuration.layoutCalculator.style::minCategorySize) {
            max = 50.0
          }

          configurable("Max category size", gestalt.categoryLayer.configuration.layoutCalculator.style::maxCategorySize, 150.0) {
            max = 250.0
          }

          configurableEnum("Value axis side", gestalt.valueAxisLayer.configuration.side, Side.entries) {
            onChange {
              gestalt.valueAxisLayer.configuration.side = it
              markAsDirty()
            }
          }

          configurableEnum("Category axis side", gestalt.categoryAxisLayer.configuration.side, Side.entries) {
            onChange {
              gestalt.categoryAxisLayer.configuration.side = it
              markAsDirty()
            }
          }

          configurable("Value Range", gestalt.configuration::valueRange) {
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

          configurable("Bar gap", gestalt.groupedBarsPainter.configuration::barGap) {
            max = 50.0
          }

          configurable("Axis tick font", gestalt.valueAxisLayer.configuration::tickFont) {
            onChange {
              gestalt.configuration.applyAxisTickFont(it)
              markAsDirty()
            }
          }

          configurable("Axis title font", gestalt.valueAxisLayer.configuration::titleFont) {
            onChange {
              gestalt.configuration.applyAxisTitleFont(it)
              markAsDirty()
            }
          }

          configurable("Show grid", gestalt.configuration::showGrid)

          configurable("Show value labels", gestalt.groupedBarsPainter.configuration::showValueLabel)

          configurable("Value label color", gestalt.groupedBarsPainter.configuration::valueLabelColor)

          configurable("Value label stroke color", gestalt.groupedBarsPainter.configuration::valueLabelStrokeColor)

          configurable("Value label font", gestalt.groupedBarsPainter.configuration::valueLabelFont)
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


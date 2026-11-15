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
import com.meistercharts.algorithms.layers.barchart.CategoryModelBoxStylesProvider
import com.meistercharts.algorithms.layers.barchart.CategorySeriesModelColorsProvider
import com.meistercharts.algorithms.layers.crosswire.CrossWireLayer
import com.meistercharts.charts.BarChartGroupedGestalt
import com.meistercharts.color.Color
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.VariabilityType
import com.meistercharts.demo.configurableColorPickerProviderNullable
import com.meistercharts.demo.configurableDecimalsFormat
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.configurableEnum
import com.meistercharts.demo.configurableFont
import com.meistercharts.demo.configurableFontProvider
import com.meistercharts.demo.configurableInsetsSeparate
import com.meistercharts.demo.configurableValueRange
import com.meistercharts.demo.section
import com.meistercharts.demo.style
import com.meistercharts.design.Theme
import com.meistercharts.design.multiProvider
import com.meistercharts.model.BorderRadius
import com.meistercharts.model.category.Category
import com.meistercharts.model.category.DefaultCategorySeriesModel
import com.meistercharts.model.category.DefaultSeries
import com.meistercharts.range.ValueRange
import com.meistercharts.style.BoxStyle
import com.meistercharts.style.Shadow
import it.neckar.geometry.Side
import it.neckar.open.formatting.decimalFormat
import it.neckar.open.formatting.decimalFormat2digits
import it.neckar.open.i18n.TextKey
import it.neckar.open.kotlin.lang.asProvider
import it.neckar.open.provider.DoublesProvider
import it.neckar.open.provider.MultiProvider1
import it.neckar.open.provider.mapped

class BarChartSingleDemoDescriptor : MeisterchartsDemoDescriptor<(gestalt: BarChartGroupedGestalt) -> Unit> {
  override val uuid: Uuid = Uuid.parse("1393c591-6fab-49f5-9d05-b31124c28a34")

  override val name: String = "Bar Chart - Single"

  override val quality: DemoQuality = DemoQuality.High
  override val variabilityType: VariabilityType = VariabilityType.Stable

  //language=HTML
  override val description: String = "A single-series bar chart based on the grouped bar chart gestalt"

  override val category: DemoCategory = DemoCategory.Gestalt

  override val predefinedConfigurations: List<PredefinedConfiguration<(gestalt: BarChartGroupedGestalt) -> Unit>> = listOf(
    PredefinedConfiguration(
      Uuid.parse("3bc0090f-8e17-442a-8107-7622b8b07999"),
      { gestalt ->
        gestalt.configuration.categorySeriesModel = positiveCategoryModel
        gestalt.configuration.valueRange = positiveLinearValueRange

        gestalt.configuration.thresholdValues = DoublesProvider.forValues(22.0)
        gestalt.configuration.thresholdLabels = MultiProvider1 { index, _ -> listOf("Label for index $index") }
      },
      "default (vertical)"
    ),
    PredefinedConfiguration(
      Uuid.parse("ae4b9036-90af-4224-9a04-a5ad1f370694"),
      { gestalt ->
        gestalt.configuration.categorySeriesModel = positiveCategoryModel
        gestalt.configuration.valueRange = positiveLinearValueRange
        gestalt.configuration.applyAxisTitleOnTop()
        gestalt.valueAxisLayer.configuration.titleProvider = { _, _ -> "MyValue Axis Title" }

        gestalt.configuration.thresholdValues = DoublesProvider.forValues(22.0)
        gestalt.configuration.thresholdLabels = MultiProvider1 { index, _ -> listOf("Label for index $index") }
      },
      "default (vertical) - top title"
    ),
    PredefinedConfiguration(
      Uuid.parse("5a393f54-0750-4ed7-bbb6-9af2cc663523"),
      { gestalt ->
        gestalt.configuration.categorySeriesModel = positiveCategoryModel
        gestalt.configuration.valueRange = positiveLinearValueRange
        gestalt.configuration.applyHorizontalConfiguration()
        gestalt.configuration.applyAxisTitleOnTop()
        gestalt.categoryAxisLayer.configuration.titleProvider = { _, _ -> "Category Axis Title" }
      },
      "default (horizontal) - top title"
    ),
    PredefinedConfiguration(
      Uuid.parse("0c52634b-73ff-4827-a65a-59bcae60914f"),
      { gestalt ->
        gestalt.configuration.thresholdValues = DoublesProvider.forValues(20.0, 17.0, 33.0)
        gestalt.configuration.thresholdLabels = MultiProvider1 { index, _ ->
          listOf("Threshold value", decimalFormat.format(gestalt.configuration.thresholdValues.valueAt(index)))
        }
        gestalt.configuration.categorySeriesModel = positiveCategoryModel
        gestalt.configuration.valueRange = positiveLinearValueRange
        gestalt.configuration.applyAxisTitleOnTop()
        gestalt.valueAxisLayer.configuration.titleProvider = { _, _ -> "MyValue Axis Title" }
      },
      "vertical - with thresholds"
    ),
    PredefinedConfiguration(
      Uuid.parse("00a69e8a-e2c0-4af3-ac6a-65e583c8cebc"),
      { gestalt ->
        gestalt.configuration.categorySeriesModel = valuesOutside
      },
      "values outside"
    ),
    PredefinedConfiguration(
      Uuid.parse("84b4592a-62e0-4c2d-b47d-1bd93f6f6279"),
      { gestalt ->
        gestalt.configuration.categorySeriesModel = valuesOutside
        gestalt.configuration.applyHorizontalConfiguration()
      },
      "values outside - horizontal"
    ),
    PredefinedConfiguration(
      Uuid.parse("9d4300c7-6b7d-49db-8195-175613fe6a0a"),
      { gestalt ->
        gestalt.configuration.categorySeriesModel = positiveLogarithmicCategoryModel
        gestalt.configuration.valueRange = positiveLogarithmicValueRange
        gestalt.valueAxisLayer.configuration.applyLogarithmicScale()
        gestalt.valueAxisLayer.configuration.ticksFormat = decimalFormat2digits
      },
      "logarithmic axis"
    ),
    PredefinedConfiguration(
      Uuid.parse("7c3ac895-8e21-414a-9edf-9ae8c5b294f4"),
      { gestalt ->
        gestalt.configuration.categorySeriesModel = positiveCategoryModel
        gestalt.configuration.valueRange = positiveLinearValueRange
        gestalt.configuration.applyHorizontalConfiguration()
      },
      "default (horizontal)"
    ),
    PredefinedConfiguration(
      Uuid.parse("9d724c44-7ab4-4f45-9660-8a4c37719511"),
      { gestalt ->
        gestalt.configuration.categorySeriesModel = negativeCategoryModel
        gestalt.configuration.valueRange = negativeLinearValueRange
      },
      "only negative values (vertical)"
    ),
    PredefinedConfiguration(
      Uuid.parse("e49ea9ca-3668-4af2-94b3-c9b0706614fd"),
      { gestalt ->
        gestalt.configuration.categorySeriesModel = negativeCategoryModel
        gestalt.configuration.valueRange = negativeLinearValueRange
        gestalt.configuration.applyHorizontalConfiguration()
      },
      "only negative values (horizontal)"
    ),
    PredefinedConfiguration(
      Uuid.parse("e1b5c01b-84a7-4423-ab67-33b3d1c2a21a"),
      { gestalt ->
        gestalt.configuration.categorySeriesModel = categoryModel
        gestalt.configuration.valueRange = linearValueRange
      },
      "positive + negative values (vertical)"
    ),
    PredefinedConfiguration(
      Uuid.parse("c179b8e1-f4bb-4a79-af9f-b2543ea76a09"),
      { gestalt ->
        gestalt.configuration.categorySeriesModel = categoryModel
        gestalt.configuration.valueRange = linearValueRange
        gestalt.configuration.applyHorizontalConfiguration()
      },
      "positive + negative values (horizontal)"
    ),
  )

  override fun prepareDemo(configuration: PredefinedConfiguration<(gestalt: BarChartGroupedGestalt) -> Unit>?): MeisterchartsDemo {
    require(configuration != null) { "configuration must not be null" }

    return MeisterchartsDemo(this) {

      val gestalt = BarChartGroupedGestalt()
      gestalt.prepareSimpleBarChart()
      configuration.payload(gestalt)

      meistercharts {
        gestalt.configure(this)

        configure {
          configurableDecimalsFormat("Decimal places", property = gestalt.valueAxisLayer.configuration::ticksFormat)

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

          configurable("Min category size", gestalt.categoryLayer.configuration.layoutCalculator.style::minCategorySize, {
            max = 1000.0
          })

          configurable("Max category size", gestalt.categoryLayer.configuration.layoutCalculator.style::maxCategorySize, 150.0) {
            max = 1000.0
          }

          configurableEnum("Value axis side", gestalt.valueAxisLayer.configuration.side, kotlin.enums.enumEntries<Side>().toList()) {
            onChange {
              gestalt.valueAxisLayer.configuration.side = it
              markAsDirty()
            }
          }

          configurableEnum("Category axis side", gestalt.categoryAxisLayer.configuration.side, kotlin.enums.enumEntries<Side>().toList()) {
            onChange {
              gestalt.categoryAxisLayer.configuration.side = it
              markAsDirty()
            }
          }

          configurable("Value Range", gestalt.configuration::valueRange) {
            min = negativeLinearValueRange.start * 2
            max = positiveLogarithmicValueRange.end * 10
          }

          section("Group")

          configurableDouble("Min bar size", gestalt.groupedBarsPainter.configuration.minBarSize) {
            min = 1.0
            max = 200.0
            onChange {
              gestalt.groupedBarsPainter.configuration.setBarSizeRange(it, gestalt.groupedBarsPainter.configuration.maxBarSize)
              markAsDirty()
            }
          }

          configurableDouble("Max bar size", gestalt.groupedBarsPainter.configuration.maxBarSize ?: 200.0) {
            min = 1.0
            max = 200.0
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

          section("Value labels")

          configurable("Show", gestalt.groupedBarsPainter.configuration::showValueLabel)

          configurable("Anchor gap Horizontal", gestalt.groupedBarsPainter.configuration::valueLabelAnchorGapHorizontal) {
            min = 0.0
            max = 100.0
          }
          configurable("Anchor gap Vertical", gestalt.groupedBarsPainter.configuration::valueLabelAnchorGapVertical) {
            min = 0.0
            max = 100.0
          }

          configurable("Font", gestalt.groupedBarsPainter.configuration::valueLabelFont)

          configurable("Color", gestalt.groupedBarsPainter.configuration::valueLabelColor)

          configurable("Stroke color", gestalt.groupedBarsPainter.configuration::valueLabelStrokeColor)

          configurable("Show tool tips", gestalt.configuration::showTooltip)
        }
      }
    }
  }

  companion object {
    private val categoryModel = DefaultCategorySeriesModel(
      listOf(
        Category(TextKey.simple("Giraffe")),
        Category(TextKey.simple("Elephant with a very long Name!")),
        Category(TextKey.simple("Cheetah")),
        Category(TextKey.simple("Penguin")),
        Category(TextKey.simple("Owl")),
      ),
      listOf(
        DefaultSeries("1", listOf(-34.0, 47.0, -19.0, 12.0, 17.0)),
      )
    )

    private val linearValueRange = ValueRange.linear(-55.0, 55.0)

    private val positiveCategoryModel = DefaultCategorySeriesModel(
      listOf(
        Category(TextKey.simple("Giraffe")),
        Category(TextKey.simple("Elephant with a very long Name!")),
        Category(TextKey.simple("Cheetah")),
        Category(TextKey.simple("Penguin")),
        Category(TextKey.simple("Owl")),
      ),
      listOf(
        DefaultSeries("1", listOf(34.0, 47.0, 19.0, 12.0, 17.0)),
      )
    )

    private val positiveLinearValueRange = ValueRange.linear(0.0, 55.0)

    private val negativeCategoryModel = DefaultCategorySeriesModel(
      listOf(
        Category(TextKey.simple("Giraffe")),
        Category(TextKey.simple("Elephant with a very long Name!")),
        Category(TextKey.simple("Cheetah")),
        Category(TextKey.simple("Penguin")),
        Category(TextKey.simple("Owl")),
      ),
      listOf(
        DefaultSeries("1", listOf(-34.0, -47.0, -19.0, -12.0, -17.0)),
      )
    )

    private val negativeLinearValueRange = ValueRange.linear(-55.0, 0.0)

    private val positiveLogarithmicCategoryModel = DefaultCategorySeriesModel(
      listOf(
        Category(TextKey.simple("Giraffe")),
        Category(TextKey.simple("Elephant with a very long Name!")),
        Category(TextKey.simple("Cheetah")),
        Category(TextKey.simple("Penguin")),
        Category(TextKey.simple("Owl")),
        Category(TextKey.simple("Lion")),
      ),
      listOf(
        DefaultSeries("1", listOf(0.03, 0.4, 7.0, 19.0, 170.0, 847.0)),
      )
    )

    private val valuesOutside = DefaultCategorySeriesModel(
      listOf(
        Category(TextKey.simple("Giraffe")),
        Category(TextKey.simple("Elephant with a very long Name!")),
        Category(TextKey.simple("Cheetah")),
        Category(TextKey.simple("Penguin")),
        Category(TextKey.simple("Owl")),
        Category(TextKey.simple("Lion")),
      ),
      listOf(
        DefaultSeries("1", listOf(-2.03, 0.4, 7.0, 19.0, 170.0, 250.0)),
      )
    )

    private val positiveLogarithmicValueRange = ValueRange.logarithmic(0.001, 1_000.0)

  }
}

fun BarChartGroupedGestalt.prepareSimpleBarChart() {
  // no grid
  configuration.showGrid = false

  val categoriesBoxStyles = Theme.chartColors.multiProvider().mapped {
    //TODO add caching!
    BoxStyle(
      fill = it.asProvider(),
      borderColor = Color.white,
      borderWidth = 2.0,
      padding = CrossWireLayer.Configuration.DefaultLabelBoxPadding,
      radii = BorderRadius.all2,
      shadow = Shadow.LightDrop
    )
  }
  configuration.crossWireLabelBoxStyles = CategoryModelBoxStylesProvider.onlyCategoryBoxStylesProvider(categoriesBoxStyles)

  groupedBarsPainter.configuration.showValueLabel = true
  // use theme-dependent bar-colors
  groupedBarsPainter.configuration.colorsProvider = CategorySeriesModelColorsProvider.onlyCategoryColorsProvider(Theme.chartColors.resolve())
}

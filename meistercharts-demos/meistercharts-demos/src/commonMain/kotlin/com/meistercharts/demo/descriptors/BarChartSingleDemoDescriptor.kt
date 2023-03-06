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
import com.meistercharts.algorithms.layers.barchart.CategoryModelBoxStylesProvider
import com.meistercharts.algorithms.layers.barchart.CategorySeriesModelColorsProvider
import com.meistercharts.algorithms.layers.crosswire.CrossWireLayer
import com.meistercharts.algorithms.model.Category
import com.meistercharts.algorithms.model.DefaultCategorySeriesModel
import com.meistercharts.algorithms.model.DefaultSeries
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.canvas.BorderRadius
import com.meistercharts.charts.BarChartGroupedGestalt
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableBoolean
import com.meistercharts.demo.configurableColorPickerNullable
import com.meistercharts.demo.configurableDecimalsFormat
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.configurableEnum
import com.meistercharts.demo.configurableFont
import com.meistercharts.demo.configurableInsetsSeparate
import com.meistercharts.demo.configurableValueRange
import com.meistercharts.demo.section
import com.meistercharts.demo.style
import com.meistercharts.design.Theme
import com.meistercharts.model.Side
import it.neckar.open.provider.DoublesProvider
import it.neckar.open.provider.MultiProvider1
import it.neckar.open.provider.mapped
import it.neckar.open.formatting.decimalFormat
import it.neckar.open.formatting.decimalFormat2digits
import it.neckar.open.i18n.TextKey
import com.meistercharts.style.BoxStyle
import com.meistercharts.style.Shadow

class BarChartSingleDemoDescriptor : ChartingDemoDescriptor<(gestalt: BarChartGroupedGestalt) -> Unit> {

  override val name: String = "Bar Chart - Single"

  //language=HTML
  override val description: String = """
    <h2>Bar chart</h2>
    <p>A bar chart that uses a <i>BarChartGroupedGestalt</i> under the hood</p>
    """

  override val category: DemoCategory = DemoCategory.Gestalt

  override val predefinedConfigurations: List<PredefinedConfiguration<(gestalt: BarChartGroupedGestalt) -> Unit>> = listOf(
    PredefinedConfiguration(
      { gestalt ->
        gestalt.configuration.categorySeriesModel = positiveCategoryModel
        gestalt.style.valueRange = positiveLinearValueRange

        gestalt.configuration.thresholdValues = DoublesProvider.forValues(22.0)
        gestalt.configuration.thresholdLabels = MultiProvider1 { index, _ -> listOf("Label for index $index") }
      },
      "default (vertical)"
    ),
    PredefinedConfiguration(
      { gestalt ->
        gestalt.configuration.categorySeriesModel = positiveCategoryModel
        gestalt.style.valueRange = positiveLinearValueRange
        gestalt.style.applyAxisTitleOnTop()
        gestalt.valueAxisLayer.style.titleProvider = { _, _ -> "MyValue Axis Title" }
      },
      "default (vertical) - top title"
    ),
    PredefinedConfiguration(
      { gestalt ->
        gestalt.configuration.categorySeriesModel = positiveCategoryModel
        gestalt.style.valueRange = positiveLinearValueRange
        gestalt.style.applyHorizontalConfiguration()
        gestalt.style.applyAxisTitleOnTop()
        gestalt.categoryAxisLayer.style.titleProvider = { _, _ -> "Category Axis Title" }
      },
      "default (horizontal) - top title"
    ),
    PredefinedConfiguration(
      { gestalt ->
        gestalt.configuration.thresholdValues = DoublesProvider.forValues(20.0, 17.0, 33.0)
        gestalt.configuration.thresholdLabels = MultiProvider1 { index, _ ->
          listOf("Threshold value", decimalFormat.format(gestalt.configuration.thresholdValues.valueAt(index)))
        }
        gestalt.configuration.categorySeriesModel = positiveCategoryModel
        gestalt.style.valueRange = positiveLinearValueRange
        gestalt.style.applyAxisTitleOnTop()
        gestalt.valueAxisLayer.style.titleProvider = { _, _ -> "MyValue Axis Title" }
      },
      "vertical - with thresholds"
    ),
    PredefinedConfiguration(
      { gestalt ->
        gestalt.configuration.categorySeriesModel = valuesOutside
      },
      "values outside"
    ),
    PredefinedConfiguration(
      { gestalt ->
        gestalt.configuration.categorySeriesModel = valuesOutside
        gestalt.style.applyHorizontalConfiguration()
      },
      "values outside - horizontal"
    ),
    PredefinedConfiguration(
      { gestalt ->
        gestalt.configuration.categorySeriesModel = positiveLogarithmicCategoryModel
        gestalt.style.valueRange = positiveLogarithmicValueRange
        gestalt.valueAxisLayer.style.applyLogarithmicScale()
        gestalt.valueAxisLayer.style.ticksFormat = decimalFormat2digits
      },
      "logarithmic axis"
    ),
    PredefinedConfiguration(
      { gestalt ->
        gestalt.configuration.categorySeriesModel = positiveCategoryModel
        gestalt.style.valueRange = positiveLinearValueRange
        gestalt.style.applyHorizontalConfiguration()
      },
      "default (horizontal)"
    ),
    PredefinedConfiguration(
      { gestalt ->
        gestalt.configuration.categorySeriesModel = negativeCategoryModel
        gestalt.style.valueRange = negativeLinearValueRange
      },
      "only negative values (vertical)"
    ),
    PredefinedConfiguration(
      { gestalt ->
        gestalt.configuration.categorySeriesModel = negativeCategoryModel
        gestalt.style.valueRange = negativeLinearValueRange
        gestalt.style.applyHorizontalConfiguration()
      },
      "only negative values (horizontal)"
    ),
    PredefinedConfiguration(
      { gestalt ->
        gestalt.configuration.categorySeriesModel = categoryModel
        gestalt.style.valueRange = linearValueRange
      },
      "positive + negative values (vertical)"
    ),
    PredefinedConfiguration(
      { gestalt ->
        gestalt.configuration.categorySeriesModel = categoryModel
        gestalt.style.valueRange = linearValueRange
        gestalt.style.applyHorizontalConfiguration()
      },
      "positive + negative values (horizontal)"
    ),
  )

  override fun createDemo(configuration: PredefinedConfiguration<(gestalt: BarChartGroupedGestalt) -> Unit>?): ChartingDemo {
    require(configuration != null) { "configuration must not be null" }

    return ChartingDemo {

      val gestalt = BarChartGroupedGestalt()
      gestalt.prepareSimpleBarChart()
      configuration.payload(gestalt)

      meistercharts {
        gestalt.configure(this)

        configure {
          configurableDecimalsFormat("Decimal places", property = gestalt.valueAxisLayer.style::ticksFormat)

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

          configurableDouble("Min category size", gestalt.categoryLayer.style.layoutCalculator.style::minCategorySize, {
            max = 1000.0
          })

          configurableDouble("Max category size", gestalt.categoryLayer.style.layoutCalculator.style::maxCategorySize, 150.0) {
            max = 1000.0
          }

          configurableEnum("Value axis side", gestalt.valueAxisLayer.style.side, Side.values()) {
            onChange {
              gestalt.valueAxisLayer.style.side = it
              markAsDirty()
            }
          }

          configurableEnum("Category axis side", gestalt.categoryAxisLayer.style.side, Side.values()) {
            onChange {
              gestalt.categoryAxisLayer.style.side = it
              markAsDirty()
            }
          }

          configurableValueRange("Value Range", gestalt.style::valueRange) {
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

          section("Value labels")

          configurableBoolean("Show", gestalt.groupedBarsPainter.configuration::showValueLabel)

          configurableDouble("Anchor gap Horizontal", gestalt.groupedBarsPainter.configuration::valueLabelAnchorGapHorizontal) {
            min = 0.0
            max = 100.0
          }
          configurableDouble("Anchor gap Vertical", gestalt.groupedBarsPainter.configuration::valueLabelAnchorGapVertical) {
            min = 0.0
            max = 100.0
          }

          configurableFont("Font", gestalt.groupedBarsPainter.configuration::valueLabelFont)

          configurableColorPickerNullable("Color", gestalt.groupedBarsPainter.configuration::valueLabelColor)

          configurableColorPickerNullable("Stroke color", gestalt.groupedBarsPainter.configuration::valueLabelStrokeColor)

          configurableBoolean("Show tool tips", gestalt.style.showTooltipsProperty)
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
  style.showGrid = false

  style.crossWireLabelBoxStyles = CategoryModelBoxStylesProvider.onlyCategoryBoxStylesProvider(Theme.chartColors().mapped {
    BoxStyle(
      fill = it,
      borderColor = Color.white,
      borderWidth = 2.0,
      padding = CrossWireLayer.Style.DefaultLabelBoxPadding,
      radii = BorderRadius.all2,
      shadow = Shadow.LightDrop
    )
  })

  // start with visible value-labels
  groupedBarsPainter.configuration.showValueLabel = true
  // use theme-dependent bar-colors
  groupedBarsPainter.configuration.colorsProvider = CategorySeriesModelColorsProvider.onlyCategoryColorsProvider(Theme.chartColors())
}

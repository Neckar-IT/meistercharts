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
import com.meistercharts.annotations.Domain
import com.meistercharts.charts.BarChartStackedGestalt
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.VariabilityType
import com.meistercharts.demo.configurableBoolean
import com.meistercharts.demo.configurableColorPickerProvider
import com.meistercharts.demo.configurableColorPickerProviderNullable
import com.meistercharts.demo.configurableDecimals
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.configurableFont
import com.meistercharts.demo.configurableFontProvider
import com.meistercharts.demo.configurableInsetsSeparate
import com.meistercharts.demo.section
import com.meistercharts.demo.style
import com.meistercharts.model.BorderRadius
import com.meistercharts.model.category.Category
import com.meistercharts.model.category.DefaultCategorySeriesModel
import com.meistercharts.model.category.DefaultSeries
import com.meistercharts.range.ValueRange
import com.meistercharts.style.Palette
import it.neckar.open.i18n.TextKey
import it.neckar.open.provider.MultiProvider

class BarChartStackedGestaltDemoDescriptor : MeisterchartsDemoDescriptor<(gestalt: BarChartStackedGestalt) -> Unit> {
  override val uuid: Uuid = Uuid.parse("84c8b034-7dfa-4a14-892f-b5f82257a345")

  override val name: String = "Bar Chart Stacked"
  override val quality: DemoQuality = DemoQuality.High
  override val variabilityType: VariabilityType = VariabilityType.Stable

  //language=HTML
  override val description: String = "A stacked bar chart"
  override val category: DemoCategory = DemoCategory.Gestalt

  override val predefinedConfigurations: List<PredefinedConfiguration<(gestalt: BarChartStackedGestalt) -> Unit>> = listOf(
    PredefinedConfiguration(
      Uuid.parse("042a400a-626d-448e-8011-3d1fc1ecf9e8"),
      { _ ->
      },
      "default (vertical)"
    ),
    PredefinedConfiguration(
      Uuid.parse("923d7929-30b5-4c1b-8791-512c085b5419"),
      { gestalt ->
        gestalt.configuration.applyAxisTitleOnTop(40.0)
        gestalt.valueAxisLayer.configuration.titleProvider = { _, _ -> "MyValue Axis Title" }
        gestalt.configuration.applyValueAxisVisible()
      },
      "default (vertical) Title on top"
    ),
    PredefinedConfiguration(
      Uuid.parse("ed4cf46d-8764-455f-becf-5672f6f44b0c"),
      { gestalt ->
        gestalt.valueAxisLayer.configuration.titleProvider = { _, _ -> "MyValue Axis Title" }
        gestalt.configuration.applyValueAxisVisible()
      },
      "default (vertical) with Value Axis"
    ),
    PredefinedConfiguration(
      Uuid.parse("21d40613-86cf-4e27-9a17-668f781844ed"),
      { gestalt ->
        gestalt.configuration.applyHorizontalConfiguration()
        gestalt.configuration.applyAxisTitleOnTop(40.0)
        gestalt.categoryAxisLayer.configuration.titleProvider = { _, _ -> "MyValue Category Title" }
      },
      "default (horizontal) Title on top"
    ),
    PredefinedConfiguration(
      Uuid.parse("0b3bcae5-8155-42fb-b4d6-042b37a9a0d7"),
      { gestalt ->
        gestalt.configuration.applyHorizontalConfiguration()
      },
      "default (horizontal)"
    ),
    PredefinedConfiguration(
      Uuid.parse("5e9542e8-9e88-43ff-a70e-f81a1553595d"),
      { gestalt ->
        gestalt.configuration.applyHorizontalConfiguration()
        gestalt.configuration.applyValueAxisVisible()
      },
      "default (horizontal) - with value axis"
    ),
    PredefinedConfiguration(
      Uuid.parse("0ecd4d3f-101a-4c86-a001-03d1db645ea7"),
      { gestalt ->
        gestalt.configuration.categorySeriesModel = createSampleCategoryModel()
        gestalt.stackedBarsPainter.stackedBarPaintable.configuration.valueRange = ValueRange.linear(-10.0, 14.0)
        gestalt.stackedBarsPainter.stackedBarPaintable.configuration.colorsProvider = MultiProvider.forListModuloProvider(Palette.stateColors)
      },
      "positive + negative values (vertical)"
    ),
    PredefinedConfiguration(
      Uuid.parse("7866e906-ada9-4611-af65-a599e4848038"),
      { gestalt ->
        gestalt.configuration.categorySeriesModel = createSampleCategoryModel()
        gestalt.stackedBarsPainter.stackedBarPaintable.configuration.valueRange = ValueRange.linear(-10.0, 14.0)
        gestalt.stackedBarsPainter.stackedBarPaintable.configuration.colorsProvider = MultiProvider.forListModuloProvider(Palette.stateColors)
        gestalt.configuration.applyHorizontalConfiguration()
      },
      "positive + negative values (horizontal)"
    ),
    PredefinedConfiguration(
      Uuid.parse("053d69ad-8862-493d-9f2f-abf95379d257"),
      { gestalt ->
        gestalt.configuration.categorySeriesModel = createSampleCategoryModelManySmallValues()
        gestalt.stackedBarsPainter.stackedBarPaintable.configuration.valueRange = ValueRange.linear(-10.0, 10.0)
        gestalt.stackedBarsPainter.stackedBarPaintable.configuration.colorsProvider = MultiProvider.forListModuloProvider(Palette.stateColors)
        gestalt.configuration.applyHorizontalConfiguration()
      },
      "many positive + negative values (horizontal)"
    ),
    PredefinedConfiguration(
      Uuid.parse("f836da00-e69d-43b6-ba7f-e60e38a61196"),
      { gestalt ->
        gestalt.configuration.categorySeriesModel = createSampleNegativeCategoryModel()
        gestalt.stackedBarsPainter.stackedBarPaintable.configuration.valueRange = ValueRange.linear(-24.0, 0.0)
        gestalt.stackedBarsPainter.stackedBarPaintable.configuration.colorsProvider = MultiProvider.forListModuloProvider(Palette.stateColors)
      },
      "only negative values (vertical)"
    ),
    PredefinedConfiguration(
      Uuid.parse("152afd06-e8c9-4836-97b8-f2c9de81ebc5"),
      { gestalt ->
        gestalt.configuration.categorySeriesModel = createSampleNegativeCategoryModel()
        gestalt.stackedBarsPainter.stackedBarPaintable.configuration.valueRange = ValueRange.linear(-24.0, 0.0)
        gestalt.stackedBarsPainter.stackedBarPaintable.configuration.colorsProvider = MultiProvider.forListModuloProvider(Palette.stateColors)
        gestalt.configuration.applyHorizontalConfiguration()
      },
      "only negative values (horizontal)"
    ),
    PredefinedConfiguration(
      Uuid.parse("71402917-8293-498b-8860-4597de303d05"),
      { gestalt ->
        gestalt.configuration.categorySeriesModel = DefaultCategorySeriesModel(
          listOf(
            Category(TextKey.simple("Product 1")),
            Category(TextKey.simple("Product 2")),
            Category(TextKey.simple("Product 3")),
            Category(TextKey.simple("Product 4")),
            Category(TextKey.simple("Product 5")),
          ),
          listOf(
            DefaultSeries("1", listOf(0.0, 0.0, 0.0, 0.0, 0.0)),
            DefaultSeries("2", listOf(0.0, 0.0, 0.0, 0.0, 0.0)),
            DefaultSeries("3", listOf(0.0, 0.0, 0.0, 0.0, 0.0)),
            DefaultSeries("4", listOf(0.0, 0.0, 0.0, 0.0, 0.0)),
          )
        )

        gestalt.stackedBarsPainter.stackedBarPaintable.configuration.valueRange = ValueRange.linear(-24.0, 0.0)
        gestalt.stackedBarsPainter.stackedBarPaintable.configuration.colorsProvider = MultiProvider.forListModuloProvider(Palette.stateColors)
        gestalt.configuration.applyHorizontalConfiguration()
      },
      "only remainders (horizontal)"
    ),
    PredefinedConfiguration(
      Uuid.parse("e18d289d-6e10-4fe2-95d8-37a927758c73"),
      { gestalt ->
        gestalt.configuration.categorySeriesModel = DefaultCategorySeriesModel(
          listOf(
            Category(TextKey.simple("Product 1")),
            Category(TextKey.simple("Product 2")),
            Category(TextKey.simple("Product 3")),
            Category(TextKey.simple("Product 4")),
            Category(TextKey.simple("Product 5")),
          ),
          listOf(
            DefaultSeries("1", listOf(0.0, 10.0, -10.0, 10.0, 10.0)),
            DefaultSeries("2", listOf(0.0, 0.0, 0.0, -0.0, 1.0)),
            DefaultSeries("3", listOf(0.0, 0.0, 0.0, 0.0, -5.0)),
            DefaultSeries("4", listOf(0.0, 0.0, 0.0, 0.0, -5.0)),
          )
        )

        gestalt.stackedBarsPainter.stackedBarPaintable.configuration.valueRange = ValueRange.linear(-10.0, 20.0)
        gestalt.stackedBarsPainter.stackedBarPaintable.configuration.colorsProvider = MultiProvider.forListModuloProvider(Palette.stateColors)
      },
      "All combinations (Vertical)"
    ),
  )

  private fun createSampleCategoryModel(): DefaultCategorySeriesModel {
    return DefaultCategorySeriesModel(
      listOf(
        Category(TextKey.simple("Product 1")),
        Category(TextKey.simple("Product 2")),
        Category(TextKey.simple("Product 3")),
        Category(TextKey.simple("Product 4")),
        Category(TextKey.simple("Product 5")),
      ),
      listOf(
        DefaultSeries("1", listOf(-10.0, 7.0, 10.0, -5.0, 1.0)),
        DefaultSeries("2", listOf(7.0, -10.0, -5.0, 1.0, 0.0)),
        DefaultSeries("3", listOf(7.0, 5.0, -3.0, 0.0, 0.0)),
        DefaultSeries("4", listOf(0.0, 0.0, 0.0, 0.0, -2.0)),
      )
    )
  }

  /**
   * Creates many values in the range from -10 to +10
   */
  private fun createSampleCategoryModelManySmallValues(): DefaultCategorySeriesModel {
    return DefaultCategorySeriesModel(
      listOf(
        Category(TextKey.simple("Product 1")),
        Category(TextKey.simple("Product 2")),
        Category(TextKey.simple("Product 3")),
        Category(TextKey.simple("Product 4")),
        Category(TextKey.simple("Product 5")),
      ),

      buildList {
        val valuesPositive: List<@Domain Double> = buildList {
          for (i in 0 until 5) {
            add((i + 1) / 50.0)
          }
        }
        val valuesNegative: List<@Domain Double> = buildList {
          for (i in 0 until 5) {
            add(-(i + 1) / 50.0)
          }
        }

        //Create 20 series until 100 is reached
        for (i in 0 until 100) {
          //Add 5 data points to each series (one for every category)
          add(DefaultSeries("$i", valuesPositive))
          add(DefaultSeries("-$i", valuesNegative))
        }
      }
    )
  }

  private fun createSampleNegativeCategoryModel(): DefaultCategorySeriesModel {
    return DefaultCategorySeriesModel(
      listOf(
        Category(TextKey.simple("Product 1")),
        Category(TextKey.simple("Product 2")),
        Category(TextKey.simple("Product 3")),
        Category(TextKey.simple("Product 4")),
        Category(TextKey.simple("Product 5")),
      ),
      listOf(
        DefaultSeries("1", listOf(-10.0, -7.0, -10.0, -5.0, -1.0)),
        DefaultSeries("2", listOf(-7.0, -10.0, -5.0, -1.0, 0.0)),
        DefaultSeries("3", listOf(-7.0, -5.0, -3.0, 0.0, 0.0)),
        DefaultSeries("4", listOf(0.0, 0.0, 0.0, 0.0, -2.0)),
      )
    )
  }

  override fun prepareDemo(configuration: PredefinedConfiguration<(gestalt: BarChartStackedGestalt) -> Unit>?): MeisterchartsDemo {
    require(configuration != null) { "Config required" }

    return MeisterchartsDemo(this) {

      val gestalt = BarChartStackedGestalt()
      configuration.payload(gestalt)

      meistercharts {
        gestalt.configure(this)

        configure {

          configurable(property = gestalt.stackedBarsPainter.stackedBarPaintable.style::valueLabelFormat)

          configurable("Min category size", gestalt.categoryLayer.configuration.layoutCalculator.style::minCategorySize) {
            max = 1000.0
          }

          configurable("Max category size", gestalt.categoryLayer.configuration.layoutCalculator.style::maxCategorySize, 150.0) {
            max = 1000.0
          }

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

          configurableDouble("Value range start", gestalt.stackedBarsPainter.stackedBarPaintable.configuration.valueRange.start) {
            min = -100.0
            max = 100.0
            onChange {
              gestalt.stackedBarsPainter.stackedBarPaintable.configuration.valueRange = gestalt.stackedBarsPainter.stackedBarPaintable.configuration.valueRange.withStart(it)
              markAsDirty()
            }
          }

          configurableDouble("Value range end", gestalt.stackedBarsPainter.stackedBarPaintable.configuration.valueRange.end) {
            min = -100.0
            max = 100.0
            onChange {
              gestalt.stackedBarsPainter.stackedBarPaintable.configuration.valueRange = gestalt.stackedBarsPainter.stackedBarPaintable.configuration.valueRange.withEnd(it)
              markAsDirty()
            }
          }

          configurable("Segments gap", gestalt.stackedBarsPainter.stackedBarPaintable.style::segmentsGap) {
            min = 0.0
            max = 10.0
          }

          configurable("Grid-0", gestalt.configuration::showGrid) { }

          configurableDouble("Segment radii", gestalt.stackedBarsPainter.stackedBarPaintable.configuration.segmentRadii.topLeft) {
            min = 0.0
            max = 10.0
            onChange {
              gestalt.stackedBarsPainter.stackedBarPaintable.configuration.segmentRadii = BorderRadius.of(it)
              markAsDirty()
            }
          }

          section("Remainder")
          configurable("Show as segment", gestalt.stackedBarsPainter.stackedBarPaintable.style::showRemainderAsSegment) { }
          configurable("Background", gestalt.stackedBarsPainter.stackedBarPaintable.style::remainderSegmentBackgroundColor) { }
          this@MeisterchartsDemo.configurable("Border color", gestalt.stackedBarsPainter.stackedBarPaintable.style::remainderSegmentBorderColor) { }
          configurable("Border width", gestalt.stackedBarsPainter.stackedBarPaintable.style::remainderSegmentBorderLineWidth) { max = 10.0 }

          section("Border")
          configurable("Show", gestalt.stackedBarsPainter.stackedBarPaintable.style::showBorder)
          this@MeisterchartsDemo.configurable("Border color", gestalt.stackedBarsPainter.stackedBarPaintable.style::borderColor) { }
          configurable("Border width", gestalt.stackedBarsPainter.stackedBarPaintable.style::borderLineWidth) { max = 10.0 }

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

          this@MeisterchartsDemo.configurable("Axis line color", gestalt.categoryAxisLayer.configuration::lineColor) {
          }

          configurable("Axis line width", gestalt.categoryAxisLayer.configuration::axisLineWidth) {
            max = 10.0
          }

          configurableFont("Value label font", gestalt.stackedBarsPainter.stackedBarPaintable.configuration.valueLabelFont) {
            onChange {
              gestalt.configuration.applyValueLabelFont(it)
              markAsDirty()
            }
          }

          configurable("Value label color", gestalt.stackedBarsPainter.stackedBarPaintable.style::valueLabelColor)


          configurableSeparate("Content Viewport Margin", gestalt::contentViewportMargin)
        }
      }
    }
  }
}

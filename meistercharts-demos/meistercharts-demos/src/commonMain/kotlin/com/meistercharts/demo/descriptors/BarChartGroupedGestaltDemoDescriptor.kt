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
import com.meistercharts.algorithms.layers.HudElementIndex
import com.meistercharts.algorithms.layers.debug.MarkAsDirtyLayer
import com.meistercharts.algorithms.layers.linechart.LineStyle
import com.meistercharts.algorithms.model.Category
import com.meistercharts.algorithms.model.CategorySeriesModel
import com.meistercharts.algorithms.model.DefaultCategorySeriesModel
import com.meistercharts.algorithms.model.DefaultSeries
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.annotations.Domain
import com.meistercharts.charts.BarChartGroupedGestalt
import com.meistercharts.demo.CategoryModelFactory
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.CylinderPressureCategoryModelFactory
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableBoolean
import com.meistercharts.demo.configurableColorPickerNullable
import com.meistercharts.demo.configurableDecimalsFormat
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.configurableEnum
import com.meistercharts.demo.configurableFont
import com.meistercharts.demo.configurableFontProvider
import com.meistercharts.demo.configurableInsetsSeparate
import com.meistercharts.demo.configurableSize
import com.meistercharts.demo.configurableValueRange
import com.meistercharts.demo.section
import com.meistercharts.demo.style
import com.meistercharts.model.Side
import com.meistercharts.model.Size
import it.neckar.open.provider.DoublesProvider
import it.neckar.open.provider.MultiProvider
import it.neckar.open.formatting.decimalFormat2digits
import it.neckar.open.i18n.TextKey
import com.meistercharts.style.Palette

class BarChartGroupedGestaltDemoDescriptor : ChartingDemoDescriptor<(gestalt: BarChartGroupedGestalt) -> Unit> {

  override val name: String = "Bar Chart Grouped"

  //language=HTML
  override val description: String = "Bar Chart Grouped"
  override val category: DemoCategory = DemoCategory.Gestalt

  private val defaultCategorySeriesModelFactory: CategoryModelFactory = object : CategoryModelFactory {
    override fun createModel(): CategorySeriesModel {
      return BarChartGroupedGestalt.createDefaultCategoryModel()
    }

    override fun createValueRange(): ValueRange {
      return BarChartGroupedGestalt.defaultValueRange
    }
  }

  private val valuesOutsideModelFactory: CategoryModelFactory = object : CategoryModelFactory {
    override fun createModel(): CategorySeriesModel {
      return BarChartGroupedGestalt.createValuesOutsideCategoryModel()
    }

    override fun createValueRange(): ValueRange {
      return BarChartGroupedGestalt.defaultValueRange
    }
  }

  private val valuesSomeInvalidModelFactory: CategoryModelFactory = object : CategoryModelFactory {
    override fun createModel(): CategorySeriesModel {
      return BarChartGroupedGestalt.createSomeInvalidCategoryModel()
    }

    override fun createValueRange(): ValueRange {
      return BarChartGroupedGestalt.defaultValueRange
    }
  }

  override val predefinedConfigurations: List<PredefinedConfiguration<(gestalt: BarChartGroupedGestalt) -> Unit>> = listOf(
    PredefinedConfiguration(
      { gestalt ->
        gestalt.configuration.categorySeriesModel = defaultCategorySeriesModelFactory.createModel()
        gestalt.style.valueRange = defaultCategorySeriesModelFactory.createValueRange()
        gestalt.configuration.thresholdValues = DoublesProvider.forValues(10.0, 44.0)
      },
      "default (vertical)"
    ),
    PredefinedConfiguration(
      { gestalt ->
        gestalt.configuration.categorySeriesModel = valuesSomeInvalidModelFactory.createModel()
        gestalt.style.valueRange = valuesOutsideModelFactory.createValueRange()
        gestalt.configuration.thresholdValues = DoublesProvider.forValues(-10.0, 30.0, 51.0, 99.0)
      },
      "vertical - invalid values"
    ),
    PredefinedConfiguration(
      { gestalt ->
        gestalt.configuration.categorySeriesModel = valuesOutsideModelFactory.createModel()
        gestalt.style.valueRange = valuesOutsideModelFactory.createValueRange()
        gestalt.configuration.thresholdValues = DoublesProvider.forValues(10.0, Double.NaN, Double.NaN, 99.0)
      },
      "vertical - threshold values outside"
    ),
    PredefinedConfiguration(
      { gestalt ->
        gestalt.configuration.categorySeriesModel = defaultCategorySeriesModelFactory.createModel()
        gestalt.style.valueRange = defaultCategorySeriesModelFactory.createValueRange()
        gestalt.style.applyAxisTitleOnTop()

        gestalt.valueAxisLayer.style.titleProvider = { _, _ -> "Bar Values" }
      },
      "default (vertical) - top title"
    ),
    PredefinedConfiguration(
      { gestalt ->
        gestalt.configuration.categorySeriesModel = defaultCategorySeriesModelFactory.createModel()
        gestalt.style.valueRange = ValueRange.logarithmic(0.1, 55.0)
        gestalt.valueAxisLayer.style.applyLogarithmicScale()
        gestalt.valueAxisLayer.style.ticksFormat = decimalFormat2digits
      },
      "logarithmic axis"
    ),
    PredefinedConfiguration(
      { gestalt ->
        gestalt.configuration.categorySeriesModel = defaultCategorySeriesModelFactory.createModel()
        gestalt.style.valueRange = defaultCategorySeriesModelFactory.createValueRange()
        gestalt.style.applyHorizontalConfiguration()
      },
      "default (horizontal)"
    ),
    PredefinedConfiguration(
      { gestalt ->
        gestalt.configuration.categorySeriesModel = valuesOutsideModelFactory.createModel()
        gestalt.style.valueRange = valuesOutsideModelFactory.createValueRange()
        gestalt.style.applyHorizontalConfiguration()
      },
      "default (horizontal) - values outside"
    ),
    PredefinedConfiguration(
      { gestalt ->
        configureForNegativeValuesOnly(gestalt)
      },
      "only negative values (vertical)"
    ),
    PredefinedConfiguration(
      { gestalt ->
        configureForNegativeValuesOnly(gestalt)
        gestalt.style.applyHorizontalConfiguration()
      },
      "only negative values (horizontal)"
    ),
    PredefinedConfiguration(
      { gestalt ->
        configureForNegativeAndPositiveValues(gestalt)
      },
      "positive + negative values (vertical)"
    ),
    PredefinedConfiguration(
      { gestalt ->
        configureForNegativeAndPositiveValues(gestalt)
        gestalt.style.applyHorizontalConfiguration()
      },
      "positive + negative values (horizontal)"
    ),
    PredefinedConfiguration(
      { gestalt ->
        gestalt.configuration.categorySeriesModel = CylinderPressureCategoryModelFactory.createModel()
        gestalt.style.valueRange = CylinderPressureCategoryModelFactory.createValueRange()

        gestalt.configure {
          //always repaint!
          this.layers.addLayer(MarkAsDirtyLayer())
        }
      },
      "cylinder pressure (vertical)"
    ),
    PredefinedConfiguration(
      { gestalt ->
        gestalt.configuration.categorySeriesModel = CylinderPressureCategoryModelFactory.createModel()
        gestalt.style.valueRange = CylinderPressureCategoryModelFactory.createValueRange()
        gestalt.style.applyHorizontalConfiguration()

        gestalt.configure {
          //always repaint!
          this.layers.addLayer(MarkAsDirtyLayer())
        }
      },
      "cylinder pressure (horizontal)"
    ),
    PredefinedConfiguration(
      { gestalt ->
        configureForNegativeAndPositiveValues(gestalt)
        gestalt.valueAxisLayer.style.hideAxisLine()
        gestalt.valueAxisLayer.style.hideTicks()
        gestalt.categoryAxisLayer.style.hideAxisLine()
        gestalt.categoryAxisLayer.style.hideTicks()

        val lineStyleZeroLine = LineStyle(color = Color.gray) //avoid instantiation in lambda
        val lineStyleDefault = LineStyle(color = Color.lightgray) //avoid instantiation in lambda

        gestalt.style.gridLineStyles = { value: @Domain Double ->
          if (value == 0.0) {
            lineStyleZeroLine
          } else {
            lineStyleDefault
          }
        }
      },
      "special grid (vertical)"
    ),
    PredefinedConfiguration(
      { gestalt ->
        configureForNegativeAndPositiveValues(gestalt)
        gestalt.valueAxisLayer.style.hideAxisLine()
        gestalt.valueAxisLayer.style.hideTicks()
        gestalt.categoryAxisLayer.style.hideAxisLine()
        gestalt.categoryAxisLayer.style.hideTicks()

        val lineStyleZeroLine = LineStyle(color = Color.gray) //avoid instantiation in lambda
        val lineStyleDefault = LineStyle(color = Color.lightgray) //avoid instantiation in lambda

        gestalt.style.gridLineStyles = { value: @Domain Double ->
          if (value == 0.0) {
            lineStyleZeroLine
          } else {
            lineStyleDefault
          }
        }
        gestalt.style.applyHorizontalConfiguration()
      },
      "special grid (horizontal)"
    ),
  )

  override fun createDemo(configuration: PredefinedConfiguration<(gestalt: BarChartGroupedGestalt) -> Unit>?): ChartingDemo {
    require(configuration != null) { "configuration must not be null" }

    return ChartingDemo {

      val gestalt = BarChartGroupedGestalt()
      configuration.payload(gestalt)

      meistercharts {
        gestalt.configure(this)

        val configuration = object {
          var labelColor = Palette.defaultGray
        }

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
            max = 100.0
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

          configurableBoolean("Show value labels", gestalt.groupedBarsPainter.configuration::showValueLabel)

          configurableColorPickerNullable("Value label color", gestalt.groupedBarsPainter.configuration::valueLabelColor)

          configurableColorPickerNullable("Value label stroke color", gestalt.groupedBarsPainter.configuration::valueLabelStrokeColor)

          configurableFont("Value label font", gestalt.groupedBarsPainter.configuration::valueLabelFont)


          section("Tooltips")

          gestalt.balloonTooltipSupport.tooltipContentPaintable.delegate.configuration.maxLabelWidth = 300.0 //is set to Double.MAX_VALUE by default
          configurableDouble("Label Width", gestalt.balloonTooltipSupport.tooltipContentPaintable.delegate.configuration::maxLabelWidth) {
            max = 300.0
          }

          configurableSize("Symbol Size", Size.PX_16) {
            onChange {
              gestalt.configuration.applyBalloonTooltipSize(it)
              markAsDirty()
            }
          }

          configurableDouble("Symbol Label Gap", gestalt.balloonTooltipSupport.tooltipContentPaintable.delegate.configuration::symbolLabelGap) {
            max = 20.0
          }

          configurableDouble("Entries Gap", gestalt.balloonTooltipSupport.tooltipContentPaintable.delegate.configuration::entriesGap) {
            max = 50.0
          }

          configurableFontProvider("Tooltip Font", gestalt.balloonTooltipSupport.tooltipContentPaintable.delegate.configuration::textFont)
        }
      }
    }
  }

  companion object {
    private fun configureForNegativeValuesOnly(gestalt: BarChartGroupedGestalt) {
      gestalt.configuration.categorySeriesModel = DefaultCategorySeriesModel(
        listOf(
          Category(TextKey.simple("A")),
          Category(TextKey.simple("B")),
          Category(TextKey.simple("C")),
        ),
        listOf(
          DefaultSeries("1", listOf(-10.0, -20.0, -30.0)),
          DefaultSeries("2", listOf(-10.0, -20.0, -30.0)),
          DefaultSeries("3", listOf(-10.0, -20.0, -30.0)),
        )
      )
      gestalt.configuration.thresholdValues = DoublesProvider.forValues(-28.0, -8.0)

      gestalt.configuration.thresholdLabels = MultiProvider.forListOrException<HudElementIndex, List<String>>(
        listOf(
          listOf("Min ${BarChartGroupedGestalt.defaultNumberFormat.format(-28.0)}"),
          listOf("Max ${BarChartGroupedGestalt.defaultNumberFormat.format(-8.0)}"),
        )
      )
      gestalt.style.valueRange = ValueRange.linear(-33.0, 0.0)
    }

    private fun configureForNegativeAndPositiveValues(gestalt: BarChartGroupedGestalt) {
      gestalt.configuration.categorySeriesModel = DefaultCategorySeriesModel(
        listOf(
          Category(TextKey.simple("A")),
          Category(TextKey.simple("B")),
          Category(TextKey.simple("C")),
        ),
        listOf(
          DefaultSeries("1", listOf(-10.0, 20.0, -30.0)),
          DefaultSeries("2", listOf(-10.0, -20.0, 30.0)),
          DefaultSeries("3", listOf(10.0, -20.0, -30.0)),
        )
      )
      gestalt.configuration.thresholdValues = DoublesProvider.forValues(-28.0, -8.0, 8.0, 28.0)
      gestalt.configuration.thresholdLabels = MultiProvider.forListOrException<HudElementIndex, List<String>>(
        listOf(
          listOf("Min ${BarChartGroupedGestalt.defaultNumberFormat.format(-28.0)}"),
          listOf("Max ${BarChartGroupedGestalt.defaultNumberFormat.format(-8.0)}"),
          listOf("Min ${BarChartGroupedGestalt.defaultNumberFormat.format(8.0)}"),
          listOf("Max ${BarChartGroupedGestalt.defaultNumberFormat.format(28.0)}"),
        )
      )
      gestalt.style.valueRange = ValueRange.linear(-33.0, 33.0)
    }
  }
}


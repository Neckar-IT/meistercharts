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
import com.meistercharts.algorithms.layers.axis.HudElementIndex
import com.meistercharts.algorithms.layers.debug.MarkAsDirtyLayer
import com.meistercharts.algorithms.layers.linechart.LineStyle
import com.meistercharts.annotations.Domain
import com.meistercharts.charts.BarChartGroupedGestalt
import com.meistercharts.color.Color
import com.meistercharts.demo.CategoryModelFactory
import com.meistercharts.demo.CylinderPressureCategoryModelFactory
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.VariabilityType
import com.meistercharts.demo.configurableBoolean
import com.meistercharts.demo.configurableColorPickerProviderNullable
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
import com.meistercharts.model.category.Category
import com.meistercharts.model.category.CategorySeriesModel
import com.meistercharts.model.category.DefaultCategorySeriesModel
import com.meistercharts.model.category.DefaultSeries
import com.meistercharts.range.ValueRange
import com.meistercharts.style.Palette
import it.neckar.geometry.Side
import it.neckar.geometry.Size
import it.neckar.open.formatting.decimalFormat2digits
import it.neckar.open.i18n.TextKey
import it.neckar.open.provider.DoublesProvider
import it.neckar.open.provider.MultiProvider

class BarChartGroupedGestaltDemoDescriptor : MeisterchartsDemoDescriptor<(gestalt: BarChartGroupedGestalt) -> Unit> {
  override val uuid: Uuid = Uuid.parse("2122eaf3-ca11-4d02-a427-60168f3ed9d5")

  override val name: String = "Bar Chart Grouped"

  override val quality: DemoQuality = DemoQuality.High
  override val variabilityType: VariabilityType = VariabilityType.Stable

  //language=HTML
  override val description: String = "A grouped bar chart"
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
      Uuid.parse("6b04333b-6083-429f-b27f-d2d49c450ae0"),
      { gestalt ->
        gestalt.configuration.categorySeriesModel = defaultCategorySeriesModelFactory.createModel()
        gestalt.configuration.valueRange = defaultCategorySeriesModelFactory.createValueRange()
        gestalt.configuration.thresholdValues = DoublesProvider.forValues(10.0, 44.0)
      },
      "default (vertical)"
    ),
    PredefinedConfiguration(
      Uuid.parse("45f5d2b1-3ba4-4f9a-adbb-2b78ee8f7ca9"),
      { gestalt ->
        gestalt.configuration.categorySeriesModel = valuesSomeInvalidModelFactory.createModel()
        gestalt.configuration.valueRange = valuesOutsideModelFactory.createValueRange()
        gestalt.configuration.thresholdValues = DoublesProvider.forValues(-10.0, 30.0, 51.0, 99.0)
      },
      "vertical - invalid values"
    ),
    PredefinedConfiguration(
      Uuid.parse("ec413d5e-a345-4a3a-83f1-ac89b38a5850"),
      { gestalt ->
        gestalt.configuration.categorySeriesModel = valuesOutsideModelFactory.createModel()
        gestalt.configuration.valueRange = valuesOutsideModelFactory.createValueRange()
        gestalt.configuration.thresholdValues = DoublesProvider.forValues(10.0, Double.NaN, Double.NaN, 99.0)
      },
      "vertical - threshold values outside"
    ),
    PredefinedConfiguration(
      Uuid.parse("4ef9f092-dfd0-4147-b72a-509b299d2df7"),
      { gestalt ->
        gestalt.configuration.categorySeriesModel = defaultCategorySeriesModelFactory.createModel()
        gestalt.configuration.valueRange = defaultCategorySeriesModelFactory.createValueRange()
        gestalt.configuration.applyAxisTitleOnTop()

        gestalt.valueAxisLayer.configuration.titleProvider = { _, _ -> "Bar Values" }
      },
      "default (vertical) - top title"
    ),
    PredefinedConfiguration(
      Uuid.parse("c8042401-2e88-4a7a-875a-da60a8c30d02"),
      { gestalt ->
        gestalt.configuration.categorySeriesModel = defaultCategorySeriesModelFactory.createModel()
        gestalt.configuration.valueRange = ValueRange.logarithmic(0.1, 55.0)
        gestalt.valueAxisLayer.configuration.applyLogarithmicScale()
        gestalt.valueAxisLayer.configuration.ticksFormat = decimalFormat2digits
      },
      "logarithmic axis"
    ),
    PredefinedConfiguration(
      Uuid.parse("e3ee8912-6b59-4315-9125-a1d37a71bcc5"),
      { gestalt ->
        gestalt.configuration.categorySeriesModel = defaultCategorySeriesModelFactory.createModel()
        gestalt.configuration.valueRange = defaultCategorySeriesModelFactory.createValueRange()
        gestalt.configuration.applyHorizontalConfiguration()
      },
      "default (horizontal)"
    ),
    PredefinedConfiguration(
      Uuid.parse("0c17b750-973b-406e-a0ff-3df4e93aaa3f"),
      { gestalt ->
        gestalt.configuration.categorySeriesModel = valuesOutsideModelFactory.createModel()
        gestalt.configuration.valueRange = valuesOutsideModelFactory.createValueRange()
        gestalt.configuration.applyHorizontalConfiguration()
      },
      "default (horizontal) - values outside"
    ),
    PredefinedConfiguration(
      Uuid.parse("377a8af8-e16a-4dc8-8153-939b5b4dff22"),
      { gestalt ->
        configureForNegativeValuesOnly(gestalt)
      },
      "only negative values (vertical)"
    ),
    PredefinedConfiguration(
      Uuid.parse("18476c66-a6c1-47d1-a7ec-a8145bd4033f"),
      { gestalt ->
        configureForNegativeValuesOnly(gestalt)
        gestalt.configuration.applyHorizontalConfiguration()
      },
      "only negative values (horizontal)"
    ),
    PredefinedConfiguration(
      Uuid.parse("0fb09e2d-0eb0-4e42-9391-2686f90ed0dd"),
      { gestalt ->
        configureForNegativeAndPositiveValues(gestalt)
      },
      "positive + negative values (vertical)"
    ),
    PredefinedConfiguration(
      Uuid.parse("c9559984-180f-4082-acbc-758884af26b3"),
      { gestalt ->
        configureForNegativeAndPositiveValues(gestalt)
        gestalt.configuration.applyHorizontalConfiguration()
      },
      "positive + negative values (horizontal)"
    ),
    PredefinedConfiguration(
      Uuid.parse("4e0eaba6-d195-4c45-bb5f-153731b4c637"),
      { gestalt ->
        gestalt.configuration.categorySeriesModel = CylinderPressureCategoryModelFactory.createModel()
        gestalt.configuration.valueRange = CylinderPressureCategoryModelFactory.createValueRange()

        gestalt.configure {
          //always repaint!
          this.layers.addLayer(MarkAsDirtyLayer())
        }
      },
      "cylinder pressure (vertical)"
    ),
    PredefinedConfiguration(
      Uuid.parse("0b6f11b4-77c2-4511-9c7e-c1e182f65504"),
      { gestalt ->
        gestalt.configuration.categorySeriesModel = CylinderPressureCategoryModelFactory.createModel()
        gestalt.configuration.valueRange = CylinderPressureCategoryModelFactory.createValueRange()
        gestalt.configuration.applyHorizontalConfiguration()

        gestalt.configure {
          //always repaint!
          this.layers.addLayer(MarkAsDirtyLayer())
        }
      },
      "cylinder pressure (horizontal)"
    ),
    PredefinedConfiguration(
      Uuid.parse("385f42ee-ca56-4d90-ac52-1eadbcccc1ce"),
      { gestalt ->
        configureForNegativeAndPositiveValues(gestalt)
        gestalt.valueAxisLayer.configuration.hideAxisLine()
        gestalt.valueAxisLayer.configuration.hideTicks()
        gestalt.categoryAxisLayer.configuration.hideAxisLine()
        gestalt.categoryAxisLayer.configuration.hideTicks()

        val lineStyleZeroLine = LineStyle(color = Color.gray) //avoid instantiation in lambda
        val lineStyleDefault = LineStyle(color = Color.lightgray) //avoid instantiation in lambda

        gestalt.configuration.gridLineStyles = { value: @Domain Double ->
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
      Uuid.parse("31b3f147-5937-4b6e-a647-bde15a9f52f8"),
      { gestalt ->
        configureForNegativeAndPositiveValues(gestalt)
        gestalt.valueAxisLayer.configuration.hideAxisLine()
        gestalt.valueAxisLayer.configuration.hideTicks()
        gestalt.categoryAxisLayer.configuration.hideAxisLine()
        gestalt.categoryAxisLayer.configuration.hideTicks()

        val lineStyleZeroLine = LineStyle(color = Color.gray) //avoid instantiation in lambda
        val lineStyleDefault = LineStyle(color = Color.lightgray) //avoid instantiation in lambda

        gestalt.configuration.gridLineStyles = { value: @Domain Double ->
          if (value == 0.0) {
            lineStyleZeroLine
          } else {
            lineStyleDefault
          }
        }
        gestalt.configuration.applyHorizontalConfiguration()
      },
      "special grid (horizontal)"
    ),
  )

  override fun prepareDemo(configuration: PredefinedConfiguration<(gestalt: BarChartGroupedGestalt) -> Unit>?): MeisterchartsDemo {
    require(configuration != null) { "configuration must not be null" }

    return MeisterchartsDemo(this) {

      val gestalt = BarChartGroupedGestalt()
      configuration.payload(gestalt)

      meistercharts {
        gestalt.configure(this)

        val configuration = object {
          val labelColor = Palette.defaultGray
        }

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


          section("Tooltips")

          gestalt.balloonTooltipSupport.tooltipContentPaintable.delegate.configuration.maxLabelWidth = 300.0 //is set to Double.MAX_VALUE by default
          configurable("Label Width", gestalt.balloonTooltipSupport.tooltipContentPaintable.delegate.configuration::maxLabelWidth) {
            max = 300.0
          }

          configurableSize("Symbol Size", Size.PX_16) {
            onChange {
              gestalt.configuration.applyBalloonTooltipSize(it)
              markAsDirty()
            }
          }

          configurable("Symbol Label Gap", gestalt.balloonTooltipSupport.tooltipContentPaintable.delegate.configuration::symbolLabelGap) {
            max = 20.0
          }

          configurable("Entries Gap", gestalt.balloonTooltipSupport.tooltipContentPaintable.delegate.configuration::entriesGap) {
            max = 50.0
          }

          configurable("Tooltip Font", gestalt.balloonTooltipSupport.tooltipContentPaintable.delegate.configuration::textFont)
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
      gestalt.configuration.valueRange = ValueRange.linear(-33.0, 0.0)
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
      gestalt.configuration.valueRange = ValueRange.linear(-33.0, 33.0)
    }
  }
}


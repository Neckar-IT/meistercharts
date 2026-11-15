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
import com.meistercharts.demo.section
import kotlin.uuid.Uuid
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.barchart.CategoryChartOrientation
import com.meistercharts.algorithms.layers.barchart.CategoryDebugPainter
import com.meistercharts.algorithms.layers.barchart.CategoryLayer
import com.meistercharts.algorithms.layers.barchart.CategoryPainter
import com.meistercharts.algorithms.layers.barchart.DebugCategoryAxisLabelPainter
import com.meistercharts.algorithms.layers.barchart.DefaultCategoryAxisLabelPainter
import com.meistercharts.algorithms.layers.barchart.GroupedBarsPainter
import com.meistercharts.algorithms.layers.barchart.StackedBarsPainter
import com.meistercharts.algorithms.layers.barchart.createAxisLayer
import com.meistercharts.algorithms.layers.debug.ContentAreaDebugLayer
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.VariabilityType
import com.meistercharts.demo.configurableBoolean
import com.meistercharts.demo.configurableColorPickerProviderNullable
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.configurableEnum
import com.meistercharts.demo.configurableFont
import com.meistercharts.demo.configurableFontProvider
import com.meistercharts.demo.configurableList
import com.meistercharts.demo.style
import com.meistercharts.model.Insets
import com.meistercharts.model.category.Category
import com.meistercharts.model.category.CategoryIndex
import com.meistercharts.model.category.CategorySeriesModel
import com.meistercharts.model.category.DefaultCategorySeriesModel
import com.meistercharts.model.category.DefaultSeries
import com.meistercharts.model.category.Series
import com.meistercharts.model.category.SeriesIndex
import com.meistercharts.range.LinearValueRange
import com.meistercharts.range.ValueRange
import com.meistercharts.resize.ResetToDefaultsOnWindowResize
import com.meistercharts.style.Palette
import com.meistercharts.zoom.FittingWithMargin
import it.neckar.geometry.Orientation
import it.neckar.geometry.Side
import it.neckar.open.i18n.TextKey
import it.neckar.open.kotlin.lang.asProvider
import it.neckar.open.kotlin.lang.or0ifNaN
import it.neckar.open.provider.MultiProvider

class CategoryChartLayerDemoDescriptor : MeisterchartsDemoDescriptor<Nothing> {
  override val uuid: Uuid = Uuid.parse("ce9e5f99-4c6f-47cb-aae6-6c0014d1d5bf")
  override val name: String = "Category Chart"
  override val quality: DemoQuality = DemoQuality.High
  override val variabilityType: VariabilityType = VariabilityType.Stable

  //language=HTML
  override val description: String = "The category chart layer"
  override val category: DemoCategory = DemoCategory.Layers

  override fun prepareDemo(configuration: PredefinedConfiguration<Nothing>?): MeisterchartsDemo {
    return MeisterchartsDemo(this) {

      meistercharts {
        zoomAndTranslationDefaults {
          FittingWithMargin(Insets.of(100.0))
        }

        configure {
          chartSupport.windowResizeBehavior = ResetToDefaultsOnWindowResize

          layers.addClearBackground()
          layers.addLayer(ContentAreaDebugLayer())

          val categories = listOf(
            Category(TextKey("2018", "2018")),
            Category(TextKey("2019", "2019")),
            Category(TextKey("2020", "2020"))
          )
          val series = listOf(
            DefaultSeries("Gorillas", listOf(10.0, 0.0, 12.0)),
            DefaultSeries("Giraffen", listOf(5.0, 3.0, 5.0)),
            DefaultSeries("Erdmännchen", listOf(20.0, 8.0, 4.0)),
            DefaultSeries("Zebras", listOf(4.0, 3.0, 3.0))
          )
          val model = DefaultCategorySeriesModel(categories, series)

          val groupedBarsPainter = GroupedBarsPainter {
            valueRangeProvider = { CategoryChartDemoHelper.calculateValueRangeForGroupedBars(model) }
          }

          val stackedBarsPainter = StackedBarsPainter().apply {
            stackedBarPaintable.configuration.colorsProvider = MultiProvider.forListModuloProvider(Palette.chartColors)
            stackedBarPaintable.configuration.valueRange = CategoryChartDemoHelper.calculateValueRangeForStackedBars(model)
          }

          val categoryPainters: List<CategoryPainter<CategorySeriesModel>> = listOf(groupedBarsPainter, stackedBarsPainter, CategoryDebugPainter())

          val defaultCategoryAxisPainter = DefaultCategoryAxisLabelPainter()
          val axisPainters = listOf(defaultCategoryAxisPainter, DebugCategoryAxisLabelPainter())

          val layer = CategoryLayer<CategorySeriesModel>(CategoryLayer.Configuration<CategorySeriesModel> { model }) {
            categoryPainter = groupedBarsPainter
          }
          layers.addLayer(layer)

          val categoryAxisLayer = layer.createAxisLayer {
            axisLabelPainter = defaultCategoryAxisPainter
          }
          layers.addLayer(categoryAxisLayer)

          configurable("Orientation", layer.configuration::orientation, kotlin.enums.enumEntries<CategoryChartOrientation>().toList()) {
            onChange {
              categoryAxisLayer.configuration.side = when (it.categoryOrientation) {
                Orientation.Vertical -> Side.Bottom
                Orientation.Horizontal -> Side.Left
              }
              markAsDirty()
            }
          }
          configurable("Min category size", layer.configuration.layoutCalculator.style::minCategorySize, {
            max = 1000.0
          })

          configurable("Max category size", layer.configuration.layoutCalculator.style::maxCategorySize, 150.0) {
            max = 1000.0
          }

          configurableList("Category Painter", layer.configuration.categoryPainter, categoryPainters) {
            this.converter = { painter ->
              painter::class.simpleName ?: painter.toString()
            }
            onChange {
              layer.configuration.categoryPainter = it
              markAsDirty()
            }
          }

          configurableList("Axis Painter", categoryAxisLayer.configuration.axisLabelPainter, axisPainters) {
            this.converter = { painter ->
              painter::class.simpleName ?: painter.toString()
            }
            onChange {
              categoryAxisLayer.configuration.axisLabelPainter = it
              markAsDirty()
            }
          }

          section("Bars Painter")

          configurable("Bar gap", groupedBarsPainter.configuration::barGap) {
            max = 100.0
            onChange {
              groupedBarsPainter.configuration.barGap = it
              stackedBarsPainter.stackedBarPaintable.configuration.segmentsGap = it
              markAsDirty()
            }
          }

          configurable("Show value labels", groupedBarsPainter.configuration::showValueLabel) {
            onChange {
              stackedBarsPainter.stackedBarPaintable.configuration.showValueLabels = it
              markAsDirty()
            }
          }

          configurable("Value labels font", groupedBarsPainter.configuration::valueLabelFont) {
            onChange {
              stackedBarsPainter.stackedBarPaintable.configuration.valueLabelFont = it
              markAsDirty()
            }
          }

          configurable("Value labels color", groupedBarsPainter.configuration::valueLabelColor) {
            onChange {
              stackedBarsPainter.stackedBarPaintable.configuration.valueLabelColor = it.asProvider()
              markAsDirty()
            }
          }

          configurable("Value labels gap (Horizontal)", groupedBarsPainter.configuration::valueLabelAnchorGapHorizontal) {
            max = 100.0
            onChange {
              stackedBarsPainter.stackedBarPaintable.configuration.valueLabelGapHorizontal = it
              markAsDirty()
            }
          }
          configurable("Value labels gap (Vertical)", groupedBarsPainter.configuration::valueLabelAnchorGapVertical) {
            max = 100.0
            onChange {
              stackedBarsPainter.stackedBarPaintable.configuration.valueLabelGapVertical = it
              markAsDirty()
            }
          }

          section("Category Axis")

          configurable("label in category", defaultCategoryAxisPainter.style::labelWithinCategory)
          configurable("label in category gap", defaultCategoryAxisPainter.style::labelWithinCategoryGap) {
            max = 50.0
          }

          configurable("Category axis font", categoryAxisLayer.configuration::tickFont) {
          }
        }
      }
    }
  }

}

object CategoryChartDemoHelper {
  /**
   * Computes the value-range for this chart when using stacked bars.
   */
  fun calculateValueRangeForStackedBars(model: CategorySeriesModel): LinearValueRange {
    if (model.isEmpty()) {
      return ValueRange.linear(0.0, 0.0)
    }
    var maxStackValue = 0.0
    for (categoryIndex in 0 until model.numberOfCategories) {
      var stackValue = 0.0
      for (seriesIndex in 0 until model.numberOfSeries) {
        stackValue += model.valueAt(CategoryIndex(categoryIndex), SeriesIndex(seriesIndex)).or0ifNaN()
      }
      maxStackValue = maxStackValue.coerceAtLeast(stackValue)
    }
    val start = 0.0.coerceAtMost(maxStackValue)
    val end = 0.0.coerceAtLeast(maxStackValue)
    return ValueRange.linear(start, end)
  }

  /**
   * Computes the value-range for this chart when using grouped bars
   *
   * The value range starts with the smallest start of all series value ranges
   * and ends with the largest end of all series value ranges.
   */
  fun calculateValueRangeForGroupedBars(
    /**
     * The model. Do *not* weaken this to the interface, since the method [DefaultCategorySeriesModel.seriesAt] is required,
     * which is not available in the interface
     */
    model: DefaultCategorySeriesModel,
  ): ValueRange {
    if (model.isEmpty()) {
      return ValueRange.linear(0.0, 0.0)
    }
    var start = 0.0
    var end = 0.0
    for (seriesIndex in 0 until model.numberOfSeries) {
      calculateValueRange(model.seriesAt(SeriesIndex(seriesIndex))).also {
        start = start.coerceAtMost(it.start)
        end = end.coerceAtLeast(it.end)
      }
    }
    return ValueRange.linear(start, end)
  }

  /**
   * Computes the value-range for [series]
   *
   * The the value range starts with the minimum value of [series] and ends with the maximum value of [series]
   */
  private fun calculateValueRange(series: Series): ValueRange {
    if (series.size() == 0) {
      return ValueRange.linear(0.0, 0.0)
    }
    var start = 0.0
    var end = 0.0
    for (index in 0 until series.size()) {
      series.valueAt(index).also {
        start = start.coerceAtMost(it)
        end = end.coerceAtLeast(it)
      }
    }
    return ValueRange.linear(start, end)
  }
}

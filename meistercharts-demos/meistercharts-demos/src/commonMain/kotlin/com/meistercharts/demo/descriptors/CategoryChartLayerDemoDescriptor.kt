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

import com.meistercharts.algorithms.LinearValueRange
import com.meistercharts.algorithms.ResetToDefaultsOnWindowResize
import com.meistercharts.algorithms.ValueRange
import com.meistercharts.algorithms.impl.FittingWithMargin
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
import com.meistercharts.algorithms.model.Category
import com.meistercharts.algorithms.model.CategoryIndex
import com.meistercharts.algorithms.model.CategorySeriesModel
import com.meistercharts.algorithms.model.DefaultCategorySeriesModel
import com.meistercharts.algorithms.model.DefaultSeries
import com.meistercharts.algorithms.model.Series
import com.meistercharts.algorithms.model.SeriesIndex
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableBoolean
import com.meistercharts.demo.configurableColorPickerNullable
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.configurableEnum
import com.meistercharts.demo.configurableFont
import com.meistercharts.demo.configurableList
import com.meistercharts.demo.style
import com.meistercharts.model.Insets
import com.meistercharts.model.Orientation
import com.meistercharts.model.Side
import it.neckar.open.kotlin.lang.or0ifNaN
import it.neckar.open.provider.MultiProvider
import it.neckar.open.i18n.TextKey
import com.meistercharts.style.Palette

class CategoryChartLayerDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Category Chart"

  //language=HTML
  override val description: String = "## Category Chart Layer"
  override val category: DemoCategory = DemoCategory.Layers

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {

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
            stackedBarPaintable.style.colorsProvider = MultiProvider.forListModulo(Palette.chartColors)
            stackedBarPaintable.data.valueRange = CategoryChartDemoHelper.calculateValueRangeForStackedBars(model)
          }

          val categoryPainters: List<CategoryPainter<CategorySeriesModel>> = listOf(groupedBarsPainter, stackedBarsPainter, CategoryDebugPainter())

          val defaultCategoryAxisPainter = DefaultCategoryAxisLabelPainter()
          val axisPainters = listOf(defaultCategoryAxisPainter, DebugCategoryAxisLabelPainter())

          val layer = CategoryLayer<CategorySeriesModel>(CategoryLayer.Data<CategorySeriesModel> { model }) {
            categoryPainter = groupedBarsPainter
          }
          layers.addLayer(layer)

          val categoryAxisLayer = layer.createAxisLayer {
            axisLabelPainter = defaultCategoryAxisPainter
          }
          layers.addLayer(categoryAxisLayer)

          configurableEnum("Orientation", layer.style::orientation, CategoryChartOrientation.values()) {
            onChange {
              categoryAxisLayer.style.side = when (it.categoryOrientation) {
                Orientation.Vertical -> Side.Bottom
                Orientation.Horizontal -> Side.Left
              }
              markAsDirty()
            }
          }
          configurableDouble("Min category size", layer.style.layoutCalculator.style::minCategorySize, {
            max = 1000.0
          })

          configurableDouble("Max category size", layer.style.layoutCalculator.style::maxCategorySize, 150.0) {
            max = 1000.0
          }

          configurableList("Category Painter", layer.style.categoryPainter, categoryPainters) {
            this.converter = { painter ->
              painter::class.simpleName ?: painter.toString()
            }
            onChange {
              layer.style.categoryPainter = it
              markAsDirty()
            }
          }

          configurableList("Axis Painter", categoryAxisLayer.style.axisLabelPainter, axisPainters) {
            this.converter = { painter ->
              painter::class.simpleName ?: painter.toString()
            }
            onChange {
              categoryAxisLayer.style.axisLabelPainter = it
              markAsDirty()
            }
          }

          declare {
            section("Bars Painter")
          }

          configurableDouble("Bar gap", groupedBarsPainter.configuration::barGap) {
            max = 100.0
            onChange {
              groupedBarsPainter.configuration.barGap = it
              stackedBarsPainter.stackedBarPaintable.style.segmentsGap = it
              markAsDirty()
            }
          }

          configurableBoolean("Show value labels", groupedBarsPainter.configuration::showValueLabel) {
            onChange {
              stackedBarsPainter.stackedBarPaintable.style.showValueLabels = it
              markAsDirty()
            }
          }

          configurableFont("Value labels font", groupedBarsPainter.configuration::valueLabelFont) {
            onChange {
              stackedBarsPainter.stackedBarPaintable.style.valueLabelFont = it
              markAsDirty()
            }
          }

          configurableColorPickerNullable("Value labels color", groupedBarsPainter.configuration::valueLabelColor) {
            onChange {
              stackedBarsPainter.stackedBarPaintable.style.valueLabelColor = it
              markAsDirty()
            }
          }

          configurableDouble("Value labels gap (Horizontal)", groupedBarsPainter.configuration::valueLabelAnchorGapHorizontal) {
            max = 100.0
            onChange {
              stackedBarsPainter.stackedBarPaintable.style.valueLabelGapHorizontal = it
              markAsDirty()
            }
          }
          configurableDouble("Value labels gap (Vertical)", groupedBarsPainter.configuration::valueLabelAnchorGapVertical) {
            max = 100.0
            onChange {
              stackedBarsPainter.stackedBarPaintable.style.valueLabelGapVertical = it
              markAsDirty()
            }
          }

          declare {
            section("Category Axis")
          }

          configurableBoolean("label in category", defaultCategoryAxisPainter.style::labelWithinCategory)
          configurableDouble("label in category gap", defaultCategoryAxisPainter.style::labelWithinCategoryGap) {
            max = 50.0
          }

          configurableFont("Category axis font", categoryAxisLayer.style::tickFont) {
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

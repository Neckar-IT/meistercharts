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
package com.meistercharts.charts.bullet

import com.meistercharts.model.ValueRange
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.barchart.CategoryPainter
import com.meistercharts.algorithms.layers.barchart.CategoryPainterPaintingVariables
import com.meistercharts.model.category.CategoryIndex
import com.meistercharts.model.category.valueAt
import com.meistercharts.color.Color
import com.meistercharts.annotations.Domain
import com.meistercharts.annotations.Window
import com.meistercharts.annotations.Zoomed
import com.meistercharts.canvas.StrokeLocation
import com.meistercharts.charts.OverflowIndicatorPainter
import com.meistercharts.design.Theme
import com.meistercharts.model.Orientation
import com.meistercharts.provider.ValueRangeProvider
import it.neckar.open.provider.MultiProvider
import it.neckar.open.unit.number.MayBeNaN
import it.neckar.open.unit.other.px

class BulletChartPainter(
  configurationLambda: Configuration.() -> Unit = {},
) : CategoryPainter<CategoryModelBulletChart> {

  val configuration: Configuration = Configuration().also(configurationLambda)

  override fun paintingVariables(): CategoryPainterPaintingVariables {
    return paintingVariables
  }

  private val paintingVariables = object : CategoryPainterPaintingVariables {
    override val actualSize: @Zoomed Double
      get() {
        return categorySize
      }

    /**
     * The size (width for vertical areas, height for horizontal areas) for the category (max of bar and current value indicator)
     */
    @Zoomed
    var categorySize: Double = Double.NaN

    @Suppress("UNUSED_PARAMETER")
    fun layout(paintingContext: LayerPaintingContext, categorySize: @Zoomed Double, categoryModel: CategoryModelBulletChart, categoryOrientation: Orientation) {
      this.categorySize = configuration.barSize.coerceAtLeast(configuration.currentValueIndicatorSize)
    }
  }


  override fun layout(paintingContext: LayerPaintingContext, categorySize: @Zoomed Double, categoryModel: CategoryModelBulletChart, categoryOrientation: Orientation) {
    paintingVariables.layout(paintingContext, categorySize, categoryModel, categoryOrientation)
  }

  /**
   * Remember the markers that are painted
   */
  private val outOfBoundsIndicatorsSelection = OverflowIndicatorPainter.MutableOverflowIndicatorsSelection()

  /**
   * ```
   *  ┃     ┃
   *  ┃   ┃ ┃
   *  ┃ ┃ ┃ ┃
   *```
   */
  override fun paintCategoryVertical(
    paintingContext: LayerPaintingContext,
    categoryWidth: @Zoomed Double,
    categoryIndex: CategoryIndex,
    isLast: Boolean,
    categoryModel: CategoryModelBulletChart,
  ) {
    val gc = paintingContext.gc
    val chartCalculator = paintingContext.chartCalculator

    @Domain val valueRange = configuration.valueRange()

    //Paint the range
    @Domain val barRange = categoryModel.barRange(categoryIndex)
    @MayBeNaN @Domain val rangeStartValue = barRange?.start ?: Double.NaN
    @MayBeNaN @Domain val rangeEndValue = barRange?.end ?: Double.NaN

    @MayBeNaN @Window val rangeStartYUnbound = chartCalculator.domain2windowY(rangeStartValue, valueRange)
    @MayBeNaN @Window val rangeEndYUnbound = chartCalculator.domain2windowY(rangeEndValue, valueRange)

    @MayBeNaN @Window val rangeStartY = chartCalculator.coerceInViewportY(rangeStartYUnbound)
    @MayBeNaN @Window val rangeEndY = chartCalculator.coerceInViewportY(rangeEndYUnbound)

    //Paint the bar
    gc.fill(configuration.barColors.valueAt(categoryIndex))
    gc.fillRect(-configuration.barSize / 2.0, rangeStartY, configuration.barSize, rangeEndY - rangeStartY)

    //Paint the Current Value Indicator
    @Domain val currentValue = categoryModel.currentValue(categoryIndex)
    @Window val currentY = chartCalculator.domain2windowY(currentValue, valueRange)

    when {
      chartCalculator.isInViewportY(currentY) -> {
        gc.fill(configuration.currentValueColor)
        val currentValueIndicatorWidth = configuration.currentValueIndicatorWidth
        gc.fillRect(-configuration.currentValueIndicatorSize / 2.0, currentY - currentValueIndicatorWidth / 2.0, configuration.currentValueIndicatorSize, currentValueIndicatorWidth)

        gc.stroke(configuration.currentValueOutlineColor)
        gc.lineWidth = configuration.currentValueIndicatorOutlineWidth
        gc.strokeRect(-configuration.currentValueIndicatorSize / 2.0, currentY - currentValueIndicatorWidth / 2.0, configuration.currentValueIndicatorSize, currentValueIndicatorWidth, StrokeLocation.Outside)
      }
    }

    //Calculate the out-of-bounds markers that should be painted
    outOfBoundsIndicatorsSelection.reset()

    outOfBoundsIndicatorsSelection.updateVertical(rangeStartYUnbound, chartCalculator)
    outOfBoundsIndicatorsSelection.updateVertical(rangeEndYUnbound, chartCalculator)
    outOfBoundsIndicatorsSelection.updateVertical(currentY, chartCalculator)

    configuration.overflowIndicatorPainter.paintIndicators(outOfBoundsIndicatorsSelection, paintingContext)
  }

  /**
   * ```
   * ━━━
   * ━━
   * ━━━━━━━
   * ━━━━
   *```
   */
  override fun paintCategoryHorizontal(
    paintingContext: LayerPaintingContext,
    categoryHeight: @Zoomed Double,
    categoryIndex: CategoryIndex,
    isLast: Boolean,
    categoryModel: CategoryModelBulletChart,
  ) {
    val gc = paintingContext.gc
    val chartCalculator = paintingContext.chartCalculator


    @Domain val valueRange = configuration.valueRange()

    //Paint the range
    @Domain val barRange = categoryModel.barRange(categoryIndex)
    @MayBeNaN @Domain val rangeStartValue = barRange?.start ?: Double.NaN
    @MayBeNaN @Domain val rangeEndValue = barRange?.end ?: Double.NaN

    @MayBeNaN @Window val rangeStartXUnbound = chartCalculator.domain2windowX(rangeStartValue, valueRange)
    @MayBeNaN @Window val rangeEndXUnbound = chartCalculator.domain2windowX(rangeEndValue, valueRange)

    @MayBeNaN @Window val rangeStartX = chartCalculator.coerceInViewportX(rangeStartXUnbound)
    @MayBeNaN @Window val rangeEndX = chartCalculator.coerceInViewportX(rangeEndXUnbound)

    //Paint the bar
    gc.fill(configuration.barColors.valueAt(categoryIndex))
    gc.fillRect(rangeStartX, -configuration.barSize / 2.0, rangeEndX - rangeStartX, configuration.barSize)

    //Paint the Current Value
    @Domain val currentValue = categoryModel.currentValue(categoryIndex)
    @Window val currentX = chartCalculator.domain2windowX(currentValue, valueRange)

    when {
      chartCalculator.isInViewportX(currentX) -> {
        gc.fill(configuration.currentValueColor)
        val currentValueIndicatorWidth = configuration.currentValueIndicatorWidth
        gc.fillRect(currentX - currentValueIndicatorWidth / 2.0, -configuration.currentValueIndicatorSize / 2.0, currentValueIndicatorWidth, configuration.currentValueIndicatorSize)

        gc.stroke(configuration.currentValueOutlineColor)
        gc.lineWidth = configuration.currentValueIndicatorOutlineWidth
        gc.strokeRect(currentX - currentValueIndicatorWidth / 2.0, -configuration.currentValueIndicatorSize / 2.0, currentValueIndicatorWidth, configuration.currentValueIndicatorSize, StrokeLocation.Outside)
      }
    }


    //Calculate the out-of-bounds markers that should be painted
    outOfBoundsIndicatorsSelection.reset()

    outOfBoundsIndicatorsSelection.updateHorizontal(rangeStartXUnbound, chartCalculator)
    outOfBoundsIndicatorsSelection.updateHorizontal(rangeEndXUnbound, chartCalculator)
    outOfBoundsIndicatorsSelection.updateHorizontal(currentX, chartCalculator)

    configuration.overflowIndicatorPainter.paintIndicators(outOfBoundsIndicatorsSelection, paintingContext)
  }

  class Configuration {
    /**
     * The value range to be used for all categories painted by the painter
     */
    var valueRange: ValueRangeProvider = { ValueRange.default }

    /**
     * The width ("thickness") of the current value indicator
     */
    var currentValueIndicatorWidth: @px Double = 2.0

    /**
     * The width of the current line indicator outline
     */
    var currentValueIndicatorOutlineWidth: @px Double = 1.0

    var currentValueColor: Color = Color.black
    var currentValueOutlineColor: Color = Color.white

    /**
     * Provides the color for a bar.
     */
    var barColors: CategoryColorProvider = Theme.chartColors()

    /**
     * The size of the bar.
     * For horizontal: the height
     * For vertical: the width
     */
    var barSize: Double = 25.0

    /**
     * The size of the current value indicator
     */
    var currentValueIndicatorSize: Double = 30.0

    /**
     * Used to paint the overflow indicators
     */
    var overflowIndicatorPainter: OverflowIndicatorPainter = OverflowIndicatorPainter()
  }
}

typealias CategoryColorProvider = MultiProvider<CategoryIndex, Color>

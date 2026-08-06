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
package com.meistercharts.algorithms.layers.barchart

import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.model.category.CategoryIndex
import com.meistercharts.model.category.CategorySeriesModel
import com.meistercharts.model.category.SeriesIndex
import com.meistercharts.annotations.Zoomed
import com.meistercharts.canvas.StyleDsl
import com.meistercharts.canvas.saved
import it.neckar.geometry.HorizontalAlignment
import it.neckar.geometry.Orientation
import it.neckar.open.provider.DoublesProvider
import it.neckar.open.unit.other.px

/**
 * Paints bars that belong to a category stacked
 */
class StackedBarsPainter(
  styleConfiguration: Style.() -> Unit = {},
) : CategoryPainter<CategorySeriesModel> {

  val style: Style = Style().also(styleConfiguration)

  /**
   * The paintable used to paint the stacked bars
   */
  val stackedBarPaintable: StackedBarPaintable = StackedBarPaintable(1.0, 1.0) {}

  override fun paintingVariables(): CategoryPainterPaintingVariables {
    return paintingVariables
  }

  private val paintingVariables = object : CategoryPainterPaintingVariables {
    /**
     * The actual size of the bar
     */
    override var actualSize: @Zoomed Double = 0.0
  }

  /**
   * The category model the [valuesProvider] reads from - updated before each category is painted
   */
  private var currentCategoryModel: CategorySeriesModel? = null

  /**
   * The category index the [valuesProvider] reads from - updated before each category is painted
   */
  private var currentCategoryIndex: CategoryIndex = CategoryIndex.zero

  /**
   * Reusable values provider for the current category - avoids allocating a provider per category per frame
   */
  private val valuesProvider: DoublesProvider = object : DoublesProvider {
    override fun size(): Int {
      return currentCategoryModel?.numberOfSeries ?: 0
    }

    override fun valueAt(index: Int): Double {
      val categoryModel = requireNotNull(currentCategoryModel) { "currentCategoryModel must be set" }
      return categoryModel.valueAt(currentCategoryIndex, SeriesIndex(index))
    }
  }

  override fun layout(paintingContext: LayerPaintingContext, categorySize: Double, categoryModel: CategorySeriesModel, categoryOrientation: Orientation) {
    paintingVariables.actualSize = categorySize.coerceAtMost(style.maxBarSize)
  }


  /**
   * ```
   *  ┃     ┃
   *  ┃   ┃ ┃
   *  ┃ ┃ ┃ ┃
   *```
   */
  override fun paintCategoryVertical(paintingContext: LayerPaintingContext, categoryWidth: Double, categoryIndex: CategoryIndex, isLast: Boolean, categoryModel: CategorySeriesModel) {
    if (categoryModel.numberOfSeries == 0) {
      return
    }

    currentCategoryModel = categoryModel
    currentCategoryIndex = categoryIndex

    val chartCalculator = paintingContext.chartCalculator
    @Zoomed val height = chartCalculator.contentAreaRelative2zoomedY(1.0)

    paintingContext.gc.saved {
      stackedBarPaintable.configuration.orientation = Orientation.Vertical
      stackedBarPaintable.configuration.valuesProvider = valuesProvider
      stackedBarPaintable.width = paintingVariables.actualSize
      stackedBarPaintable.height = height

      //How much space for the value label?
      val horizontalAlignment = stackedBarPaintable.configuration.valueLabelAnchorDirection.horizontalAlignment
      if (horizontalAlignment != HorizontalAlignment.Center) {
        val isFirst = categoryIndex.isFirst

        stackedBarPaintable.configuration.maxValueLabelWidth =
          when {
            (isFirst && horizontalAlignment == HorizontalAlignment.Right) ||
              (isLast && horizontalAlignment == HorizontalAlignment.Left) -> {
              //First column. We do *not* want to paint into the axis. Therefore, just very little space is available
              categoryWidth / 2.0 - stackedBarPaintable.width / 2.0 - stackedBarPaintable.configuration.valueLabelGapHorizontal
            }

            else -> {
              //Use all space up to the neighbor bar on the left, keeping a double value-label gap from it
              categoryWidth - stackedBarPaintable.width - stackedBarPaintable.configuration.valueLabelGapHorizontal * 3
            }
          }
      } else {
        stackedBarPaintable.configuration.maxValueLabelWidth = null
      }

      stackedBarPaintable.paint(paintingContext, 0.0, chartCalculator.domainRelative2windowY(0.0))
    }
  }

  /**
   * ```
   * ━━━
   * ━━
   * ━━━━━━━
   * ━━━━
   *```
   */
  override fun paintCategoryHorizontal(paintingContext: LayerPaintingContext, categoryHeight: Double, categoryIndex: CategoryIndex, isLast: Boolean, categoryModel: CategorySeriesModel) {
    if (categoryModel.numberOfSeries == 0) {
      return
    }

    currentCategoryModel = categoryModel
    currentCategoryIndex = categoryIndex

    val chartCalculator = paintingContext.chartCalculator
    @Zoomed val width = chartCalculator.contentAreaRelative2zoomedX(1.0)

    paintingContext.gc.saved {
      stackedBarPaintable.configuration.orientation = Orientation.Horizontal
      stackedBarPaintable.configuration.valuesProvider = valuesProvider
      stackedBarPaintable.width = width
      stackedBarPaintable.height = paintingVariables.actualSize

      //How much space for the value label?
      stackedBarPaintable.configuration.maxValueLabelWidth = width

      stackedBarPaintable.paint(paintingContext, chartCalculator.domainRelative2windowX(0.0), 0.0)
    }
  }

  @StyleDsl
  class Style {
    /**
     * The max width (vertical) or height (horizontal) of a stacked bar.
     * Only relevant if the category width is greater than this value
     */
    var maxBarSize: @px Double = 40.0
  }
}

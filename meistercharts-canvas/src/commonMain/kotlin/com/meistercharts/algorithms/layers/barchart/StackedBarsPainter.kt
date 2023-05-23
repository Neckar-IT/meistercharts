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
package com.meistercharts.algorithms.layers.barchart

import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.model.CategoryIndex
import com.meistercharts.algorithms.model.CategorySeriesModel
import com.meistercharts.algorithms.model.valuesAt
import com.meistercharts.annotations.Zoomed
import com.meistercharts.canvas.ConfigurationDsl
import com.meistercharts.canvas.saved
import com.meistercharts.model.HorizontalAlignment
import com.meistercharts.model.Orientation
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
  val stackedBarPaintable: StackedBarPaintable = StackedBarPaintable(StackedBarPaintable.Data(), 1.0, 1.0) {}

  override fun paintingVariables(): CategoryPainterPaintingVariables {
    return paintingVariables
  }

  private val paintingVariables = object : CategoryPainterPaintingVariables {
    /**
     * The actual size of the bar
     */
    override var actualSize: @Zoomed Double = 0.0
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
    val valuesProvider = categoryModel.valuesAt(categoryIndex)

    if (valuesProvider.isEmpty()) {
      return
    }

    val chartCalculator = paintingContext.chartCalculator
    @Zoomed val height = chartCalculator.contentAreaRelative2zoomedY(1.0)

    paintingContext.gc.saved {
      stackedBarPaintable.style.orientation = Orientation.Vertical
      stackedBarPaintable.data.valuesProvider = valuesProvider
      stackedBarPaintable.width = paintingVariables.actualSize
      stackedBarPaintable.height = height

      //How much space for the value label?
      val horizontalAlignment = stackedBarPaintable.style.valueLabelAnchorDirection.horizontalAlignment
      if (horizontalAlignment != HorizontalAlignment.Center) {
        val isFirst = categoryIndex.isFirst

        stackedBarPaintable.style.maxValueLabelWidth =
          when {
            (isFirst && horizontalAlignment == HorizontalAlignment.Right) ||
              (isLast && horizontalAlignment == HorizontalAlignment.Left) -> {
              //First column. We do *not* want to paint into the axis. Therefore, just very little space is available
              categoryWidth / 2.0 - stackedBarPaintable.width / 2.0 - stackedBarPaintable.style.valueLabelGapHorizontal
            }

            else -> {
              //we use all the space until the next (on the left side) bar. We use twice the value label gab to the neighbor bar (which we are not related to)
              categoryWidth - stackedBarPaintable.width - stackedBarPaintable.style.valueLabelGapHorizontal * 3
            }
          }
      } else {
        stackedBarPaintable.style.maxValueLabelWidth = null
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
    val valuesProvider = categoryModel.valuesAt(categoryIndex)

    if (valuesProvider.isEmpty()) {
      return
    }

    val chartCalculator = paintingContext.chartCalculator
    @Zoomed val width = chartCalculator.contentAreaRelative2zoomedX(1.0)

    paintingContext.gc.saved {
      stackedBarPaintable.style.orientation = Orientation.Horizontal
      stackedBarPaintable.data.valuesProvider = valuesProvider
      stackedBarPaintable.width = width
      stackedBarPaintable.height = paintingVariables.actualSize

      //How much space for the value label?
      stackedBarPaintable.style.maxValueLabelWidth = width

      stackedBarPaintable.paint(paintingContext, chartCalculator.domainRelative2windowX(0.0), 0.0)
    }
  }

  @ConfigurationDsl
  class Style {
    /**
     * The max width (vertical) or height (horizontal) of a stacked bar.
     * Only relevant if the category width is greater than this value
     */
    var maxBarSize: @px Double = 40.0
  }
}

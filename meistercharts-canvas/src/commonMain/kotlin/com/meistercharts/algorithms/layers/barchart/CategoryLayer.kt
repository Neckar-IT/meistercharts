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

import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.CategoryLayouter
import com.meistercharts.algorithms.layers.DefaultCategoryLayouter
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.PaintingVariables
import com.meistercharts.algorithms.layout.BoxIndex
import com.meistercharts.algorithms.layout.EquisizedBoxLayout
import com.meistercharts.model.category.CategoryIndex
import com.meistercharts.model.category.CategoryModel
import com.meistercharts.color.Color
import com.meistercharts.annotations.Window
import com.meistercharts.annotations.Zoomed
import com.meistercharts.canvas.ConfigurationDsl
import com.meistercharts.canvas.fillRectCoordinates
import com.meistercharts.canvas.saved
import com.meistercharts.design.Theme
import it.neckar.geometry.Orientation

/**
 * A layer that paints categories horizontally or vertically - e.g. bar charts
 */
class CategoryLayer<T : CategoryModel>(
  val configuration: Configuration<T>,
  additionalConfiguration: Configuration<T>.() -> Unit = {},
) : AbstractLayer() {

  constructor(
    modelProvider: () -> T,
    additionalConfiguration: Configuration<T>.() -> Unit = {},
    ): this(Configuration(modelProvider), additionalConfiguration)

  init {
    configuration.additionalConfiguration()
  }

  override val type: LayerType
    get() = LayerType.Content

  override fun paintingVariables(): CategoryPaintingVariables {
    return paintingVariables
  }

  private val paintingVariables = object : CategoryPaintingVariables {
    /**
     * The current layout that has been calculated
     */
    override var layout: EquisizedBoxLayout = EquisizedBoxLayout.empty

    override fun calculate(paintingContext: LayerPaintingContext) {
      layout = configuration.layoutCalculator.calculateLayout(paintingContext, configuration.modelProvider().numberOfCategories, configuration.orientation)
    }
  }

  override fun layout(paintingContext: LayerPaintingContext) {
    super.layout(paintingContext)

    val model = configuration.modelProvider()
    configuration.categoryPainter.layout(paintingContext, paintingVariables.layout.boxSize, model, configuration.orientation.categoryOrientation)
  }

  override fun paint(paintingContext: LayerPaintingContext) {
    when (configuration.orientation.categoryOrientation) {
      Orientation.Horizontal -> paintHorizontal(paintingContext)
      Orientation.Vertical -> paintVertical(paintingContext)
    }
  }

  /**
   * ```
   *  ┃     ┃
   *  ┃   ┃ ┃
   *  ┃ ┃ ┃ ┃
   *```
   */
  private fun paintVertical(paintingContext: LayerPaintingContext) {
    val gc = paintingContext.gc

    val layout = paintingVariables.layout
    val chartCalculator = paintingContext.chartCalculator

    val model = configuration.modelProvider()

    @Window val contentStartX = chartCalculator.contentAreaRelative2windowX(0.0)
    @Window val contentEndX = chartCalculator.contentAreaRelative2windowX(1.0)


    configuration.activeCategoryIndex?.let { activeCategoryIndex ->
      @Window val centerX = chartCalculator.zoomed2windowX(layout.calculateCenter(BoxIndex(activeCategoryIndex.value)))
      if (centerX < contentStartX || centerX > contentEndX) {
        //at least half of the category is hidden, so we do not paint it at all
        return@let
      }

      gc.saved {
        gc.translate(centerX, 0.0)
        //to the center of the category
        @Zoomed val categoryWidth = layout.boxSize
        @Zoomed val backgroundWidth = configuration.activeCategoryBackgroundSize(categoryWidth)

        //paint background
        @Zoomed val start = -backgroundWidth / 2.0
        @Zoomed val end = backgroundWidth / 2.0
        gc.fill(configuration.activeCategoryBackground)
        gc.fillRectCoordinates(start, chartCalculator.contentViewportMinY(), end, chartCalculator.contentViewportMaxY())
      }
    }

    //Paint the categories
    for (categoryIndexAsInt in 0 until model.numberOfCategories) {
      @Window val centerX = chartCalculator.zoomed2windowX(layout.calculateCenter(BoxIndex(categoryIndexAsInt)))
      if (centerX < contentStartX || centerX > contentEndX) {
        //at least half of the category is hidden, so we do not paint it at all
        continue
      }

      val categoryIndex = CategoryIndex(categoryIndexAsInt)
      gc.saved {
        gc.translate(centerX, 0.0)
        //to the center of the category
        val isLast = model.numberOfCategories - 1 == categoryIndexAsInt
        configuration.categoryPainter.paintCategoryVertical(paintingContext, layout.boxSize, categoryIndex, isLast, model)
      }
    }

  }

  /**
   * Returns true if the category is highlighted (e.g. because of a mouse over)
   */
  private fun CategoryIndex.isHighlighted(): Boolean {
    return configuration.activeCategoryIndex == this
  }

  /**
   * ```
   * ━━━
   * ━━
   * ━━━━━━━
   * ━━━━
   *```
   */
  private fun paintHorizontal(paintingContext: LayerPaintingContext) {
    val gc = paintingContext.gc
    val layout = paintingVariables.layout
    val chartCalculator = paintingContext.chartCalculator

    val model = configuration.modelProvider()

    @Window val contentStartY = chartCalculator.contentAreaRelative2windowY(0.0)
    @Window val contentEndY = chartCalculator.contentAreaRelative2windowY(1.0)


    //Paint the active category first
    configuration.activeCategoryIndex?.let { activeCategoryIndex ->
      @Window val centerY = chartCalculator.zoomed2windowY(layout.calculateCenter(BoxIndex(activeCategoryIndex.value)))
      if (centerY < contentStartY || centerY > contentEndY) {
        //at least half of the category is hidden, so we do not paint it at all
        return@let
      }

      gc.saved {
        gc.translate(0.0, centerY)
        //to the center of the category

        @Zoomed val categoryHeight = layout.boxSize
        @Zoomed val backgroundHeight = configuration.activeCategoryBackgroundSize(categoryHeight)

        //paint background
        @Zoomed val start = -backgroundHeight / 2.0
        @Zoomed val end = backgroundHeight / 2.0
        gc.fill(configuration.activeCategoryBackground)
        gc.fillRectCoordinates(chartCalculator.contentViewportMinX(), start, chartCalculator.contentViewportMaxX(), end)
      }
    }

    //Paint the categories
    for (categoryIndexAsInt in 0 until model.numberOfCategories) {
      @Window val centerY = chartCalculator.zoomed2windowY(layout.calculateCenter(BoxIndex(categoryIndexAsInt)))
      if (centerY < contentStartY || centerY > contentEndY) {
        //at least half of the category is hidden, so we do not paint it at all
        continue
      }

      val categoryIndex = CategoryIndex(categoryIndexAsInt)
      gc.saved {
        gc.translate(0.0, centerY)
        //to the center of the category
        val isLast = model.numberOfCategories - 1 == categoryIndexAsInt
        configuration.categoryPainter.paintCategoryHorizontal(paintingContext, layout.boxSize, categoryIndex, isLast, model)
      }
    }
  }

  /**
   * Holds information about the appearance of this chart
   */
  @ConfigurationDsl
  open class Configuration<T : CategoryModel>(
    /**
     * Provides the model to be used by this layer
     */
    var modelProvider: () -> T,
  ) {
    constructor(categorySeriesModel: T) : this(modelProvider = {
      categorySeriesModel
    })

    /**
     * Provides the layout
     */
    var layoutCalculator: CategoryLayouter = DefaultCategoryLayouter()

    /**
     * The painter that is used to paint the categories
     */
    var categoryPainter: CategoryPainter<T> = CategoryDebugPainter()

    /**
     * How to orient the categories
     */
    var orientation: CategoryChartOrientation = CategoryChartOrientation.VerticalLeft

    /**
     * The index of the category that is highlighted (mouse over)
     */
    var activeCategoryIndex: CategoryIndex? = null

    /**
     * The background color for the highlighted category (mouse over)
     */
    var activeCategoryBackground: Color = Theme.backgroundColorActive()

    /**
     * Provides the size for the background of the active category.
     *
     * The result is used to paint the background for the active category.
     * If the returned size is small(er) than the category size, the background is also smaller
     */
    var activeCategoryBackgroundSize: (categorySize: @Zoomed Double) -> Double = { categorySize -> categorySize }
  }

}

interface CategoryPaintingVariables : PaintingVariables {
  val layout: EquisizedBoxLayout
}

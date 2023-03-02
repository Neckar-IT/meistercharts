package com.meistercharts.algorithms.layers.barchart

import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.model.CategoryIndex
import com.meistercharts.algorithms.model.CategoryModel
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.annotations.Zoomed
import com.meistercharts.canvas.StrokeLocation
import com.meistercharts.canvas.paintMark
import com.meistercharts.model.Orientation
import com.meistercharts.model.Rectangle

/**
 * Paints a category consisting of one or more series.
 */
interface CategoryPainter<in T : CategoryModel> {
  /**
   * Returns the painting properties that have been calculated by the layout calls
   */
  fun paintingVariables(): CategoryPainterPaintingVariables

  /**
   * Calculates the layout.
   * The layout method is called *once*. Then the paint methods are called for every category.
   * Depending on the orientation either [paintCategoryVertical] or [paintCategoryHorizontal] are called. Never both.
   */
  fun layout(
    paintingContext: LayerPaintingContext,
    categorySize: @Zoomed Double,
    categoryModel: T,
    categoryOrientation: Orientation,
  )

  /**
   * Paints a category of a chart with vertical chart orientation.
   *
   * This method is called with the context translated to the center of the category area
   * @param categoryWidth the width of the category
   * @param categoryIndex the index of the category to be painted
   * @param categoryModel the category model
   *
   * ```
   *  ┃     ┃
   *  ┃   ┃ ┃
   *  ┃ ┃ ┃ ┃
   *```
   */
  fun paintCategoryVertical(
    paintingContext: LayerPaintingContext,
    categoryWidth: @Zoomed Double,
    categoryIndex: CategoryIndex,
    isLast: Boolean,
    categoryModel: T,
  )

  /**
   * Paints a category of a chart with horizontal chart orientation
   *
   * This method is called with the context translated to the center of the category area
   *
   * @param categoryHeight the height of the category
   * @param categoryIndex the index of the category to be painted
   * @param categoryModel the category model
   * ```
   * ━━━
   * ━━
   * ━━━━━━━
   * ━━━━
   *```
   */
  fun paintCategoryHorizontal(
    paintingContext: LayerPaintingContext,
    categoryHeight: @Zoomed Double,
    categoryIndex: CategoryIndex,
    isLast: Boolean,
    categoryModel: T,
  )
}

/**
 * Contains the painting properties for the category painter
 */
interface CategoryPainterPaintingVariables {
  /**
   * Returns the actual size this painter uses.
   * * horizontal orientation: Width
   * * vertical orientation: Height
   *
   * This value can be used to highlight the painted area.
   */
  val actualSize: @Zoomed Double

}

/**
 * Paints areas for the categories
 */
class CategoryDebugPainter : CategoryPainter<CategoryModel> {
  override fun paintingVariables(): CategoryPainterPaintingVariables {
    return paintingVariables
  }

  private val paintingVariables = object : CategoryPainterPaintingVariables {
    /**
     * The actual size of the bar
     */
    override var actualSize: @Zoomed Double = 0.0
  }

  override fun layout(paintingContext: LayerPaintingContext, categorySize: Double, categoryModel: CategoryModel, categoryOrientation: Orientation) {
    paintingVariables.actualSize = categorySize
  }


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
    categoryModel: CategoryModel,
  ) {
    val gc = paintingContext.gc
    gc.fill(Color.silver)

    val chartCalculator = paintingContext.chartCalculator

    val startY = chartCalculator.domainRelative2windowY(0.0)
    val endY = chartCalculator.domainRelative2windowY(1.0)

    val categoryArea = Rectangle(-categoryWidth / 2.0, startY, categoryWidth, endY - startY)
    gc.fillRect(categoryArea)

    gc.stroke(Color.orange)
    gc.strokeRect(categoryArea, StrokeLocation.Inside)

    gc.paintMark(0.0, startY, color = Color.blue)
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
    categoryModel: CategoryModel,
  ) {
    val gc = paintingContext.gc
    gc.fill(Color.silver)

    val chartCalculator = paintingContext.chartCalculator

    val startX = chartCalculator.domainRelative2windowX(0.0)
    val endX = chartCalculator.domainRelative2windowX(1.0)

    val categoryArea = Rectangle(startX, -categoryHeight / 2.0, endX - startX, categoryHeight)
    gc.fillRect(categoryArea)

    gc.stroke(Color.orange)
    gc.strokeRect(categoryArea, StrokeLocation.Inside)

    gc.paintMark(startX, 0.0, color = Color.blue)
  }
}

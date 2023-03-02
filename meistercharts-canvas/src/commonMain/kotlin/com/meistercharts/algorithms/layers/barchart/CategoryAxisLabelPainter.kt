package com.meistercharts.algorithms.layers.barchart

import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layout.EquisizedBoxLayout
import com.meistercharts.algorithms.model.CategoryIndex
import com.meistercharts.annotations.Window
import com.meistercharts.annotations.Zoomed
import com.meistercharts.model.Direction
import com.meistercharts.model.Orientation
import com.meistercharts.provider.SizedLabelsProvider

/**
 * Paints categories on a category axis
 */
interface CategoryAxisLabelPainter {

  /**
   * Is called once *before* [paint] is called for each label
   */
  fun layout(
    categoryLayout: EquisizedBoxLayout,
    labelsProvider: SizedLabelsProvider,
    labelVisibleCondition: LabelVisibleCondition,
  )

  /**
   * Paints a category of a category axis
   */
  fun paint(
    paintingContext: LayerPaintingContext,
    /**
     * the x coordinate where the tick hits the bounding box
     */
    x: @Window Double,
    /**
     * the y coordinate where the tick hits the bounding box
     */
    y: @Window Double,
    /**
     * the width of the bounding box
     */
    width: @Zoomed Double,
    /**
     * the height of the bounding box
     */
    height: @Zoomed Double,
    /**
     * The direction in which to find the tick of the category axis
     */
    tickDirection: Direction,
    /**
     * The label of the category
     */
    label: String?,
    /**
     * the index of the category to be painted
     */
    categoryIndex: CategoryIndex,

    /**
     * The orientation of the category *axis*.
     *
     * Horizontal: ------
     * Vertical: |
     */
    categoryAxisOrientation: Orientation,
  )
}


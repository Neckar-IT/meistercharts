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
import com.meistercharts.algorithms.layout.EquisizedBoxLayout
import com.meistercharts.model.category.CategoryIndex
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


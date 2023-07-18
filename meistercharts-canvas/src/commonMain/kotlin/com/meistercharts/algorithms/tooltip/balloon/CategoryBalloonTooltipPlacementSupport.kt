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
package com.meistercharts.algorithms.tooltip.balloon

import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layout.BoxIndex
import com.meistercharts.algorithms.layout.EquisizedBoxLayout
import com.meistercharts.model.category.CategoryIndex
import com.meistercharts.annotations.ContentArea
import com.meistercharts.annotations.Window
import com.meistercharts.model.Orientation
import com.meistercharts.model.Side
import it.neckar.open.provider.CoordinatesProvider1
import it.neckar.open.provider.DoubleProvider
import it.neckar.open.unit.other.px

/**
 * Supports the placement for category balloon tooltips - based on a category model
 */
class CategoryBalloonTooltipPlacementSupport(
  /**
   * The orientation of the categories.
   * ATTENTION: This is *not* the orientation of the bars! It describes how the *categories* are placed.
   *
   * * [Orientation.Horizontal]: The categories are placed besides each other
   * * [Orientation.Vertical]: The categories are placed one above the other
   */
  orientation: () -> Orientation,

  /**
   * Provides the active category index - where the tooltip shall be shown for
   */
  activeCategoryIndexProvider: () -> CategoryIndex?,

  /**
   * Provides the current segments layout that is used to calculate the location of the current category
   */
  boxLayout: () -> EquisizedBoxLayout,

  /**
   * Provides the size of a category (width/height) depending on the orientation
   */
  categorySize: @px DoubleProvider,

  additionalConfiguration: Configuration.() -> Unit = {},
) {

  /**
   * The configuration
   */
  val configuration: Configuration = Configuration(
    orientation = orientation, activeCategoryIndexProvider = activeCategoryIndexProvider, boxLayout = boxLayout, categorySize = categorySize
  ).also(additionalConfiguration)

  /**
   * Returns the active category index.
   * Throws an exception if there is no active category
   */
  fun activeCategoryIndex(): CategoryIndex {
    return configuration.activeCategoryIndex()
  }

  private var paintingVariables = object {
    /**
     * The side where the tooltips are placed - relative to the category
     */
    var tooltipSide: Side = Side.Left
  }

  /**
   * The side where the tooltips are painted.
   * The value is calculated dynamically
   */
  val tooltipSide: Side
    get() {
      return paintingVariables.tooltipSide
    }

  /**
   * Provides the tooltip locations
   */
  val coordinates: CoordinatesProvider1<LayerPaintingContext> = object : CoordinatesProvider1<LayerPaintingContext> {
    override fun size(param1: LayerPaintingContext): Int {
      if (configuration.activeCategoryIndexOrNull() == null) {
        return 0
      }
      return 1
    }

    override fun xAt(index: Int, param1: LayerPaintingContext): Double {
      return when (configuration.orientation()) {
        Orientation.Vertical -> {
          //Categories placed above each other
          param1.chartCalculator.contentAreaRelative2windowX(0.5)
        }

        Orientation.Horizontal -> {
          //Categories placed besides each other
          calculateXForHorizontalCategories(param1)
        }
      }
    }

    private fun calculateXForHorizontalCategories(param1: LayerPaintingContext): Double {
      val layout: EquisizedBoxLayout = configuration.boxLayout()

      @ContentArea val centerOfCategory = layout.calculateCenter(configuration.activeSegmentIndex())
      @Window val center = param1.chartCalculator.contentArea2windowX(centerOfCategory)

      @px val categorySize = configuration.categorySize()

      val leftSide = center - categorySize / 2.0
      val rightSide = center + categorySize / 2.0

      val remainingSpaceRight = param1.width - rightSide

      return if (leftSide > remainingSpaceRight) {
        paintingVariables.tooltipSide = Side.Left
        leftSide
      } else {
        paintingVariables.tooltipSide = Side.Right
        rightSide
      }
    }

    override fun yAt(index: Int, param1: LayerPaintingContext): Double {
      return when (configuration.orientation()) {
        Orientation.Vertical -> {
          //Categories placed above each other
          calculateYForVerticalCategories(param1)
        }

        Orientation.Horizontal -> {
          //Categories placed besides each other
          param1.chartCalculator.contentAreaRelative2windowY(0.5)
        }
      }
    }

    private fun calculateYForVerticalCategories(param1: LayerPaintingContext): Double {
      val layout: EquisizedBoxLayout = configuration.boxLayout()

      @ContentArea val centerOfCategory = layout.calculateCenter(configuration.activeSegmentIndex())
      @Window val center = param1.chartCalculator.contentArea2windowY(centerOfCategory)

      @px val categorySize = configuration.categorySize()

      val topSide = center - categorySize / 2.0
      val bottomSide = center + categorySize / 2.0

      val remainingSpaceBottom = param1.height - bottomSide

      return if (topSide > remainingSpaceBottom) {
        paintingVariables.tooltipSide = Side.Top
        topSide
      } else {
        paintingVariables.tooltipSide = Side.Bottom
        bottomSide
      }
    }
  }

  open class Configuration(
    /**
     * The orientation of the categories.
     * ATTENTION: This is *not* the orientation of the bars! It describes how the *categories* are placed.
     *
     * * [Orientation.Horizontal]: The categories are placed besides each other
     * * [Orientation.Vertical]: The categories are placed one above the other
     */
    val orientation: () -> Orientation,

    /**
     * Provides the active category index - where the tooltip shall be shown for
     */
    private val activeCategoryIndexProvider: () -> CategoryIndex?,

    /**
     * Provides the current segments layout that is used to calculate the location of the current category
     */
    val boxLayout: () -> EquisizedBoxLayout,

    /**
     * Provides the size of a category (width/height) depending on the orientation
     */
    val categorySize: @px DoubleProvider,
  ) {

    fun activeCategoryIndexOrNull(): CategoryIndex? {
      return activeCategoryIndexProvider()
    }

    /**
     * Returns the active category index - throws an exception if there is no category index
     */
    fun activeCategoryIndex(): CategoryIndex {
      return activeCategoryIndexOrNull() ?: throw IllegalStateException("No activeCategoryIndexOrNull found")
    }

    /**
     * Returns the active segment index
     */
    fun activeSegmentIndex(): BoxIndex {
      return BoxIndex(activeCategoryIndex().value)
    }
  }
}

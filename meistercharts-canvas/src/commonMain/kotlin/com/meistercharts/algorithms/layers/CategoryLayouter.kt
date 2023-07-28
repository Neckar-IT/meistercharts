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
package com.meistercharts.algorithms.layers

import com.meistercharts.algorithms.layers.barchart.CategoryChartOrientation
import com.meistercharts.algorithms.layout.Exact
import com.meistercharts.algorithms.layout.LayoutMode
import com.meistercharts.algorithms.layout.EquisizedBoxLayout
import com.meistercharts.algorithms.layout.BoxLayoutCalculator
import com.meistercharts.canvas.ConfigurationDsl
import it.neckar.geometry.Orientation
import it.neckar.open.provider.DoubleProvider
import it.neckar.open.unit.other.px

/**
 * Calculates a layout related to categories
 */
interface CategoryLayouter {

  /**
   * Calculates the layout for categories
   */
  fun calculateLayout(
    paintingContext: LayerPaintingContext,
    /**
     * The number of segments
     */
    numberOfSegments: Int,
    /**
     * The orientation of the chart
     */
    orientation: CategoryChartOrientation,
  ): EquisizedBoxLayout

}

/**
 * Default implementation that uses the SegmentsLayoutCalculator
 */
class DefaultCategoryLayouter(
  styleConfiguration: Style.() -> Unit = {}
) : CategoryLayouter {

  val style: Style = Style().also(styleConfiguration)

  override fun calculateLayout(paintingContext: LayerPaintingContext, numberOfSegments: Int, orientation: CategoryChartOrientation): EquisizedBoxLayout {
    //The space that is available for all categories
    val availableSpace = when (orientation.categoryOrientation) {
      Orientation.Vertical -> paintingContext.chartCalculator.contentAreaRelative2zoomedX(1.0)
      Orientation.Horizontal -> paintingContext.chartCalculator.contentAreaRelative2zoomedY(1.0)
    }

    return BoxLayoutCalculator.layout(
      availableSpace = availableSpace,
      numberOfBoxes = numberOfSegments,
      layoutDirection = orientation.layoutDirection,
      minBoxSize = style.minCategorySizeProvider(),
      maxBoxSize = style.maxCategorySizeProvider(),
      gapSize = style.gapSize(),
      layoutMode = style.layoutMode
    )
  }

  @ConfigurationDsl
  class Style {
    /**
     * The layout mode
     */
    var layoutMode: LayoutMode = Exact

    /**
     * Provides the minimum width/height of a category
     * This is *not* the bar width/height - but instead the width/height for the complete category (e.g. including labels and other stuff)
     * @see minCategorySize
     */
    var minCategorySizeProvider: () -> @px Double = { 40.0 }

    /**
     * Provides the maximum width/height of a category
     * This is *not* the bar width/height - but instead the width/height for the complete category (e.g. including labels and other stuff)
     */
    var maxCategorySizeProvider: () -> @px Double? = { 150.0 }


    /**
     * A property that can be used to set the minimum category size
     * @see minCategorySizeProvider
     */
    var minCategorySize: @px Double
      get() {
        return minCategorySizeProvider()
      }
      set(value) {
        minCategorySizeProvider = { value }
      }

    /**
     *  A property that can be used to set the maximum category size
     *  @see maxCategorySizeProvider
     */
    var maxCategorySize: @px Double?
      get() {
        return maxCategorySizeProvider()
      }
      set(value) {
        maxCategorySizeProvider = { value }
      }

    /**
     * The gap size between the categories
     */
    @px
    var gapSize: DoubleProvider = DoubleProvider { 0.0 }
  }
}

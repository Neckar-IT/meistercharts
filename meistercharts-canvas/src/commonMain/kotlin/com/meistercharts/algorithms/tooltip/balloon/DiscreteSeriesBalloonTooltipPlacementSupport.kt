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

import com.meistercharts.time.TimeRange
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layout.BoxIndex
import com.meistercharts.algorithms.layout.EquisizedBoxLayout
import com.meistercharts.annotations.ContentArea
import com.meistercharts.annotations.Window
import com.meistercharts.history.ReferenceEntryData
import com.meistercharts.history.ReferenceEntryDataSeriesIndex
import it.neckar.geometry.Side
import it.neckar.open.provider.CoordinatesProvider1
import it.neckar.open.provider.DoubleProvider
import it.neckar.open.provider.DoubleProvider1
import it.neckar.open.unit.number.MayBeNaN
import it.neckar.open.unit.other.px

/**
 * Supports the placement for balloon tooltips - for discrete timeline charts
 */
class DiscreteSeriesBalloonTooltipPlacementSupport(
  /**
   * Provides the active category index - where the tooltip shall be shown for
   */
  activeDataSeriesIndexProvider: () -> ReferenceEntryDataSeriesIndex?,

  /**
   * Provides the tooltip x location
   */
  tooltipLocationXProvider: @Window DoubleProvider1<LayerPaintingContext>,

  /**
   * Provides the data (if there is any)
   */
  referenceEntryDataProvider: () -> ReferenceEntryData?,

  /**
   * The content area time range - required to calculate the exact location
   */
  contentAreaTimeRange: () -> TimeRange,

  /**
   * Provides the current segments layout that is used to calculate the location of the current data series
   */
  boxLayout: () -> EquisizedBoxLayout,

  /**
   * Converts the [ReferenceEntryDataSeriesIndex] to an box index.
   * Has to use the visible indices to figure out, a which location this [ReferenceEntryDataSeriesIndex] is currently visible.
   */
  referenceEntryDataSeriesIndex2BoxIndex: (refIndex: ReferenceEntryDataSeriesIndex) -> BoxIndex,

  /**
   * Provides the height of a data series (height)
   */
  dataSeriesHeight: @px DoubleProvider,

  additionalConfiguration: Configuration.() -> Unit = {},
) {

  /**
   * The configuration
   */
  val configuration: Configuration = Configuration(
    activeDataSeriesIndexProvider = activeDataSeriesIndexProvider,
    tooltipLocationXProvider = tooltipLocationXProvider,
    referenceEntryDataProvider = referenceEntryDataProvider,
    boxLayout = boxLayout,
    dataSeriesHeight = dataSeriesHeight,
    contentAreaTimeRange = contentAreaTimeRange,
    referenceEntryDataSeriesIndex2BoxIndex = referenceEntryDataSeriesIndex2BoxIndex,
  ).also(additionalConfiguration)

  /**
   * Returns the active data series index.
   * Throws an exception if there is no active category
   */
  fun activeDataSeriesIndex(): ReferenceEntryDataSeriesIndex {
    return configuration.activeDataSeriesIndex()
  }

  private var paintingVariables = object {
    /**
     * The side where the tooltips are placed - relative to the data series
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
  val coordinates: @Window CoordinatesProvider1<LayerPaintingContext> = object : CoordinatesProvider1<LayerPaintingContext> {
    override fun size(param1: LayerPaintingContext): Int {
      if (configuration.activeDataSeriesIndexOrNull() == null) {
        return 0
      }
      return 1
    }

    override fun xAt(index: Int, param1: LayerPaintingContext): @Window @MayBeNaN Double {
      return configuration.tooltipLocationXProvider(param1)
    }

    override fun yAt(index: Int, param1: LayerPaintingContext): Double {
      return calculateYForVerticalCategories(param1)
    }

    private fun calculateYForVerticalCategories(param1: LayerPaintingContext): Double {
      val layout: EquisizedBoxLayout = configuration.boxLayout()

      @ContentArea val centerOfCategory = layout.calculateCenter(configuration.activeSegmentIndex())
      @Window val center = param1.chartCalculator.contentArea2windowY(centerOfCategory)

      @px val categorySize = configuration.dataSeriesHeight()

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
     * Provides the active category index - where the tooltip shall be shown for
     */
    val activeDataSeriesIndexProvider: () -> ReferenceEntryDataSeriesIndex?,

    /**
     * Provides the tooltip x location
     */
    val tooltipLocationXProvider: @Window DoubleProvider1<LayerPaintingContext>,

    /**
     * Provides the reference entry data
     */
    val referenceEntryDataProvider: () -> ReferenceEntryData?,

    /**
     * Provides the current segments layout that is used to calculate the location of the current data series
     */
    val boxLayout: () -> EquisizedBoxLayout,

    /**
     * Converts the [ReferenceEntryDataSeriesIndex] to an box index.
     * Has to use the visible indices to figure out, a which location this [ReferenceEntryDataSeriesIndex] is currently visible.
     */
    val referenceEntryDataSeriesIndex2BoxIndex: (refIndex: ReferenceEntryDataSeriesIndex) -> BoxIndex,

    /**
     * Provides the height of a data series (height)
     */
    val dataSeriesHeight: @px DoubleProvider,

    val contentAreaTimeRange: () -> TimeRange,
  ) {

    fun activeDataSeriesIndexOrNull(): ReferenceEntryDataSeriesIndex? {
      return activeDataSeriesIndexProvider()
    }

    /**
     * Returns the data series index - throws an exception if there is no category index
     */
    fun activeDataSeriesIndex(): ReferenceEntryDataSeriesIndex {
      return activeDataSeriesIndexOrNull() ?: throw IllegalStateException("No activeDataSeriesIndex found")
    }

    /**
     * Returns the active segment index
     */
    fun activeSegmentIndex(): BoxIndex {
      val activeDataSeriesIndex = activeDataSeriesIndex()
      return referenceEntryDataSeriesIndex2BoxIndex(activeDataSeriesIndex)
    }
  }
}

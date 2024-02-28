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

import com.meistercharts.algorithms.layers.legend.HeadlineAbovePaintable
import com.meistercharts.algorithms.layers.legend.LegendEntryIndex
import com.meistercharts.algorithms.layers.legend.SymbolAndLabelLegendPaintable
import com.meistercharts.algorithms.layers.legend.withHeadline
import com.meistercharts.annotations.Domain
import com.meistercharts.canvas.paintable.Paintable
import com.meistercharts.canvas.paintable.RectanglePaintable
import com.meistercharts.charts.bullet.CategoryModelBulletChart
import com.meistercharts.color.Color
import com.meistercharts.color.ColorProvider
import com.meistercharts.model.category.CategoryIndex
import com.meistercharts.range.LinearValueRange
import it.neckar.geometry.Size
import it.neckar.open.formatting.CachedNumberFormat
import it.neckar.open.provider.MultiProvider
import it.neckar.open.provider.MultiProvider1
import it.neckar.open.provider.SizedProviderWithIndexMapping
import it.neckar.open.provider.impl.IndexMappingSupport
import it.neckar.open.provider.withMappedIndex


/**
 * Supports balloon tooltips for the bullet chart
 */
class BulletChartBalloonTooltipSupport(
  /**
   * Used for the placement of the tooltips
   */
  val tooltipPlacement: CategoryBalloonTooltipPlacementSupport,

  /**
   * Provides the category model
   */
  model: () -> CategoryModelBulletChart,

  /**
   * The value format that is used to format the values
   */
  valueFormat: () -> CachedNumberFormat,

  /**
   * Returns the color for the current value symbol
   */
  val currentValueSymbolColor: ColorProvider,

  /**
   * Returns the color for the bar of the provided category index
   */
  val barSymbolColor: (CategoryIndex) -> Color,
) {

  /**
   * Returns the active category index
   */
  private fun activeCategoryIndex() = tooltipPlacement.activeCategoryIndex()

  /**
   * Provides the labels for a CategoryModelBulletChart
   * Contains two entries:
   * * current value
   * * range
   */
  val labels: @LegendEntryIndex SizedProviderWithIndexMapping<String> = object : SizedProviderWithIndexMapping<String> {
    /**
     * The index mapping -
     */
    private val indexMapping: IndexMappingSupport = IndexMappingSupport()

    private var currentValue: @Domain Double = Double.NaN
    private var barRange: LinearValueRange? = null

    private val hasCurrentValue: Boolean
      get() {
        return currentValue.isFinite()
      }

    private val hasFiniteBarRange: Boolean
      get() {
        return barRange != null
      }

    override fun mapped2Original(mappedIndex: Int): Int {
      return indexMapping.mapped2Original(mappedIndex)
    }

    override fun size(): Int {
      //Recalculate the painting variables
      val categoryIndex: CategoryIndex = activeCategoryIndex()
      val currentModel = model()
      currentValue = currentModel.currentValue(categoryIndex)
      barRange = currentModel.barRange(categoryIndex)

      return indexMapping.updateMapping(2) {
        var indexCounter = 0

        if (hasCurrentValue) {
          this.storeMapping(0, 0)
          indexCounter++
        }

        if (hasFiniteBarRange) {
          this.storeMapping(1, indexCounter)
          indexCounter++
        }

        indexCounter
      }
    }

    override fun valueAt(index: Int): String {
      return when (indexMapping.mapped2Original(index)) {
        0 -> valueFormat().format(currentValue)
        1 -> requireNotNull(barRange).let { barRange ->
          "${valueFormat().format(barRange.start)} - ${valueFormat().format(barRange.end)}"
        }

        else -> throw IllegalArgumentException("Invalid index $index")
      }
    }
  }

  /**
   * The paintable that is used to create the tooltip content.
   *
   * This paintable is used by the layer returned by [createTooltipLayer]
   */
  val tooltipContentPaintable: HeadlineAbovePaintable<SymbolAndLabelLegendPaintable> = SymbolAndLabelLegendPaintable(
    labels = labels,
    symbols = createSymbolsForBallonTooltipValueAndRange(),
  ) {
  }.withHeadline { textService, i18nConfiguration ->
    model().categoryNameAt(activeCategoryIndex(), textService, i18nConfiguration)
  }


  /**
   * Creates the symbols provider for a bullet chart model
   */
  fun createSymbolsForBallonTooltipValueAndRange(
    currentValueSymbolSize: Size = Size(16.0, 2.0),
    barSymbolSize: Size = Size.PX_16,
  ): MultiProvider<LegendEntryIndex, Paintable> {

    return MultiProvider.forListModulo<LegendEntryIndex, Paintable>(
      listOf(
        RectanglePaintable(currentValueSymbolSize) { currentValueSymbolColor() },
        RectanglePaintable(barSymbolSize) { barSymbolColor(activeCategoryIndex()) }
      )
    ).withMappedIndex(labels) //map the indices based on the labels
  }

  fun applyBalloonTooltipSizes(currentValueSymbolSize: Size, barSymbolSize: Size) {
    tooltipContentPaintable.delegate.configuration.symbols = createSymbolsForBallonTooltipValueAndRange(currentValueSymbolSize, barSymbolSize)
  }

  /**
   * Creates the tooltip layer
   */
  fun createTooltipLayer(): BalloonTooltipLayer {
    return BalloonTooltipLayer(
      BalloonTooltipLayer.Configuration(
        coordinates = tooltipPlacement.coordinates,
        tooltipContent = MultiProvider1.always(tooltipContentPaintable)
      )
    ) {
    }.also {
      it.tooltipPainter.configuration.noseSide = {
        tooltipPlacement.tooltipSide.flipped()
      }
    }
  }
}

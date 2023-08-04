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

import com.meistercharts.provider.formatted
import com.meistercharts.algorithms.layers.legend.HeadlineAbovePaintable
import com.meistercharts.algorithms.layers.legend.LegendEntryIndex
import com.meistercharts.algorithms.layers.legend.SymbolAndLabelLegendPaintable
import com.meistercharts.algorithms.layers.legend.withHeadline
import com.meistercharts.model.category.CategorySeriesModel
import com.meistercharts.model.category.SeriesIndex
import com.meistercharts.color.Color
import com.meistercharts.annotations.Domain
import it.neckar.geometry.Size
import it.neckar.open.formatting.CachedNumberFormat
import it.neckar.open.kotlin.lang.DoublesComparator
import it.neckar.open.provider.DoublesProvider
import it.neckar.open.provider.FilteredDoublesProvider
import it.neckar.open.provider.MappedIndexMultiProvider
import it.neckar.open.provider.MultiProvider
import it.neckar.open.provider.MultiProvider1
import it.neckar.open.provider.SizedProvider
import it.neckar.open.provider.SortedDoublesProvider
import it.neckar.open.provider.filteredOnlyFinite
import it.neckar.open.provider.sorted
import it.neckar.open.unit.number.IsFinite
import it.neckar.open.unit.number.MayBeNaN

/**
 * Ballon tooltip support for category series.
 *
 * This is a custom implementation that is useful for exactly its category series.
 */
class CategorySeriesModelBalloonTooltipSupport(
  val tooltipPlacement: CategoryBalloonTooltipPlacementSupport,

  /**
   * Provides the category model.
   * The provided model is queried for the values.
   */
  categoryModel: () -> CategorySeriesModel,
  /**
   * The comparator that is used to sort the values
   */
  comparator: DoublesComparator = DoublesComparator.naturalReversed,
  /**
   * The value format that is used to format the values
   */
  valueFormat: () -> CachedNumberFormat,

  /**
   * Provides the colors for a series index
   */
  val colors: MultiProvider<SeriesIndex, Color>,
) {

  /**
   * The values - is used to generate the labels
   */
  val values: @Domain @MayBeNaN DoublesProvider = object : DoublesProvider {
    override fun size(): Int {
      return categoryModel().numberOfSeries
    }

    override fun valueAt(index: Int): Double {
      val categoryIndex = tooltipPlacement.configuration.activeCategoryIndex()
      val seriesIndex = SeriesIndex(index)

      return categoryModel().valueAt(categoryIndex, seriesIndex)
    }
  }

  /**
   * Contains only finite values
   */
  val valuesFiltered: @Domain @IsFinite @OnlyFiniteIndex FilteredDoublesProvider = values.filteredOnlyFinite()

  /**
   * The colors - matching [valuesFiltered]
   */
  val colorsFiltered: MultiProvider<OnlyFiniteIndex, Color> = valuesFiltered.wrapMultiProvider(colors)

  /**
   * Filtered and sorted values
   */
  val valuesFilteredAndSorted: @LegendEntryIndex SortedDoublesProvider = valuesFiltered.sorted(comparator)

  /**
   * Filtered and sorted colors - matching [valuesFilteredAndSorted]
   */
  val colorsFilteredAndSorted: MappedIndexMultiProvider<LegendEntryIndex, Color> = valuesFilteredAndSorted.wrapMultiProvider(colorsFiltered)

  /**
   * The sorted value labels
   */
  val valueLabelsFilteredAndSorted: @LegendEntryIndex SizedProvider<String> = valuesFilteredAndSorted.formatted(valueFormat)


  /**
   * Paints the symbols and the labels
   */
  val symbolAndLegendPaintable: SymbolAndLabelLegendPaintable = SymbolAndLabelLegendPaintable.rectangles(
    labels = valueLabelsFilteredAndSorted,
    symbolColors = colorsFilteredAndSorted
  ) {
  }

  /**
   * The tooltip content paintable
   */
  val tooltipContentPaintable: HeadlineAbovePaintable<SymbolAndLabelLegendPaintable> = symbolAndLegendPaintable
    .withHeadline { textService, i18nConfiguration ->
      categoryModel().categoryNameAt(tooltipPlacement.configuration.activeCategoryIndex(), textService, i18nConfiguration)
    }

  /**
   * Applies the symbol size for the legend
   */
  fun applyLegendSymbolSize(symbolSize: Size) {
    tooltipContentPaintable.delegate.configuration.symbols = SymbolAndLabelLegendPaintable.defaultSymbols(symbolSize, colorsFilteredAndSorted)
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

  /**
   * Annotation that identifies providers that are filtered and only contain the finite elements
   */
  @Target(AnnotationTarget.TYPE)
  annotation class OnlyFiniteIndex
}

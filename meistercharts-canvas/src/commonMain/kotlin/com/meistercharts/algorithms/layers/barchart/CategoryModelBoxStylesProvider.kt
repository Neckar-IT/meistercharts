package com.meistercharts.algorithms.layers.barchart

import com.meistercharts.algorithms.model.CategoryIndex
import com.meistercharts.algorithms.model.SeriesIndex
import it.neckar.open.provider.MultiProvider
import com.meistercharts.style.BoxStyle

/**
 * Provides the [BoxStyle] for every series of every category
 */
fun interface CategoryModelBoxStylesProvider {
  /**
   * Provides the [BoxStyle] for the category at index [categoryIndex] and the series at [seriesIndex]
   */
  fun boxStyle(categoryIndex: CategoryIndex, seriesIndex: SeriesIndex): BoxStyle

  companion object {
    /**
     * Ensures that a series has the same [BoxStyle] in every category.
     *
     * This is useful if there is more than one series visible per category.
     */
    fun onlySeriesBoxStylesProvider(seriesBoxStyles: List<BoxStyle>): CategoryModelBoxStylesProvider {
      return onlySeriesBoxStylesProvider(MultiProvider.forListModulo(seriesBoxStyles))
    }

    /**
     * Ensures that a series has the same [BoxStyle] in every category.
     *
     * This is useful if there is more than one series visible per category.
     */
    fun onlySeriesBoxStylesProvider(seriesBoxStyles: MultiProvider<SeriesIndex, BoxStyle>): CategoryModelBoxStylesProvider {
      return CategoryModelBoxStylesProvider { _, seriesIndex -> seriesBoxStyles.valueAt(seriesIndex.value) }
    }

    /**
     * Ensures that all series of the same category have the same [BoxStyle].
     *
     * This is especially useful if there is only one data series, and you want every category to have a different [BoxStyle].
     */
    fun onlyCategoryBoxStylesProvider(categoriesBoxStyles: List<BoxStyle>): CategoryModelBoxStylesProvider {
      return onlyCategoryBoxStylesProvider(MultiProvider.forListModulo(categoriesBoxStyles))
    }

    /**
     * Ensures that all series of the same category have the same [BoxStyle].
     *
     * This is especially useful if there is only one data series, and you want every category to have a different [BoxStyle].
     */
    fun onlyCategoryBoxStylesProvider(categoriesBoxStyles: MultiProvider<CategoryIndex, BoxStyle>): CategoryModelBoxStylesProvider {
      return CategoryModelBoxStylesProvider { categoryIndex, _ -> categoriesBoxStyles.valueAt(categoryIndex.value) }
    }
  }
}

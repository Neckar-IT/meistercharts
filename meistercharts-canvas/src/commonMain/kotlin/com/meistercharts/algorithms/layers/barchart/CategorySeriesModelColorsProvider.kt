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

import com.meistercharts.algorithms.model.CategoryIndex
import com.meistercharts.algorithms.model.SeriesIndex
import com.meistercharts.algorithms.painter.Color
import it.neckar.open.provider.MultiProvider

/**
 * Provides colors for every series of every category
 */
fun interface CategorySeriesModelColorsProvider {
  /**
   * Provides the color for the category at index [categoryIndex] and the series at [seriesIndex]
   */
  fun color(categoryIndex: CategoryIndex, seriesIndex: SeriesIndex): Color

  companion object {
    /**
     * Ensures that a series has the same color in every category.
     *
     * This is useful if there is more than one series visible per category.
     */
    fun onlySeriesColorsProvider(seriesColors: List<Color>): CategorySeriesModelColorsProvider {
      return onlySeriesColorsProvider(MultiProvider.forListModulo(seriesColors))
    }

    /**
     * Ensures that a series has the same color in every category.
     *
     * This is useful if there is more than one series visible per category.
     */
    fun onlySeriesColorsProvider(seriesColors: MultiProvider<SeriesIndex, Color>): CategorySeriesModelColorsProvider {
      return CategorySeriesModelColorsProvider { _, seriesIndex -> seriesColors.valueAt(seriesIndex.value) }
    }

    /**
     * Ensures that all series of the same category have the same color.
     *
     * This is especially useful if there is only one data series, and you want every category to be in a different color.
     */
    fun onlyCategoryColorsProvider(categoriesColors: List<Color>): CategorySeriesModelColorsProvider {
      return onlyCategoryColorsProvider(MultiProvider.forListModulo(categoriesColors))
    }

    /**
     * Ensures that all series of the same category have the same color.
     *
     * This is especially useful if there is only one data series, and you want every category to be in a different color.
     */
    fun onlyCategoryColorsProvider(categoriesColors: MultiProvider<CategoryIndex, Color>): CategorySeriesModelColorsProvider {
      return CategorySeriesModelColorsProvider { categoryIndex, _ -> categoriesColors.valueAt(categoryIndex.value) }
    }
  }
}

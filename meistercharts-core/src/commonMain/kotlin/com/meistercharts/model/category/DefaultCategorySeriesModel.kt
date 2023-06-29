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
package com.meistercharts.model.category

import com.meistercharts.annotations.Domain
import it.neckar.open.unit.number.MayBeNaN
import it.neckar.open.collections.fastForEachIndexed
import it.neckar.open.i18n.I18nConfiguration
import it.neckar.open.i18n.TextService
import it.neckar.open.i18n.resolve

/**
 * A simple immutable implementation of [CategorySeriesModel]
 */
data class DefaultCategorySeriesModel(
  private val categories: List<Category>,
  private val series: List<Series>,
) : CategorySeriesModel {

  override val numberOfCategories: Int = categories.size
  override val numberOfSeries: Int = series.size

  init {
    series.fastForEachIndexed { index, series ->
      require(series.size() == categories.size) { "series[$index] must hold ${categories.size} values but holds ${series.size()} values instead" }
    }
  }

  /**
   * Retrieves the [Category] at [index]
   *
   * @param index a value between 0 (inclusive) and [numberOfCategories] (exclusive)
   */
  fun categoryAt(index: CategoryIndex): Category = categories[index.value]

  override fun valueAt(categoryIndex: CategoryIndex, seriesIndex: SeriesIndex): @Domain @MayBeNaN Double {
    return seriesAt(seriesIndex).valueAt(categoryIndex.value)
  }

  /**
   * Returns the series as the given index
   */
  fun seriesAt(seriesIndex: SeriesIndex): Series = series[seriesIndex.value]

  override fun categoryNameAt(categoryIndex: CategoryIndex, textService: TextService, i18nConfiguration: I18nConfiguration): String {
    return categoryAt(categoryIndex).name.resolve(textService, i18nConfiguration)
  }

  companion object {
    /**
     * Creates an empty [CategorySeriesModel]
     */
    val empty: DefaultCategorySeriesModel = DefaultCategorySeriesModel(listOf(), listOf())
  }
}

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
package com.meistercharts.algorithms.model

import com.meistercharts.annotations.Domain
import it.neckar.open.unit.number.MayBeNaN
import com.meistercharts.provider.SizedLabelsProvider
import it.neckar.open.kotlin.lang.ifNaN
import it.neckar.open.provider.DoublesProvider
import it.neckar.open.i18n.I18nConfiguration
import it.neckar.open.i18n.TextService
import kotlin.reflect.KProperty0

/**
 * A model that organizes series in categories.
 *
 * Instances of this model are typically used by bar charts.
 *
 * Given the table
 *
 * ```
 * |  Category  |  Gorillas  |  Zebras  | Rhinos |
 * |:----------:|:----------:|:--------:|:------:|
 * |    2018    |     10     |    5     |    8   |
 * |    2019    |     17     |    4     |    9   |
 * |    2020    |     19     |    4     |    7   |
 * ```
 * The rows are categories (2018, 2019, 2020) and the columns are series (gorillas, zebras, rhinos).
 */
interface CategorySeriesModel : CategoryModel {

  /**
   * The number of [Series] instances
   */
  val numberOfSeries: Int

  /**
   * Retrieves the value for the series with index [seriesIndex] at the category with index [categoryIndex]
   *
   * May return [Double.NaN] if the value does not exist
   */
  fun valueAt(categoryIndex: CategoryIndex, seriesIndex: SeriesIndex): @Domain @MayBeNaN Double

  /**
   * Returns true if this model does not contain any values
   */
  fun isEmpty(): Boolean {
    return numberOfCategories == 0 || numberOfSeries == 0
  }
}

/**
 * Creates a [DoublesProvider] with the values of all series that belong to the category at index [categoryIndex]
 */
@Deprecated("No longer required - access the model directly (somehow)")
fun CategorySeriesModel.valuesAt(categoryIndex: CategoryIndex): @Domain DoublesProvider {
  return object : DoublesProvider {
    override fun size(): Int = numberOfSeries

    override fun valueAt(index: Int): @Domain @MayBeNaN Double {
      return valueAt(categoryIndex, SeriesIndex(index))
    }

    override fun toString(): String {
      return "CategoryModelValues{categoryIndex: $categoryIndex, size: ${size()}}"
    }
  }
}

/**
 * Creates a [SizedLabelsProvider] with the labels of all categories
 */
fun CategoryModel.createCategoryLabelsProvider(): SizedLabelsProvider {
  val self = this
  return object : SizedLabelsProvider {
    override fun size(param1: TextService, param2: I18nConfiguration): Int {
      return self.numberOfCategories

    }

    override fun valueAt(index: Int, textService: TextService, i18nConfiguration: I18nConfiguration): String {
      return categoryNameAt(CategoryIndex(index), textService, i18nConfiguration)
    }
  }
}

/**
 * Returns a labels provider that returns the labels for the current value of this property
 */
fun KProperty0<CategorySeriesModel>.createCategoryLabelsProvider(): SizedLabelsProvider {
  return object : SizedLabelsProvider {
    override fun size(param1: TextService, param2: I18nConfiguration): Int {
      return get().numberOfCategories
    }

    override fun valueAt(index: Int, textService: TextService, i18nConfiguration: I18nConfiguration): String {
      return get().categoryNameAt(CategoryIndex(index), textService, i18nConfiguration)
    }
  }
}

/**
 * Creates a [CategorySeriesModel] that delegates all calls to the current value of this property.
 */
fun KProperty0<CategorySeriesModel>.delegate(): CategorySeriesModel {
  return object : CategorySeriesModel {
    override val numberOfCategories: Int
      get() {
        return get().numberOfCategories
      }

    override val numberOfSeries: Int
      get() {
        return get().numberOfSeries
      }

    override fun valueAt(categoryIndex: CategoryIndex, seriesIndex: SeriesIndex): @Domain @MayBeNaN Double {
      return get().valueAt(categoryIndex, seriesIndex)
    }

    override fun categoryNameAt(categoryIndex: CategoryIndex, textService: TextService, i18nConfiguration: I18nConfiguration): String {
      return get().categoryNameAt(categoryIndex, textService, i18nConfiguration)
    }

    override fun isEmpty(): Boolean {
      return get().isEmpty()
    }
  }
}

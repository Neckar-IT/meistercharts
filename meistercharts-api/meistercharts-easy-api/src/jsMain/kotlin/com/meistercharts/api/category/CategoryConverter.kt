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
package com.meistercharts.api.category

import com.meistercharts.algorithms.layers.barchart.CategorySeriesModelColorsProvider
import com.meistercharts.algorithms.painter.UrlPaintable
import com.meistercharts.annotations.Domain
import com.meistercharts.api.bullet.BulletChartConfiguration
import com.meistercharts.api.toColor
import com.meistercharts.canvas.paintable.Paintable
import com.meistercharts.charts.bullet.CategoryColorProvider
import com.meistercharts.color.Color
import com.meistercharts.model.category.Category
import com.meistercharts.model.category.CategoryIndex
import com.meistercharts.model.category.CategorySeriesModel
import com.meistercharts.model.category.DefaultCategorySeriesModel
import com.meistercharts.model.category.MutableSeries
import com.meistercharts.range.LinearValueRange
import com.meistercharts.range.ValueRange
import it.neckar.open.http.Url
import it.neckar.open.i18n.TextKey
import it.neckar.open.kotlin.lang.getModulo
import it.neckar.open.provider.DoublesProvider
import it.neckar.open.provider.DoublesProvider.Companion.forDoubles
import it.neckar.open.provider.MultiProvider
import it.neckar.open.unit.number.MayBeNaN
import kotlin.math.max

/**
 */
object CategoryConverter {

  /**
   * Converts the data contained in [jsData] into a [CategorySeriesModel]
   *
   * @return the converted model or null if there is no change
   */
  fun toCategoryModel(jsData: CategoriesSeriesData): CategorySeriesModel? {
    val jsCategories: Array<CategorySeriesData?> = jsData.categories ?: return null // null indicates no change

    var maxJsCategoriesValuesCount = 0
    // this list will contain all valid categories; a category is valid if values are provided
    val categories = mutableListOf<Category>()
    for (jsCategory in jsCategories) {
      val size = jsCategory?.values?.size ?: break // the first null-value indicates that there will be no more categories -> bail out
      maxJsCategoriesValuesCount = max(maxJsCategoriesValuesCount, size)

      categories.add(Category(jsCategory.label?.let { TextKey.simple(it) } ?: TextKey.empty))
    }

    val series = List(maxJsCategoriesValuesCount) { index -> MutableSeries(TextKey.simple(index.toString()), categories.size) }
    for ((jsCategoryIndex, jsCategory) in jsCategories.withIndex()) {
      val jsCategoryValues = jsCategory?.values ?: break // the first null-value indicates that there will be no more categories -> bail out
      for ((jsCategoryValueIndex, value) in jsCategoryValues.withIndex()) {
        series[jsCategoryValueIndex].setValueAt(jsCategoryIndex, value)
      }
    }

    return DefaultCategorySeriesModel(categories, series)
  }

  /**
   * Creates a current values provider
   */
  fun toCurrentValuesProvider(jsConfiguration: BulletChartConfiguration): DoublesProvider? {
    val jsCategories: Array<CategoryBulletChartData> = jsConfiguration.categories ?: return null // null indicates no change

    val currentValues = jsCategories.map {
      it.current
    }.toDoubleArray()

    return forDoubles(*currentValues)
  }

  fun toAreaValueRangesProvider(jsConfiguration: BulletChartConfiguration): MultiProvider<CategoryIndex, LinearValueRange?>? {
    val jsCategories: Array<CategoryBulletChartData> = jsConfiguration.categories ?: return null // null indicates no change

    val currentValues = jsCategories.map {
      @MayBeNaN @Domain val start = it.barStart
      @MayBeNaN @Domain val end = it.barEnd

      if (start.isFinite() && end.isFinite() && start < end) {
        ValueRange.linear(start, end)
      } else {
        null
      }
    }

    return MultiProvider.forListModulo(currentValues)
  }

  //fun toCategoryModel(jsData: CategoriesBulletChartData): CategorySeriesModel? {
  //  val jsCategories = jsData.categories ?: return null // null indicates no change
  //  var maxJsCategoriesValuesCount = 0
  //  // this list will contain all valid categories; a category is valid if values are provided
  //  val categories = mutableListOf<Category>()
  //  for (jsCategory in jsCategories) {
  //    val size = jsCategory?.values?.size ?: break // the first null-value indicates that there will be no more categories -> bail out
  //    maxJsCategoriesValuesCount = max(maxJsCategoriesValuesCount, size)
  //
  //    categories.add(Category(jsCategory.label?.let { TextKey.simple(it) } ?: TextKey.empty))
  //  }
  //
  //  val series = List(maxJsCategoriesValuesCount) { index -> MutableSeries(TextKey.simple(index.toString()), categories.size) }
  //  for ((jsCategoryIndex, jsCategory) in jsCategories.withIndex()) {
  //    val jsCategoryValues = jsCategory?.values ?: break // the first null-value indicates that there will be no more categories -> bail out
  //    for ((jsCategoryValueIndex, value) in jsCategoryValues.withIndex()) {
  //      series[jsCategoryValueIndex].setValueAt(jsCategoryIndex, value)
  //    }
  //  }
  //
  //  return DefaultCategorySeriesModel(categories, series)
  //}

  /**
   * Extracts the category images contained in [jsData]
   */
  fun toCategoryImages(jsData: CategoriesSeriesData): List<Paintable?>? {
    return jsData.categories?.map { jsCategory ->
      toCategoryImage(jsCategory)
    }
  }

  fun toCategoryImages(jsData: BulletChartConfiguration): List<Paintable?>? {
    return jsData.categories?.map { jsCategory ->
      jsCategory.image
        ?.takeIf { it.isNotBlank() }
        ?.let { UrlPaintable.naturalSize(Url(it)) }
    }
  }

  private fun toCategoryImage(jsCategory: CategorySeriesData?): Paintable? {
    return jsCategory?.image
      ?.takeIf { it.isNotBlank() }
      ?.let { UrlPaintable.naturalSize(Url(it)) }
  }

  /**
   * Converts the two-dimensional array into a [CategorySeriesModelColorsProvider]
   *
   * @param jsColors The first dimension denotes the category, the second the series.
   */
  fun toCategoryModelColorsProvider(jsColors: Array<Array<String?>?>?): CategorySeriesModelColorsProvider? {
    if (jsColors == null) {
      return null
    }
    val colors = mutableListOf<MutableList<Color>>()
    for (categoryIndex in jsColors.indices) {
      val jsSeriesColors = jsColors[categoryIndex]
      if (jsSeriesColors != null) {
        val seriesColors = mutableListOf<Color>()
        for (seriesIndex in jsSeriesColors.indices) {
          val jsSeriesColor = jsSeriesColors[seriesIndex];
          if (jsSeriesColor != null) {
            seriesColors.add(jsSeriesColor.toColor())
          }
        }
        if (seriesColors.isNotEmpty()) {
          colors.add(seriesColors)
        }
      }
    }
    if (colors.isEmpty()) {
      return null
    }
    return CategorySeriesModelColorsProvider { categoryIndex, seriesIndex ->
      colors.getModulo(categoryIndex.value).getModulo(seriesIndex.value)
    }
  }

  fun toCategoryColorProvider(jsColors: Array<String>?): CategoryColorProvider? {
    if (jsColors.isNullOrEmpty()) {
      return null
    }

    val colors = jsColors.map {
      it.toColor()
    }

    return MultiProvider.Companion.forListModulo(colors)
  }
}

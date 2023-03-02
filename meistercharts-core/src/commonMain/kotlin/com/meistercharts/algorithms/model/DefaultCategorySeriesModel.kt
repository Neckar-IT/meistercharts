package com.meistercharts.algorithms.model

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

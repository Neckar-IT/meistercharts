package com.meistercharts.algorithms.model

import it.neckar.open.provider.DoublesProvider
import com.meistercharts.provider.LabelsProvider
import com.meistercharts.provider.EmptyStrings
import it.neckar.open.i18n.I18nConfiguration
import it.neckar.open.i18n.TextKey
import it.neckar.open.i18n.TextService
import it.neckar.open.i18n.resolve
import it.neckar.open.unit.other.Index
import kotlin.jvm.JvmOverloads

/**
 * A [CategorySeriesModel] that consists of a single series
 */
class SingleSeriesCategorySeriesModel @JvmOverloads constructor(
  /**
   * Provides the values for the series
   */
  val valuesProvider: DoublesProvider,
  /**
   * Provides the labels for the categories
   */
  val labelsProvider: LabelsProvider<Index> = EmptyStrings,
  /**
   * The name of the series
   */
  val seriesName: TextKey = TextKey.empty,
) : CategorySeriesModel {

  private val series: Series = object : Series {
    override fun size(): Int {
      return valuesProvider.size()
    }

    override fun valueAt(index: Int): Double {
      return valuesProvider.valueAt(index)
    }

    override fun seriesName(textService: TextService, i18nConfiguration: I18nConfiguration): String {
      return seriesName.resolve(textService, i18nConfiguration)
    }
  }

  override val numberOfCategories: Int
    get() {
      return valuesProvider.size()
    }

  override val numberOfSeries: Int = 1

  override fun categoryNameAt(categoryIndex: CategoryIndex, textService: TextService, i18nConfiguration: I18nConfiguration): String {
    return labelsProvider.valueAt(categoryIndex.value, textService, i18nConfiguration)
  }

  override fun valueAt(categoryIndex: CategoryIndex, seriesIndex: SeriesIndex): Double {
    return valuesProvider.valueAt(categoryIndex.value)
  }

  fun seriesAt(seriesIndex: Int): Series {
    return series
  }
}


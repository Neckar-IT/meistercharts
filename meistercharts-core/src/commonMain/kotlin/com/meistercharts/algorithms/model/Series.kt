package com.meistercharts.algorithms.model

import com.meistercharts.annotations.Domain
import it.neckar.open.unit.number.MayBeNaN
import it.neckar.open.provider.HasSize
import it.neckar.open.i18n.I18nConfiguration
import it.neckar.open.i18n.TextKey
import it.neckar.open.i18n.TextService
import it.neckar.open.i18n.resolve

/**
 * A series of related values
 */
interface Series : HasSize {
  /**
   * Retrieves the value at the given [index].
   *
   * The value can be a [@Domain], a [@DomainRelative] *or* a [@pct] value
   *
   * @param index a value between 0 (inclusive) and [size] (exclusive)
   * @return the value at the given [index]
   */
  fun valueAt(index: Int): @Domain @MayBeNaN Double

  /**
   * Returns the name of the series
   */
  fun seriesName(textService: TextService, i18nConfiguration: I18nConfiguration): String?
}

/**
 * A default implementation of [Series]
 */
class DefaultSeries(
  val name: TextKey,
  private val values: List<@Domain @MayBeNaN Double>,
) : Series {
  /**
   * Creates a series using a string
   */
  constructor(
    name: String,
    values: List<@Domain Double>,
  ) : this(TextKey.simple(name), values)

  override fun size(): Int = values.size

  override fun valueAt(index: Int): @Domain @MayBeNaN Double {
    return values[index]
  }

  override fun seriesName(textService: TextService, i18nConfiguration: I18nConfiguration): String {
    return name.resolve(textService, i18nConfiguration)
  }
}

/**
 * An implementation of [Series] that is mutable
 */
class MutableSeries(
  val name: TextKey,
  numberOfCategories: Int,
) : Series {
  private val values: MutableList<@MayBeNaN Double> = MutableList(numberOfCategories) { Double.NaN }

  override fun size(): Int = values.size

  override fun valueAt(index: Int): @Domain @MayBeNaN Double {
    return values[index]
  }

  fun setValueAt(index: Int, newValue: @Domain Double) {
    values[index] = newValue
  }

  override fun seriesName(textService: TextService, i18nConfiguration: I18nConfiguration): String {
    return name.resolve(textService, i18nConfiguration)
  }

}

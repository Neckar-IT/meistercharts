package com.meistercharts.history

import it.neckar.open.i18n.TextKey
import it.neckar.open.unit.other.ID
import kotlinx.serialization.Serializable

/**
 * Abstract base class for history configuration stuff
 */
@Serializable
abstract class AbstractHistoryConfiguration {

  /**
   * The ids of the data series.
   */
  abstract val dataSeriesIds: @ID IntArray

  /**
   * The display names for each data series
   * Has the same size as [dataSeriesIds]
   */
  abstract val displayNames: List<TextKey>

  /**
   * Returns the number of data series
   */
  val dataSeriesCount: Int
    get() {
      return dataSeriesIds.size
    }

  /**
   * Returns the data series id as int.
   * This method should not be used directly. Instead, use the typed methods in the subclass.
   */
  protected fun getDataSeriesIdAsInt(dataSeriesIndexAsInt: Int): Int {
    if (dataSeriesIndexAsInt >= dataSeriesIds.size) {
      throw IndexOutOfBoundsException("Invalid index <$dataSeriesIndexAsInt> - size: <${dataSeriesIds.size}>")
    }

    return dataSeriesIds[dataSeriesIndexAsInt]
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || this::class != other::class) return false

    other as com.meistercharts.history.AbstractHistoryConfiguration

    if (!dataSeriesIds.contentEquals(other.dataSeriesIds)) return false
    if (displayNames != other.displayNames) return false

    return true
  }

  override fun hashCode(): Int {
    var result = dataSeriesIds.contentHashCode()
    result = 31 * result + displayNames.hashCode()
    return result
  }
}

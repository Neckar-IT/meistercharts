package com.meistercharts.history

import it.neckar.open.annotations.Slow
import it.neckar.open.collections.IntArrayList
import it.neckar.open.collections.emptyIntArray
import it.neckar.open.i18n.TextKey
import it.neckar.open.unit.other.ID
import kotlinx.serialization.Serializable

/**
 * History configuration for object values
 */
@Serializable
class HistoryReferenceEntryConfiguration(
  /**
   * The ids of the data series.
   */
  override val dataSeriesIds: @ID IntArray,

  /**
   * The display names for each data series
   * Has the same size as [dataSeriesIds]
   */
  override val displayNames: List<TextKey>,

  ) : AbstractHistoryConfiguration() {
  init {
    require(dataSeriesIds.size == displayNames.size) {
      "size mismatch - display names: ${dataSeriesIds.size} vs ${displayNames.size}"
    }
  }

  @Slow
  fun getDataSeriesIndex(dataSeriesId: DataSeriesId): ReferenceEntryDataSeriesIndex = ReferenceEntryDataSeriesIndex(dataSeriesIds.indexOf(dataSeriesId.value))

  /**
   * Returns the data series id at the given index
   */
  fun getDataSeriesId(dataSeriesIndex: ReferenceEntryDataSeriesIndex): DataSeriesId {
    return DataSeriesId(getDataSeriesIdAsInt(dataSeriesIndex.value))
  }

  /**
   * Returns the human-readable name for the *index* (not ID)
   */
  fun getDisplayName(dataSeriesIndex: ReferenceEntryDataSeriesIndex): TextKey {
    return displayNames[dataSeriesIndex.value]
  }

  override fun toString(): String {
    return "HistoryEnumConfiguration(dataSeriesIds=${dataSeriesIds.contentToString()}, displayNames=$displayNames)"
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || this::class != other::class) return false
    if (!super.equals(other)) return false

    other as HistoryReferenceEntryConfiguration

    if (!dataSeriesIds.contentEquals(other.dataSeriesIds)) return false
    return displayNames == other.displayNames
  }

  override fun hashCode(): Int {
    var result = super.hashCode()
    result = 31 * result + dataSeriesIds.contentHashCode()
    result = 31 * result + displayNames.hashCode()
    return result
  }


  companion object {
    val empty: HistoryReferenceEntryConfiguration = HistoryReferenceEntryConfiguration(emptyIntArray(), emptyList())
  }

  class Builder {
    private val dataSeriesIds: @ID IntArrayList = IntArrayList()
    private val displayNames: MutableList<TextKey> = mutableListOf()
    private val dataBuilder: MutableList<ReferenceEntriesDataMap> = mutableListOf()

    fun referenceEntryDataSeries(id: DataSeriesId, displayName: String, dataMap: ReferenceEntriesDataMap) {
      referenceEntryDataSeries(id, TextKey.simple(displayName), dataMap)
    }

    fun referenceEntryDataSeries(id: DataSeriesId, displayName: TextKey, dataMap: ReferenceEntriesDataMap) {
      require(dataSeriesIds.contains(id.value).not()) { "id <$id> already added" }

      dataSeriesIds.add(id.value)
      displayNames.add(displayName)
      this.dataBuilder.add(dataMap)
    }

    fun build(): HistoryReferenceEntryConfiguration {
      return HistoryReferenceEntryConfiguration(dataSeriesIds.toIntArray(), displayNames.toList())
    }
  }
}

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
  override val dataSeriesIds: @ReferenceEntryIdInt IntArray,

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

    fun referenceEntryDataSeries(id: DataSeriesId, displayName: String) {
      referenceEntryDataSeries(id, TextKey.simple(displayName))
    }

    fun referenceEntryDataSeries(id: DataSeriesId, displayName: TextKey) {
      require(dataSeriesIds.contains(id.value).not()) { "id <$id> already added" }

      dataSeriesIds.add(id.value)
      displayNames.add(displayName)
    }

    fun build(): HistoryReferenceEntryConfiguration {
      return HistoryReferenceEntryConfiguration(dataSeriesIds.toIntArray(), displayNames.toList())
    }
  }
}

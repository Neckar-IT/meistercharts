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
import it.neckar.open.collections.fastForEachIndexed
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

  /**
   * The enum configurations for each data series.
   * Has the same size as [dataSeriesIds]
   *
   * This represents the status for each data series - may be null if the data series does not have a status enum.
   */
  val statusEnums: List<HistoryEnum?>,

  ) : AbstractHistoryConfiguration() {
  init {
    require(dataSeriesIds.size == displayNames.size) {
      "size mismatch - display names: ${dataSeriesIds.size} vs ${displayNames.size}"
    }

    require(dataSeriesIds.size == statusEnums.size) {
      "size mismatch - statusEnums configs: ${dataSeriesIds.size} vs ${statusEnums.size}"
    }
  }

  /**
   * Returns the history enum for the given data series index.
   * Returns null if there is no status enum for the data series
   */
  fun getStatusEnum(dataSeriesIndex: ReferenceEntryDataSeriesIndex): HistoryEnum? {
    return statusEnums[dataSeriesIndex.value]
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

  fun dump(): String {
    return buildString {
      dataSeriesIds.fastForEachIndexed { index, dataSeriesIndexAsInt ->
        append(dataSeriesIndexAsInt.toString().padStart(7))
        append(": ")
        append(displayNames[index].fallbackText.padEnd(25))
        append(" |")
        statusEnums[index]?.let { statusEnum ->
          append(" (")
          append((statusEnum.enumDescription + ")").padEnd(15))
        }
        appendLine()
      }
    }
  }

  override fun toString(): String {
    return "HistoryReferenceEntryConfiguration(dataSeriesIds=${dataSeriesIds.contentToString()}, displayNames=$displayNames, statusEnums=$statusEnums)"
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || this::class != other::class) return false
    if (!super.equals(other)) return false

    other as HistoryReferenceEntryConfiguration

    if (!dataSeriesIds.contentEquals(other.dataSeriesIds)) return false
    if (displayNames != other.displayNames) return false
    return statusEnums == other.statusEnums
  }

  override fun hashCode(): Int {
    var result = super.hashCode()
    result = 31 * result + dataSeriesIds.contentHashCode()
    result = 31 * result + displayNames.hashCode()
    result = 31 * result + statusEnums.hashCode()
    return result
  }


  companion object {
    val empty: HistoryReferenceEntryConfiguration = HistoryReferenceEntryConfiguration(
      dataSeriesIds = emptyIntArray(),
      displayNames = emptyList(),
      statusEnums = emptyList()
    )
  }

  /**
   * A builder class for creating a [HistoryReferenceEntryConfiguration] instance.
   */
  class Builder {
    private val dataSeriesIds: @ID IntArrayList = IntArrayList()
    private val displayNames: MutableList<TextKey> = mutableListOf()
    private val statusEnums: MutableList<HistoryEnum?> = mutableListOf()

    /**
     * Adds a reference entry data series with the specified [id], [displayName], and [statusEnum] to the builder.
     *
     * @param id the ID of the reference entry data series to add
     * @param displayName the display name of the reference entry data series to add
     * @param statusEnum the status enumeration of the reference entry data series to add
     * @throws IllegalArgumentException if the [id] is already added to the builder
     */
    fun referenceEntryDataSeries(id: DataSeriesId, displayName: String, statusEnum: HistoryEnum?) {
      referenceEntryDataSeries(id, TextKey.simple(displayName), statusEnum)
    }

    /**
     * Adds a reference entry data series with the specified [id], [displayName], and [statusEnum] to the builder.
     *
     * @param id the ID of the reference entry data series to add
     * @param displayName the display name of the reference entry data series to add
     * @param statusEnum the status enumeration of the reference entry data series to add
     *
     * @throws IllegalArgumentException if the [id] is already added to the builder
     */
    fun referenceEntryDataSeries(id: DataSeriesId, displayName: TextKey, statusEnum: HistoryEnum?) {
      require(dataSeriesIds.contains(id.value).not()) { "id <$id> already added" }

      dataSeriesIds.add(id.value)
      displayNames.add(displayName)
      statusEnums.add(statusEnum)
    }

    /**
     * Builds and returns a [HistoryReferenceEntryConfiguration] instance based on the contents of the builder.
     */
    fun build(): HistoryReferenceEntryConfiguration {
      return HistoryReferenceEntryConfiguration(dataSeriesIds.toIntArray(), displayNames.toList(), statusEnums.toList())
    }
  }
}

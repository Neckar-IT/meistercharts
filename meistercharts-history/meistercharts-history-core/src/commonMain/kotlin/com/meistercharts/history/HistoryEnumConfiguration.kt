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
 * History configuration for enum values
 */
@Serializable
class HistoryEnumConfiguration(
  /**
   * The ids of the data series.
   */
  override val dataSeriesIds: @EnumDataSeriesIndexInt IntArray,

  /**
   * The display names for each data series
   * Has the same size as [dataSeriesIds]
   */
  override val displayNames: List<TextKey>,

  /**
   * The enum configurations for each data series.
   * Has the same size as [dataSeriesIds]
   */
  val enums: List<HistoryEnum>,
) : AbstractHistoryConfiguration() {
  init {
    require(dataSeriesIds.size == enums.size) {
      "size mismatch - enum configs: ${dataSeriesIds.size} vs ${enums.size}"
    }
    require(dataSeriesIds.size == displayNames.size) {
      "size mismatch - display names: ${dataSeriesIds.size} vs ${displayNames.size}"
    }
  }

  /**
   * Returns the history enum for the given data series index
   */
  fun getEnum(dataSeriesIndex: EnumDataSeriesIndex): HistoryEnum {
    return enums[dataSeriesIndex.value]
  }

  @Slow
  fun getDataSeriesIndex(dataSeriesId: DataSeriesId): EnumDataSeriesIndex = EnumDataSeriesIndex(dataSeriesIds.indexOf(dataSeriesId.value))

  /**
   * Returns the data series id at the given index
   */
  fun getDataSeriesId(dataSeriesIndex: EnumDataSeriesIndex): DataSeriesId {
    return DataSeriesId(getDataSeriesIdAsInt(dataSeriesIndex.value))
  }

  /**
   * Returns the human-readable name for the *index* (not ID)
   */
  fun getDisplayName(dataSeriesIndex: EnumDataSeriesIndex): TextKey {
    return displayNames[dataSeriesIndex.value]
  }

  fun dump(): String {
    return buildString {
      dataSeriesIds.fastForEachIndexed { index, dataSeriesIndexAsInt ->
        append(dataSeriesIndexAsInt.toString().padStart(7))
        append(": ")
        append(displayNames[index].fallbackText.padEnd(25))
        append(" | (")
        append((enums[index].enumDescription + ")").padEnd(15))
        appendLine()
      }
    }
  }

  override fun toString(): String {
    return "HistoryEnumConfiguration(dataSeriesIds=${dataSeriesIds.contentToString()}, displayNames=$displayNames, enumConfigs=$enums)"
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || this::class != other::class) return false
    if (!super.equals(other)) return false

    other as HistoryEnumConfiguration

    if (!dataSeriesIds.contentEquals(other.dataSeriesIds)) return false
    if (displayNames != other.displayNames) return false
    if (enums != other.enums) return false

    return true
  }

  override fun hashCode(): Int {
    var result = super.hashCode()
    result = 31 * result + dataSeriesIds.contentHashCode()
    result = 31 * result + displayNames.hashCode()
    result = 31 * result + enums.hashCode()
    return result
  }

  companion object {
    val empty: HistoryEnumConfiguration = HistoryEnumConfiguration(emptyIntArray(), emptyList(), emptyList())
  }

  class Builder {
    private val dataSeriesIds: @ID IntArrayList = IntArrayList()
    private val displayNames: MutableList<TextKey> = mutableListOf()
    private val enumConfigs: MutableList<HistoryEnum> = mutableListOf()


    fun enumDataSeries(id: DataSeriesId, displayName: String, enumConfiguration: HistoryEnum) {
      enumDataSeries(id, TextKey.simple(displayName), enumConfiguration)
    }

    fun enumDataSeries(id: DataSeriesId, displayName: TextKey, enumConfiguration: HistoryEnum) {
      require(dataSeriesIds.contains(id.value).not()) { "id <$id> already added" }

      dataSeriesIds.add(id.value)
      displayNames.add(displayName)
      enumConfigs.add(enumConfiguration)
    }

    fun build(): HistoryEnumConfiguration {
      return HistoryEnumConfiguration(dataSeriesIds.toIntArray(), displayNames, enumConfigs)
    }
  }
}

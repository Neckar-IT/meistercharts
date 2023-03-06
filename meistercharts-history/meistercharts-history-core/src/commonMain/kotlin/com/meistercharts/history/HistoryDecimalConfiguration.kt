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
 * Contains the history configuration for data series that have decimal values.
 *
 * Should not be used directly - instead use [HistoryConfiguration]
 */
@Serializable
class HistoryDecimalConfiguration(
  /**
   * The ids of the data series.
   */
  override val dataSeriesIds: @ID IntArray,

  /**
   * The display names for each data series
   * Has the same size as [dataSeriesIds]
   */
  override val displayNames: List<TextKey>,

  /**
   * The units for each data series
   */
  val units: List<HistoryUnit>,

  ) : com.meistercharts.history.AbstractHistoryConfiguration() {
  init {
    require(dataSeriesIds.size == displayNames.size) {
      "size mismatch - display names: ${dataSeriesIds.size} vs ${displayNames.size}"
    }

    require(dataSeriesIds.size == units.size) {
      "size mismatch - units: ${dataSeriesIds.size} vs ${units.size}"
    }
  }

  /**
   * Returns the data series id at the given index
   */
  fun getDataSeriesId(dataSeriesIndex: DecimalDataSeriesIndex): DataSeriesId {
    return DataSeriesId(getDataSeriesIdAsInt(dataSeriesIndex.value))
  }

  /**
   * Returns the unit for the given data series index
   */
  fun getUnit(dataSeriesIndex: DecimalDataSeriesIndex): HistoryUnit {
    return units[dataSeriesIndex.value]
  }

  /**
   * Returns the human-readable name for the *index* (not ID)
   */
  fun getDisplayName(dataSeriesIndex: DecimalDataSeriesIndex): TextKey {
    return displayNames[dataSeriesIndex.value]
  }

  /**
   * Returns the data series index for a given data series id
   */
  @Slow
  fun getDataSeriesIndex(dataSeriesId: DataSeriesId): DecimalDataSeriesIndex {
    return DecimalDataSeriesIndex(dataSeriesIds.indexOf(dataSeriesId.value))
  }

  override fun toString(): String {
    return "HistoryDecimalConfiguration(dataSeriesIds=${dataSeriesIds.contentToString()},\ndisplayNames=${displayNames})"
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || this::class != other::class) return false
    if (!super.equals(other)) return false

    other as HistoryDecimalConfiguration

    if (!dataSeriesIds.contentEquals(other.dataSeriesIds)) return false
    if (displayNames != other.displayNames) return false

    return true
  }

  override fun hashCode(): Int {
    var result = super.hashCode()
    result = 31 * result + dataSeriesIds.contentHashCode()
    result = 31 * result + displayNames.hashCode()
    return result
  }

  companion object {
    /**
     * An empty history decimal configuration without any data series
     */
    val empty: HistoryDecimalConfiguration = HistoryDecimalConfiguration(emptyIntArray(), emptyList(), emptyList())
  }

  /**
   * Builder for [HistoryDecimalConfiguration]
   */
  class Builder {
    private val dataSeriesIds: @ID IntArrayList = IntArrayList()
    private val displayNames: MutableList<TextKey> = mutableListOf()
    private val units: MutableList<HistoryUnit> = mutableListOf()

    /**
     * Adds a decimal data series
     */
    fun decimalDataSeries(id: DataSeriesId, displayName: TextKey, unit: HistoryUnit = HistoryUnit.None) {
      require(dataSeriesIds.contains(id.value).not()) { "id <$id> already added" }

      dataSeriesIds.add(id.value)
      displayNames.add(displayName)
      units.add(unit)
    }

    fun decimalDataSeries(id: DataSeriesId, displayName: String, unit: HistoryUnit = HistoryUnit.None) {
      decimalDataSeries(id, TextKey.simple(displayName), unit)
    }

    fun build(): HistoryDecimalConfiguration {
      return HistoryDecimalConfiguration(dataSeriesIds.toIntArray(), displayNames, units.toList())
    }

  }
}

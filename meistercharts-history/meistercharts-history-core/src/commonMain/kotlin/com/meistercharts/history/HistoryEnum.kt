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

import it.neckar.open.i18n.TextKey
import kotlinx.serialization.Serializable

/**
 * Represents an enum configuration for a data series
 */
@Serializable
data class HistoryEnum(
  /**
   * Enum description
   */
  val enumDescription: String,

  /**
   * Contains a text key for each enum option
   */
  val values: List<HistoryEnumValue>,
) {

  val valuesCount: Int
    get() {
      return values.size
    }

  init {
    require(values.isNotEmpty()) {
      "Need at least one option"
    }

    //Since the values are stored in a bit set (int) - this is the max number of enum values
    require(values.size <= HistoryEnumSet.maxSupportedEnumValuesCount) {
      "Too many options. Was <${values.size}>"
    }
  }

  /**
   * Returns the value for the given enum ordinal
   */
  fun value(ordinal: HistoryEnumOrdinal): HistoryEnumValue {
    require(ordinal.isNoValue().not()) {
      "Must not be called with [NoValue]"
    }
    require(ordinal.isPending().not()) {
      "Must not be called with [Pending]"
    }

    require(values.size > ordinal.value){
      "Invalid ordinal: ${ordinal.value} - for values: $values"
    }

    return values[ordinal.value]
  }

  @Serializable
  data class HistoryEnumValue(
    val ordinal: HistoryEnumOrdinal,
    val key: TextKey,
  )

  companion object {
    fun createSimple(
      enumDescription: String,
      valueNames: List<String>,
    ): HistoryEnum {
      return create(enumDescription, valueNames.map { TextKey(it) })
    }

    fun create(
      enumDescription: String,
      valueNames: List<TextKey>,
    ): HistoryEnum {
      return HistoryEnum(enumDescription, valueNames.mapIndexed { index, valueName ->
        HistoryEnumValue(HistoryEnumOrdinal(index), valueName)
      })
    }

    /**
     * Represents the default boolean (true/false) enumeration
     *
     * Use
     * * [com.meistercharts.history.HistoryEnumOrdinal.Companion.BooleanTrue]
     * * [com.meistercharts.history.HistoryEnumOrdinal.Companion.BooleanFalse]
     */
    val Boolean: HistoryEnum = create("Boolean", listOf(TextKey("True"), TextKey("False")))

    /**
     * Represents active/inactive enumeration
     */
    val Active: HistoryEnum = create("Active", listOf(TextKey("Active"), TextKey("Inactive")))
  }
}

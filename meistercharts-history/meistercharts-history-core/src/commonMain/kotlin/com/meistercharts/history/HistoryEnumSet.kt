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

import it.neckar.open.kotlin.lang.fastFor
import it.neckar.open.kotlin.lang.findLowestOneBit
import it.neckar.open.kotlin.lang.isBitSet
import kotlin.jvm.JvmInline

/**
 * Represents the union of selected enum values.
 */
@JvmInline
value class HistoryEnumSet(val bitset: @HistoryEnumSetInt Int) {
  init {
    isValid(bitset)
  }

  fun isNoValue(): Boolean {
    return isNoValue(bitset)
  }

  fun isPending(): Boolean {
    return isPending(bitset)
  }

  /**
   * Returns true if the bit at the given index has been set
   */
  fun isSet(index: HistoryEnumOrdinal): Boolean {
    return bitset.isBitSet(index.value)
  }

  override fun toString(): String {
    if (isNoValue()) {
      return "-"
    }

    if (isPending()) {
      return "?"
    }

    return "0b${bitset.toString(2)}"
  }

  /**
   * Returns the ordinal of the first set bit.
   *
   * Attention: Will return special values if [isPending] or [isNoValue]
   */
  fun firstSetOrdinal(): @MayBeNoValueOrPending HistoryEnumOrdinal {
    if (isPending()) {
      return HistoryEnumOrdinal.Pending
    }
    if (isNoValue()) {
      return HistoryEnumOrdinal.NoValue
    }

    val bitIndex = bitset.findLowestOneBit().also {
      require(it > -1) {
        "Could not find index for $bitset"
      }
    }
    return HistoryEnumOrdinal(bitIndex)
  }

  /**
   * Iterates over all bits and calls the callback for all set bits
   */
  inline fun fastForSetBits(function: (HistoryEnumOrdinal) -> Unit) {
    maxSupportedEnumValuesCount.fastFor { ordinalAsInt ->
      val ordinal = HistoryEnumOrdinal(ordinalAsInt)

      if (isSet(ordinal)) {
        function(ordinal)
      }
    }
  }

  /**
   * Returns true if this has a valid value
   * (Not pending and not no value)
   */
  fun hasValidValue(): Boolean {
    return isPending().not() && isNoValue().not()
  }

  companion object {
    fun forEnumValue(enumOrdinal: Int): HistoryEnumSet {
      return forEnumOrdinal(HistoryEnumOrdinal(enumOrdinal))
    }

    /**
     * Returns an enum set for the given enum value (not a bitset!)
     */
    fun forEnumOrdinal(enumOrdinal: HistoryEnumOrdinal): HistoryEnumSet {
      if (enumOrdinal == HistoryEnumOrdinal.NoValue) {
        return NoValue
      }
      if (enumOrdinal == HistoryEnumOrdinal.Pending) {
        return Pending
      }

      return HistoryEnumSet(1 shl enumOrdinal.value)
    }

    fun isPending(value: @HistoryEnumSetInt Int): Boolean {
      return value == PendingAsInt
    }

    fun isNoValue(value: @HistoryEnumSetInt Int): Boolean {
      return value == NoValueAsInt
    }

    /**
     * Throws an exception if the given int value is not valid (e.g. too large)
     */
    fun isValid(bitset: @HistoryEnumSetInt Int) {
      // 0 should technically represent NoValue.
      // But it is too dangerous - 0 could be set as value accidentally
      require(bitset > 0) {
        "0 is not allowed in this bit set."
      }
    }

    /**
     * Only the first enum value is selected
     */
    val first: HistoryEnumSet = HistoryEnumSet(0b1)

    /**
     * Only the second enum value is selected
     */
    val second: HistoryEnumSet = HistoryEnumSet(0b10)

    /**
     * Only the third enum value is selected
     */
    val third: HistoryEnumSet = HistoryEnumSet(0b100)

    /**
     * Only the fourth enum value is selected
     */
    val fourth: HistoryEnumSet = HistoryEnumSet(0b1000)

    /**
     * This value implies that a sample for a given timestamp is pending.
     *
     * Consequently, there are no values for data series yet, but there might be values in the future.
     *
     * @see NoValueAsInt
     */
    const val PendingAsInt: @HistoryEnumSetInt Int = Int.MAX_VALUE

    /**
     * This value implies that a sample for a given timestamp is pending.
     */
    val Pending: HistoryEnumSet = HistoryEnumSet(PendingAsInt)

    /**
     * This value implies that a sample has been taken but the sample does not contain a valid value for the data series.
     *
     * TODO: The technical correct value for this should be "0". But this is too error prone. Maybe change later
     *
     * @see PendingAsInt
     */
    const val NoValueAsInt: @HistoryEnumSetInt Int = Int.MAX_VALUE - 1

    /**
     * This value implies that a sample has been taken but the sample does not contain a valid value for the data series.
     */
    val NoValue: HistoryEnumSet = HistoryEnumSet(NoValueAsInt)

    /**
     * The maximum number of supported enum values
     */
    const val maxSupportedEnumValuesCount: Int = 30

    /**
     * The max supported value that is still allowed
     */
    private const val MaxValue = 0b111111_11111111_11111111_11111111

    /**
     * The maximum value that is currently supported
     */
    val Max: HistoryEnumSet = HistoryEnumSet(MaxValue)
  }
}

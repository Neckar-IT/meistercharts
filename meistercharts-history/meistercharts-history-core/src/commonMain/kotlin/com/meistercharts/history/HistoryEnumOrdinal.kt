package com.meistercharts.history

import com.meistercharts.annotations.Domain
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

/**
 * An ordinal value for a history enum entry, represented as an integer value.
 *
 * Represents an index *within* the history enum index
 *
 * @param value the integer value of the history enum ordinal.
 */
@Serializable
@JvmInline
value class HistoryEnumOrdinal(val value: Int) {

  /**
   * Returns true if this history enum ordinal represents a pending value, false otherwise.
   */
  fun isPending(): Boolean {
    return this.value == HistoryEnumSet.PendingAsInt
  }

  /**
   * Returns true if this history enum ordinal represents a no-value value, false otherwise.
   */
  fun isNoValue(): Boolean {
    return this.value == HistoryEnumSet.NoValueAsInt
  }

  override fun toString(): String {
    if (isPending()) {
      return "?"
    }
    if (isNoValue()) {
      return "-"
    }
    return "$value"
  }

  companion object {
    /**
     * Represents a history enum ordinal for a [HistoryEnumSet.Pending]
     */
    val Pending: HistoryEnumOrdinal = HistoryEnumOrdinal(HistoryEnumSet.PendingAsInt)

    /**
     * Represents a history enum ordinal for a [HistoryEnumSet.NoValue]
     */
    val NoValue: HistoryEnumOrdinal = HistoryEnumOrdinal(HistoryEnumSet.NoValueAsInt)

    /**
     * Represents the first history enum ordinal.
     */
    val First: HistoryEnumOrdinal = HistoryEnumOrdinal(0)

    /**
     * Represents the second history enum ordinal.
     */
    val Second: HistoryEnumOrdinal = HistoryEnumOrdinal(1)

    /**
     * The maximum value that is supported.
     * Look at [HistoryEnumSet] for details why this is the limit
     */
    val Max: HistoryEnumOrdinal = HistoryEnumOrdinal(HistoryEnumSet.maxSupportedEnumValuesCount - 1)


    /**
     * Constant that can be used with [com.meistercharts.history.HistoryEnum.Companion.Boolean]
     */
    val BooleanTrue: HistoryEnumOrdinal = First

    /**
     * Constant that can be used with [com.meistercharts.history.HistoryEnum.Companion.Boolean]
     */
    val BooleanFalse: HistoryEnumOrdinal = Second

    /**
     * Returns the ordinal value for a boolean.
     * This method must only be used with [com.meistercharts.history.HistoryEnum.Companion.Boolean].
     *
     * @param state the boolean value to create an ordinal for.
     * @return the ordinal value for the specified boolean.
     */
    fun forBoolean(state: @Domain Boolean): HistoryEnumOrdinal {
      return when (state) {
        true -> BooleanTrue
        false -> BooleanFalse
      }
    }
  }
}

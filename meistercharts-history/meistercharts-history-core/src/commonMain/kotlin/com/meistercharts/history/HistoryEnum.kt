package com.meistercharts.history

import com.meistercharts.annotations.Domain
import it.neckar.open.i18n.TextKey
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

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

/**
 * Represents an index *within* the history enum index
 */
@Serializable
@JvmInline
value class HistoryEnumOrdinal(val value: Int) {
  fun isPending(): Boolean {
    return this.value == HistoryEnumSet.PendingAsInt
  }

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

    val First: HistoryEnumOrdinal = HistoryEnumOrdinal(0)
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
     * Creates the ordinal for a boolean.
     * Must only be used with [com.meistercharts.history.HistoryEnum.Companion.Boolean]
     */
    fun forBoolean(state: @Domain Boolean): HistoryEnumOrdinal {
      return when (state) {
        true -> BooleanTrue
        false -> BooleanFalse
      }
    }
  }
}

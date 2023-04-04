package com.meistercharts.api

import com.meistercharts.history.HistoryEnum
import com.meistercharts.history.HistoryEnumOrdinal
import com.meistercharts.history.HistoryEnumSet
import it.neckar.open.charting.api.sanitizing.sanitize
import it.neckar.open.i18n.TextKey


/**
 * Converts the (JS) enum configuration to a history enum
 */
fun EnumConfiguration.toHistoryEnum(): HistoryEnum {
  return HistoryEnum(this.description.sanitize(), this.values.map {
    HistoryEnum.HistoryEnumValue(HistoryEnumOrdinal(it.ordinal.sanitize()), TextKey(it.label.sanitize()))
  }).also { historyEnum ->
    //Verify that there are 16 entries
    require(historyEnum.valuesCount >= 16) {
      "Need a fully filled enum configuration with at least 16 entries. But got only <${historyEnum.valuesCount}>"
    }
  }
}

/**
 * Converts a *Double* that is provided by JS to a history enum set
 */
fun HistoryEnumSet.Companion.forEnumValueFromJsDouble(jsValue: Double): HistoryEnumSet {
  if (jsValue.isNaN()) {
    return NoValue
  }

  val jsValueAsInt = jsValue.toInt()
  return forEnumValue(jsValueAsInt)
}

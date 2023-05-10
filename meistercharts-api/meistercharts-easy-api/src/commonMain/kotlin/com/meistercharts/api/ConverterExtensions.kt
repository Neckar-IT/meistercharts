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
  return HistoryEnum(this.description.sanitize(), this.values.sanitize().map {
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
fun HistoryEnumSet.Companion.forEnumValueFromJsDouble(jsValue: Double?): HistoryEnumSet {
  if (jsValue == null || jsValue.isNaN()) {
    return NoValue
  }

  val jsValueAsInt = jsValue.toInt()
  return forEnumValue(jsValueAsInt)
}

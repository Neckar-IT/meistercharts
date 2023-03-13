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
package com.meistercharts.history.impl.io

import com.meistercharts.history.DataSeriesId
import com.meistercharts.history.HistoryEnum
import com.meistercharts.history.HistoryUnit
import com.meistercharts.history.historyConfiguration
import it.neckar.open.serialization.roundTrip
import it.neckar.open.i18n.TextKey
import org.junit.jupiter.api.Test

/**
 *
 */
class HistoryConfigurationSerializerTest {
  @Test
  fun testDecimals() {
    roundTrip(
      historyConfiguration {
        decimalDataSeries(DataSeriesId(17), TextKey.simple("hello"), HistoryUnit.None)
        decimalDataSeries(DataSeriesId(18), TextKey.simple("Val2"), HistoryUnit("da Unit"))
      }
    ) {
      //language=JSON
      """
        {
          "decimalConfiguration" : {
            "dataSeriesIds" : [ 17, 18 ],
            "displayNames" : [ {
              "key" : "hello",
              "fallbackText" : "hello"
            }, {
              "key" : "Val2",
              "fallbackText" : "Val2"
            } ],
            "units" : [ null, "da Unit" ]
          },
          "enumConfiguration" : {
            "dataSeriesIds" : [ ],
            "displayNames" : [ ],
            "enums" : [ ]
          },
          "referenceEntryConfiguration" : {
            "dataSeriesIds" : [ ],
            "displayNames" : [ ]
          }
        }
        """.trimIndent()
    }
  }

  @Test
  fun testReferenceEntries() {
    roundTrip(historyConfiguration {
      referenceEntryDataSeries(DataSeriesId(17), TextKey.simple("hello"))
      referenceEntryDataSeries(DataSeriesId(18), TextKey.simple("Val2"))
    }) {
      //language=JSON
      """
        {
          "decimalConfiguration" : {
            "dataSeriesIds" : [ ],
            "displayNames" : [ ],
            "units" : [ ]
          },
          "enumConfiguration" : {
            "dataSeriesIds" : [ ],
            "displayNames" : [ ],
            "enums" : [ ]
          },
          "referenceEntryConfiguration" : {
            "dataSeriesIds" : [ 17, 18 ],
            "displayNames" : [ {
              "key" : "hello",
              "fallbackText" : "hello"
            }, {
              "key" : "Val2",
              "fallbackText" : "Val2"
            } ]
          }
        }
      """.trimIndent()
    }
  }

  @Test
  fun testEnums() {
    roundTrip(historyConfiguration {
      enumDataSeries(DataSeriesId(17), TextKey.simple("hello"), HistoryEnum.Active)
      enumDataSeries(DataSeriesId(18), TextKey.simple("Val2"), HistoryEnum.Boolean)
    }) {
      //language=JSON
      """
        {
          "decimalConfiguration" : {
            "dataSeriesIds" : [ ],
            "displayNames" : [ ],
            "units" : [ ]
          },
          "enumConfiguration" : {
            "dataSeriesIds" : [ 17, 18 ],
            "displayNames" : [ {
              "key" : "hello",
              "fallbackText" : "hello"
            }, {
              "key" : "Val2",
              "fallbackText" : "Val2"
            } ],
            "enums" : [ {
              "enumDescription" : "Active",
              "values" : [ {
                "ordinal" : 0,
                "key" : {
                  "key" : "Active",
                  "fallbackText" : "Active"
                }
              }, {
                "ordinal" : 1,
                "key" : {
                  "key" : "Inactive",
                  "fallbackText" : "Inactive"
                }
              } ]
            }, {
              "enumDescription" : "Boolean",
              "values" : [ {
                "ordinal" : 0,
                "key" : {
                  "key" : "True",
                  "fallbackText" : "True"
                }
              }, {
                "ordinal" : 1,
                "key" : {
                  "key" : "False",
                  "fallbackText" : "False"
                }
              } ]
            } ]
          },
          "referenceEntryConfiguration" : {
            "dataSeriesIds" : [ ],
            "displayNames" : [ ]
          }
        }
      """.trimIndent()
    }
  }
}

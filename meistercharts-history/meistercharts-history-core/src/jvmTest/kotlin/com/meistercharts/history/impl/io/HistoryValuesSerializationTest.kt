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

import com.meistercharts.history.TimestampIndex
import com.meistercharts.history.impl.RecordingType
import com.meistercharts.history.impl.historyValues
import it.neckar.open.serialization.roundTrip
import org.junit.jupiter.api.Test

/**
 */
class HistoryValuesSerializationTest {
  @Test
  fun testMeasured() {
    val historyValues = historyValues(2, 1, 1, 3, RecordingType.Measured) {
      setAllValuesForTimestamp(
        timestampIndex = TimestampIndex(0),
        decimalValues = doubleArrayOf(1.0, 2.0),
        minValues = null,
        maxValues = null,
        enumValues = intArrayOf(7),
        enumOrdinalsMostTime = null,
        referenceEntryIds = intArrayOf(6),
        referenceEntryStatuses = intArrayOf(3),
        entryDataSet = emptySet()
      )
      setAllValuesForTimestamp(
        timestampIndex = TimestampIndex(1),
        decimalValues = doubleArrayOf(1.1, 2.1),
        minValues = null,
        maxValues = null,
        enumValues = intArrayOf(8),
        enumOrdinalsMostTime = null,
        referenceEntryIds = intArrayOf(7),
        referenceEntryStatuses = intArrayOf(3),
        entryDataSet = emptySet()
      )
      setAllValuesForTimestamp(
        timestampIndex = TimestampIndex(2),
        decimalValues = doubleArrayOf(1.2, 2.2),
        minValues = null,
        maxValues = null,
        enumValues = intArrayOf(9),
        enumOrdinalsMostTime = null,
        referenceEntryIds = intArrayOf(8),
        referenceEntryStatuses = intArrayOf(3),
        entryDataSet = emptySet()
      )
    }

    roundTrip(historyValues) {
      //language=JSON
      """
        {
          "decimalHistoryValues" : {
            "values" : "AAIAAz/wAAAAAAAAQAAAAAAAAAA/8ZmZmZmZmkAAzMzMzMzNP/MzMzMzMzNAAZmZmZmZmg==",
            "minValues" : null,
            "maxValues" : null
          },
          "enumHistoryValues" : {
            "values" : "AAEAAwAAAAcAAAAIAAAACQ==",
            "mostOfTheTimeValues" : null
          },
          "referenceEntryHistoryValues" : {
            "values" : "AAEAAwAAAAYAAAAHAAAACA==",
              "differentIdsCount" : null,
              "statuses" : "AAEAAwAAAAMAAAADAAAAAw==",
              "dataMap" : {
                "type" : "Default",
                "entries" : { }
              }
          }
        }
      """.trimIndent()
    }
  }

  @Test
  fun testCalculated() {
    val historyValues = historyValues(2, 1, 1, 3, RecordingType.Calculated) {
      setAllValuesForTimestamp(
        timestampIndex = TimestampIndex(0),
        decimalValues = doubleArrayOf(1.0, 2.0),
        minValues = doubleArrayOf(0.5, 1.5),
        maxValues = doubleArrayOf(5.5, 6.5),
        enumValues = intArrayOf(7),
        enumOrdinalsMostTime = intArrayOf(11),
        referenceEntryIds = intArrayOf(6),
        referenceEntryDifferentIdsCount = intArrayOf(1),
        referenceEntryStatuses = intArrayOf(3),
        entryDataSet = emptySet(),
      )
      setAllValuesForTimestamp(
        timestampIndex = TimestampIndex(1),
        decimalValues = doubleArrayOf(1.1, 2.1),
        minValues = doubleArrayOf(0.6, 1.6),
        maxValues = doubleArrayOf(5.6, 6.6),
        enumValues = intArrayOf(8),
        enumOrdinalsMostTime = intArrayOf(12),
        referenceEntryIds = intArrayOf(7),
        referenceEntryDifferentIdsCount = intArrayOf(1),
        referenceEntryStatuses = intArrayOf(4),
        entryDataSet = emptySet(),
      )
      setAllValuesForTimestamp(
        timestampIndex = TimestampIndex(2),
        decimalValues = doubleArrayOf(1.2, 2.2),
        minValues = doubleArrayOf(0.7, 1.7),
        maxValues = doubleArrayOf(5.7, 6.7),
        enumValues = intArrayOf(9),
        enumOrdinalsMostTime = intArrayOf(13),
        referenceEntryIds = intArrayOf(8),
        referenceEntryDifferentIdsCount = intArrayOf(1),
        referenceEntryStatuses = intArrayOf(5),
        entryDataSet = emptySet(),
      )
    }

    roundTrip(historyValues) {
      //language=JSON
      """
        {
          "decimalHistoryValues" : {
            "values" : "AAIAAz/wAAAAAAAAQAAAAAAAAAA/8ZmZmZmZmkAAzMzMzMzNP/MzMzMzMzNAAZmZmZmZmg==",
            "minValues" : "AAIAAz/gAAAAAAAAP/gAAAAAAAA/4zMzMzMzMz/5mZmZmZmaP+ZmZmZmZmY/+zMzMzMzMw==",
            "maxValues" : "AAIAA0AWAAAAAAAAQBoAAAAAAABAFmZmZmZmZkAaZmZmZmZmQBbMzMzMzM1AGszMzMzMzQ=="
          },
          "enumHistoryValues" : {
            "values" : "AAEAAwAAAAcAAAAIAAAACQ==",
            "mostOfTheTimeValues" : "AAEAAwAAAAsAAAAMAAAADQ=="
          },
          "referenceEntryHistoryValues" : {
            "values" : "AAEAAwAAAAYAAAAHAAAACA==",
            "differentIdsCount" : "AAEAAwAAAAEAAAABAAAAAQ==",
            "statuses" : "AAEAAwAAAAMAAAAEAAAABQ==",
            "dataMap" : {
              "type" : "Default",
              "entries" : { }
            }
          }
        }
      """.trimIndent()
    }
  }
}

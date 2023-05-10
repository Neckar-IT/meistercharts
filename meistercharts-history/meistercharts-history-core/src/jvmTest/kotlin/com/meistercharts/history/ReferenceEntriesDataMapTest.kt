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

import assertk.*
import assertk.assertions.*
import it.neckar.open.i18n.TextKey
import it.neckar.open.serialization.roundTrip
import org.junit.jupiter.api.Test

class ReferenceEntriesDataMapTest {
  @Test
  fun testEmptySerialization() {
    roundTrip(ReferenceEntriesDataMap.Empty, ReferenceEntriesDataMap.serializer()) {
      //language=JSON
      """{
           "type" : "Empty"
        }""".trimMargin()
    }
  }

  @Test
  fun testGenerated() {
    assertThat(ReferenceEntriesDataMap.generated.get(ReferenceEntryId(17))).isNotNull()

    roundTrip(ReferenceEntriesDataMap.generated, ReferenceEntriesDataMap.serializer()) {
      //language=JSON
      """{
           "type" : "Generated"
        }""".trimMargin()
    }
  }

  @Test
  fun testSerializationDefaultEmpty() {
    roundTrip(DefaultReferenceEntriesDataMap(emptyMap()), ReferenceEntriesDataMap.serializer()) {
      //language=JSON
      """
        {
          "type" : "Default",
          "entries" : { }
        }
      """.trimIndent()
    }
  }

  @Test
  fun testSerializationDefaultSimple() {
    val entriesDataMapBuilder = DefaultReferenceEntriesDataMap.Builder()
    entriesDataMapBuilder.store(ReferenceEntryData(id = ReferenceEntryId(17), label = TextKey.simple("daLabel"), start = 1000.0, end = 2000.0, payload = UnparsedJson("{}")))

    roundTrip(entriesDataMapBuilder.build(), ReferenceEntriesDataMap.serializer()) {
      //language=JSON
      """
        {
          "type" : "Default",
          "entries" : {
            "17" : {
              "id" : 17,
              "label" : {
                "key" : "daLabel",
                "fallbackText" : "daLabel"
              },
              "start" : 1000.0,
              "end" : 2000.0,
              "payload" : "{}"
            }
          }
        }
      """.trimIndent()
    }
  }

}

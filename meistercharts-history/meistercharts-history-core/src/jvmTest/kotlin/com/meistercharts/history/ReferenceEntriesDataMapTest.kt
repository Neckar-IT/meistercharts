package com.meistercharts.history

import assertk.*
import assertk.assertions.*
import it.neckar.open.serialization.roundTrip
import it.neckar.open.i18n.TextKey
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
    entriesDataMapBuilder.store(ReferenceEntryData(ReferenceEntryId(17), TextKey.simple("daLabel"), UnparsedJson("{}")))

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
              "payload" : "{}"
            }
          }
        }
      """.trimIndent()
    }
  }

}

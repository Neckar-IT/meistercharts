package com.meistercharts.history.impl.io

import com.meistercharts.history.ReferenceEntryData
import com.meistercharts.history.ReferenceEntryId
import com.meistercharts.history.UnparsedJson
import it.neckar.open.serialization.roundTrip
import it.neckar.open.i18n.TextKey
import org.junit.jupiter.api.Test

class ReferenceEntryDataTest {
  @Test
  fun testSerialization() {
    roundTrip(ReferenceEntryData(ReferenceEntryId(351583), TextKey.simple("The label"), UnparsedJson("{the unparsed json}"))) {
      //language=JSON
      """
        {
          "id" : 351583,
          "label" : {
            "key" : "The label",
            "fallbackText" : "The label"
          },
          "payload" : "{the unparsed json}"
        }
      """.trimIndent()
    }

    roundTrip(ReferenceEntryData(ReferenceEntryId(351583), TextKey.simple("The label"), null)) {
      //language=JSON
      """
        {
          "id" : 351583,
          "label" : {
            "key" : "The label",
            "fallbackText" : "The label"
          },
          "payload" : null
        }
      """.trimIndent()
    }
  }
}

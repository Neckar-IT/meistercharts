package com.meistercharts.history.impl

import it.neckar.open.collections.DoubleArray2
import it.neckar.open.serialization.roundTrip
import org.junit.jupiter.api.Test

class DecimalHistoryValuesTest {
  @Test
  fun testSerialization() {
    roundTrip(
      DecimalHistoryValues(
        values = DoubleArray2(2, 3) { it * 4.4 },
        minValues = DoubleArray2(2, 3) { it * 8.9 },
        maxValues = DoubleArray2(2, 3) { it * 9.9 },
      )
    ) {
      //language=JSON
      """
      {
        "values" : "AAIAAwAAAAAAAAAAQBGZmZmZmZpAIZmZmZmZmkAqZmZmZmZnQDGZmZmZmZpANgAAAAAAAA==",
        "minValues" : "AAIAAwAAAAAAAAAAQCHMzMzMzM1AMczMzMzMzUA6szMzMzM0QEHMzMzMzM1ARkAAAAAAAA==",
        "maxValues" : "AAIAAwAAAAAAAAAAQCPMzMzMzM1AM8zMzMzMzUA9szMzMzM0QEPMzMzMzM1ASMAAAAAAAA=="
      }
      """.trimIndent()
    }
  }

  @Test
  fun testSerializationNullValues() {
    roundTrip(
      DecimalHistoryValues(
        values = DoubleArray2(2, 3) { it * 4.4 },
        minValues = null,
        maxValues = null,
      )
    ) {
      """
      {
        "values" : "AAIAAwAAAAAAAAAAQBGZmZmZmZpAIZmZmZmZmkAqZmZmZmZnQDGZmZmZmZpANgAAAAAAAA==",
        "minValues" : null,
        "maxValues" : null
      }
      """.trimIndent()
    }
  }
}

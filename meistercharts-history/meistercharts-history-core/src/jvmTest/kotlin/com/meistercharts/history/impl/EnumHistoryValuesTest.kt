package com.meistercharts.history.impl

import it.neckar.open.collections.IntArray2
import it.neckar.open.serialization.roundTrip
import org.junit.jupiter.api.Test

class EnumHistoryValuesTest {
  @Test
  fun testIt() {
    val enumHistoryValues = EnumHistoryValues(IntArray2(3, 4) { 17 * it }, null)

    roundTrip(enumHistoryValues) {
      //language=JSON
      """
        {
          "values" : "AAMABAAAAAAAAAARAAAAIgAAADMAAABEAAAAVQAAAGYAAAB3AAAAiAAAAJkAAACqAAAAuw==",
          "mostOfTheTimeValues" : null
        }
      """.trimIndent()
    }
  }
}

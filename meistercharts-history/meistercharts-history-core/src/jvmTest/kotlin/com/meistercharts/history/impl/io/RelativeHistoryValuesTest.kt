package com.meistercharts.history.impl.io

import com.meistercharts.history.impl.HistoryValues
import it.neckar.open.serialization.roundTrip
import kotlinx.serialization.json.Json
import org.assertj.core.api.AssertionsForClassTypes.assertThat
import org.junit.jupiter.api.Test

/**
 */
@Deprecated("No longer required")
class RelativeHistoryValuesTest {
  @Test
  fun testSerialize() {
    val values: RelativeHistoryValues = HistoryValues(
      decimalsDataArray = arrayOf(
        doubleArrayOf(1.0, 2.0, 3.0),
        doubleArrayOf(10.0, 20.0, 30.0),
        doubleArrayOf(100.0, 200.0, 300.0),
        doubleArrayOf(1000.0, 2000.0, 3000.0)
      ),
      enumDataArray = arrayOf(
        intArrayOf(1, 2, 3),
        intArrayOf(10, 20, 30),
        intArrayOf(100, 200, 300),
        intArrayOf(1000, 2000, 3000)
      ),
      referenceEntryDataArray = arrayOf(
        intArrayOf(7, 8, 9),
        intArrayOf(70, 80, 90),
        intArrayOf(700, 800, 900),
        intArrayOf(7000, 8000, 9000)
      )
    ).makeRelative()

    roundTrip(values) {
      //language=JSON
      """
        {
          "decimalValues" : "AAQAAz/wAAAAAAAAQCQAAAAAAABAWQAAAAAAAECPQAAAAAAAP/AAAAAAAABAJAAAAAAAAEBZAAAAAAAAQI9AAAAAAAA/8AAAAAAAAEAkAAAAAAAAQFkAAAAAAABAj0AAAAAAAA==",
          "enumValues" : "AAQAAwAAAAEAAAAKAAAAZAAAA+gAAAABAAAACgAAAGQAAAPoAAAAAQAAAAoAAABkAAAD6A==",
          "referenceEntryHistoryValues" : "AAQAAwAAAAcAAABGAAACvAAAG1gAAAABAAAACgAAAGQAAAPoAAAAAQAAAAoAAABkAAAD6A=="
        }
      """.trimIndent()
    }
  }
}

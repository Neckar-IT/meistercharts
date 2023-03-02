package com.meistercharts.algorithms

import kotlinx.serialization.KSerializer
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.serializer
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import it.neckar.open.serialization.roundTrip


class TimeRangeSerializerTest {
  @Test
  fun testSerialization() {
    roundTrip(TimeRange(10001.0, 400000.0), TimeRangeSerializer) {
      """
        {
          "start" : 10001.0,
          "end" : 400000.0
        }
      """.trimIndent()
    }

  }

}

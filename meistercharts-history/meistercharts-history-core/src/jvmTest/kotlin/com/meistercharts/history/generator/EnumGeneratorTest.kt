package com.meistercharts.history.generator

import assertk.*
import assertk.assertions.*
import com.meistercharts.history.HistoryEnum
import com.meistercharts.history.HistoryEnumSet
import it.neckar.open.test.utils.RandomWithSeed
import org.junit.jupiter.api.Test
import kotlin.time.Duration.Companion.seconds

class EnumGeneratorTest {
  @RandomWithSeed(123132)
  @Test
  fun testRandom() {
    EnumValueGenerator.modulo(step = 1.seconds).let {
      assertThat(it.generate(123_123.0, HistoryEnum.Boolean)).isEqualTo(HistoryEnumSet.second)
      assertThat(it.generate(123_123.0, HistoryEnum.Boolean)).isEqualTo(HistoryEnumSet.second)

      assertThat(it.generate(124_124.0, HistoryEnum.Boolean)).isEqualTo(HistoryEnumSet.first)
    }
  }
}

package com.meistercharts.history.generator

import assertk.*
import assertk.assertions.*
import com.meistercharts.algorithms.ValueRange
import it.neckar.open.test.utils.RandomWithSeed
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test


class ValueGeneratorTest {
  @RandomWithSeed(123)
  @Test
  fun testRandom() {
    val generator = RandomNormalDecimalValueGenerator(ValueRange.linear(10.0, 20.0), 5.0)
    assertThat(generator.generate(0.0)).isEqualTo(20.0) //exact: 20.615264095998704
    assertThat(generator.generate(1.0)).isEqualTo(15.114210713249621)
    assertThat(generator.generate(2.0)).isEqualTo(14.446353158493093)
    assertThat(generator.generate(3.0)).isEqualTo(10.0)
    assertThat(generator.generate(3.1)).isEqualTo(10.842555432750736)
  }
}

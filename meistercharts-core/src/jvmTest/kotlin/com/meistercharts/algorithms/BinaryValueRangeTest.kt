package com.meistercharts.algorithms

import assertk.*
import assertk.assertions.*
import org.junit.jupiter.api.Test

/**
 */
internal class BinaryValueRangeTest {
  @Test
  fun testIt() {
    val range = BinaryValueRange

    assertThat(range.start).isEqualTo(0.0)
    assertThat(range.end).isEqualTo(1.0)
    assertThat(range.toDomainRelative(true)).isEqualTo(1.0)
    assertThat(range.toDomainRelative(false)).isEqualTo(0.0)

    assertThat(range.toDomain(1.0)).isEqualTo(1.0)
    assertThat(range.toDomain(0.0)).isEqualTo(0.0)
    assertThat(range.toDomainBoolean(1.0)).isEqualTo(true)
    assertThat(range.toDomainBoolean(0.0)).isEqualTo(false)
  }
}

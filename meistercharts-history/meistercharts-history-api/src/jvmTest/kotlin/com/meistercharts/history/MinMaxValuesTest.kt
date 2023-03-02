package com.meistercharts.history

import assertk.*
import assertk.assertions.*
import org.junit.jupiter.api.Test

class MinMaxValuesTest {
  @Test
  fun testEmpty() {
    val minMaxValues = MinMaxValues.Builder(listOf(DecimalDataSeriesIndex.zero)).build()
    assertThat(minMaxValues.min(DecimalDataSeriesIndex.zero)).isNull()
    assertThat(minMaxValues.max(DecimalDataSeriesIndex.zero)).isNull()
  }
}

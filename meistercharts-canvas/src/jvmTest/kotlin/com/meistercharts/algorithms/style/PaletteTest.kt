package com.meistercharts.algorithms.style

import assertk.*
import assertk.assertions.*
import com.meistercharts.style.Palette.chartColors
import org.junit.jupiter.api.Test

/**
 */
class PaletteTest {
  @Test
  internal fun testIt() {
    assertThat(chartColors).hasSize(6)
  }
}

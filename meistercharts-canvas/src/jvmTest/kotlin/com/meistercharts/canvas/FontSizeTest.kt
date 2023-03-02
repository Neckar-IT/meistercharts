package com.meistercharts.canvas

import assertk.*
import assertk.assertions.*
import org.junit.jupiter.api.Test

class FontSizeTest {
  @Test
  fun testPoints2pixels() {
    assertThat(FontSize.points2pixels(6.0)).isEqualTo(8.0)
    assertThat(FontSize.points2pixels(7.0)).isEqualTo(9.0)
    assertThat(FontSize.points2pixels(7.5)).isEqualTo(10.0)
    assertThat(FontSize.points2pixels(24.0)).isEqualTo(32.0)
  }
}

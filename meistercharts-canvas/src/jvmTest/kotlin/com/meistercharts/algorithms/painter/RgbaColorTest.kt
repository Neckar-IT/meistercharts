package com.meistercharts.algorithms.painter

import assertk.*
import assertk.assertions.*
import org.junit.jupiter.api.Test

/**
 *
 */
class RgbaColorTest {
  @Test
  fun testTint() {
    assertThat(RgbaColor(64, 168, 245).formatRgba()).isEqualTo("rgb(64,168,245)")

    assertThat(RgbaColor(64, 168, 245).lighter(0.2).formatRgba()).isEqualTo(RgbaColor(102, 185, 247).formatRgba())
    assertThat(RgbaColor(64, 168, 245).lighter(0.7).formatRgba()).isEqualTo(RgbaColor(198, 229, 252).formatRgba())

    assertThat(RgbaColor(159, 211, 213).lighter(0.2).formatRgba()).isEqualTo(RgbaColor(178, 220, 221).formatRgba())
    assertThat(RgbaColor(159, 211, 213).lighter(0.7).formatRgba()).isEqualTo(RgbaColor(226, 242, 242).formatRgba())
  }
}

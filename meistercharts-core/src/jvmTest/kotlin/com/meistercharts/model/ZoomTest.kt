package com.meistercharts.model

import assertk.*
import assertk.assertions.*
import org.junit.jupiter.api.Test

class ZoomTest {
  @Test
  fun testSmaller() {
    Zoom.of(1.0, 1.0).let {
      assertThat(it.smallerValueForBoth()).isSameAs(it)
    }

    Zoom.of(1.0, 2.0).let {
      assertThat(it.smallerValueForBoth()).isEqualTo(Zoom.of(1.0, 1.0))
    }

    Zoom.of(1.5, 2.0).let {
      assertThat(it.smallerValueForBoth()).isEqualTo(Zoom.of(1.5, 1.5))
    }

    Zoom.of(2.0, 1.8).let {
      assertThat(it.smallerValueForBoth()).isEqualTo(Zoom.of(1.8, 1.8))
    }
  }

  @Test
  fun testMin() {
    Zoom.of(1.0, 1.0).let {
      assertThat(it.withMin(0.0, 0.0)).isSameAs(it)
      assertThat(it.withMin(1.0, 1.0)).isSameAs(it)

      assertThat(it.withMin(1.1, 1.0)).isEqualTo(Zoom.of(1.1, 1.0))
      assertThat(it.withMin(1.1, 0.1)).isEqualTo(Zoom.of(1.1, 1.0))
    }
  }

  @Test
  fun testMax() {
    Zoom.of(1.0, 1.0).let {
      assertThat(it.withMax(2.0, 2.0)).isSameAs(it)
      assertThat(it.withMax(1.0, 1.0)).isSameAs(it)
      assertThat(it.withMax(0.1, 1.0)).isEqualTo(Zoom.of(0.1, 1.0))
      assertThat(it.withMax(0.1, 2.1)).isEqualTo(Zoom.of(0.1, 1.0))
    }
  }

  @Test
  fun testMinX() {
    Zoom.of(1.0, 1.0).let {
      assertThat(it.withMinX(0.0)).isSameAs(it)
      assertThat(it.withMinX(1.0)).isSameAs(it)

      assertThat(it.withMinX(1.1)).isEqualTo(Zoom.of(1.1, 1.0))
    }
  }

  @Test
  fun testMultiply() {
    assertThat(Zoom.of(1.0, 1.0).multiply(1.0, 1.0)).isEqualTo(Zoom.default)
    assertThat(Zoom.of(2.0, 3.0).multiply(2.0, 4.0)).isEqualTo(Zoom.of(4.0, 12.0))
  }
}

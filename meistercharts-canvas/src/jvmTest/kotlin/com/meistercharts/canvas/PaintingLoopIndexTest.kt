package com.meistercharts.canvas

import assertk.*
import assertk.assertions.*
import org.junit.jupiter.api.Test

class PaintingLoopIndexTest {
  @Test
  fun testOverflow() {
    assertThat(PaintingLoopIndex(0).next().value).isEqualTo(1)
    assertThat(PaintingLoopIndex(Integer.MAX_VALUE - 1).next().value).isEqualTo(Integer.MAX_VALUE)

    //Overflow to 0!
    assertThat(PaintingLoopIndex(Integer.MAX_VALUE).next().value).isEqualTo(0)
  }
}

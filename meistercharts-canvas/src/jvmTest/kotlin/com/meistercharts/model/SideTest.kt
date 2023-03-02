package com.meistercharts.model

import assertk.*
import assertk.assertions.*
import org.junit.jupiter.api.Test

class SideTest {
  @Test
  fun testAny() {
    assertThat(Side.Right.any(Side.Right)).isTrue()
    assertThat(Side.Right.any(Side.Left)).isFalse()

    assertThat(Side.Right.any(Side.Right, Side.Left)).isTrue()
    assertThat(Side.Right.any(Side.Right, Side.Bottom)).isTrue()
    assertThat(Side.Right.any(Side.Bottom, Side.Right)).isTrue()
    assertThat(Side.Right.any(Side.Bottom, Side.Right, Side.Left)).isTrue()

    assertThat(Side.Top.any(Side.Bottom, Side.Right, Side.Left)).isFalse()
  }
}

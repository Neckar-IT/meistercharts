package com.cedarsoft.math

import assertk.*
import assertk.assertions.*
import com.meistercharts.animation.Easing
import org.junit.jupiter.api.Test

class EasingTest {
  @Test
  fun testLinear() {
    assertThat(Easing.linear(0.0)).isEqualTo(0.0)
    assertThat(Easing.linear(1.0)).isEqualTo(1.0)
    assertThat(Easing.linear(0.9)).isEqualTo(0.9)
  }

  @Test
  fun testSin() {
    assertThat(Easing.sin(0.0)).isEqualTo(0.0)
    assertThat(Easing.sin(0.5)).isEqualTo(0.479425538604203)
    assertThat(Easing.sin(0.9)).isEqualTo(0.7833269096274834)
    assertThat(Easing.sin(1.0)).isEqualTo(0.8414709848078965)
  }

  @Test
  fun testSmooth() {
    assertThat(Easing.smooth(0.0)).isEqualTo(0.0)
    assertThat(Easing.smooth(1.0)).isEqualTo(1.0)

    assertThat(Easing.smooth(0.1)).isCloseTo(0.028, 0.000000001)
    assertThat(Easing.smooth(0.5)).isEqualTo(0.5)
    assertThat(Easing.smooth(0.9)).isEqualTo(0.972)
  }
}

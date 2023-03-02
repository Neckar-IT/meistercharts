package com.meistercharts.canvas

import assertk.*
import assertk.assertions.*
import org.junit.jupiter.api.Test

internal class LayerSupport2Test {
  @Test
  internal fun testSnapping() {
    val canvas = MockCanvas()
    val chartSupport = ChartSupport(canvas)
    val pixelSnapSupport = chartSupport.pixelSnapSupport

    assertThat(pixelSnapSupport.snapConfiguration.snapX).isFalse()
    assertThat(pixelSnapSupport.snapConfiguration.snapY).isFalse()

    assertThat(pixelSnapSupport.snapXValue(1.1)).isEqualTo(1.1)
    assertThat(pixelSnapSupport.snapYValue(1.1)).isEqualTo(1.1)

    pixelSnapSupport.snapConfiguration = SnapConfiguration.OnlyX
    assertThat(pixelSnapSupport.snapConfiguration.snapX).isTrue()
    assertThat(pixelSnapSupport.snapConfiguration.snapY).isFalse()

    assertThat(pixelSnapSupport.snapXValue(1.1)).isEqualTo(1.0)
    assertThat(pixelSnapSupport.snapYValue(1.1)).isEqualTo(1.1)

    pixelSnapSupport.snapConfiguration = SnapConfiguration.OnlyY
    assertThat(pixelSnapSupport.snapConfiguration.snapX).isFalse()
    assertThat(pixelSnapSupport.snapConfiguration.snapY).isTrue()

    pixelSnapSupport.snapConfiguration = SnapConfiguration.Both
    assertThat(pixelSnapSupport.snapXValue(1.1)).isEqualTo(1.0)
    assertThat(pixelSnapSupport.snapYValue(1.1)).isEqualTo(1.0)
  }
}

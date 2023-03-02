package com.meistercharts.canvas

import assertk.*
import assertk.assertions.*
import org.junit.jupiter.api.Test

/**
 *
 */
class CanvasDirtyTest {
  @Test
  internal fun testDirty() {
    val canvas = MockCanvas()
    val layerSupport = ChartSupport(canvas)

    assertThat(layerSupport.disabled).isFalse()
    assertThat(layerSupport.disabled).isFalse()

    layerSupport.markAsDirty()
  }
}

package com.meistercharts.canvas

import assertk.*
import assertk.assertions.*
import org.junit.jupiter.api.Test

/**
 *
 */
class PixelSnapSupportTest {
  @Test
  fun testInitialValue() {
    val snapSupport = PixelSnapSupport()
    assertThat(snapSupport.snapConfiguration).isEqualTo(SnapConfiguration.None)
  }
}

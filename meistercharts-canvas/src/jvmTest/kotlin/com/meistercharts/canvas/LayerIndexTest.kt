package com.meistercharts.canvas

import assertk.*
import assertk.assertions.*
import org.junit.jupiter.api.Test

/**
 *
 */
class LayerIndexTest {
  @Test
  fun testCompare() {
    assertThat(LayerIndex(1).compareTo(LayerIndex(3))).isEqualTo(-1)
    assertThat(LayerIndex(1).compareTo(3)).isEqualTo(-1)
  }
}

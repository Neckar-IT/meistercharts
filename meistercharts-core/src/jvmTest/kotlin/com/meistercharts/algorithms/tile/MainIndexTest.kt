package com.meistercharts.algorithms.tile

import assertk.*
import assertk.assertions.*
import org.junit.jupiter.api.Test

class MainIndexTest {
  @Test
  fun testIt() {
    assertThat(MainIndex.calculateMainTileIndexPart(17.0)).isEqualTo(0)
    assertThat(MainIndex.calculateMainTileIndexPart(0.0)).isEqualTo(0)
    assertThat(MainIndex.calculateMainTileIndexPart(0.5)).isEqualTo(0)
    assertThat(MainIndex.calculateMainTileIndexPart(-1.0)).isEqualTo(-1)
  }
}

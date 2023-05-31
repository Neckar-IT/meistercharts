package com.meistercharts.algorithms.tile

import assertk.*
import assertk.assertions.*
import org.junit.jupiter.api.Test

class SubIndexTest {
  @Test
  fun testIt() {
    assertThat(SubIndex.calculateSubTileIndexPart(17.0)).isEqualTo(17)
    assertThat(SubIndex.calculateSubTileIndexPart(0.0)).isEqualTo(0)
    assertThat(SubIndex.calculateSubTileIndexPart(0.5)).isEqualTo(0)
    assertThat(SubIndex.calculateSubTileIndexPart(-1.0)).isEqualTo(SubIndex.Max.value)
  }
}

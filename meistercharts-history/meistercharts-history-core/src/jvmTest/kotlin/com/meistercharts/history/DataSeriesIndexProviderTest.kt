package com.meistercharts.history

import assertk.*
import assertk.assertions.*
import org.junit.jupiter.api.Test

class DataSeriesIndexProviderTest {
  private val requestedVisibleIndices: DataSeriesIndexProvider<DecimalDataSeriesIndex> = DecimalDataSeriesIndexProvider.indices { 7 }

  @Test
  fun testAtMost() {
    assertThat(requestedVisibleIndices.size()).isEqualTo(7)
    val atMost = requestedVisibleIndices.atMost { 5 }

    assertThat(atMost.size()).isEqualTo(5)

    var calledWithIndex = -1
    atMost.fastForEachIndexed { index, value ->
      assertThat(index).isEqualTo(calledWithIndex + 1)
      assertThat(value.value).isEqualTo(index)

      calledWithIndex = index
    }

    assertThat(calledWithIndex).isEqualTo(4)
  }
}

package com.meistercharts.canvas

import assertk.*
import assertk.assertions.*
import it.neckar.open.collections.fastForEach
import org.junit.jupiter.api.Test


class SizeClassificationTest {

  @Test
  fun testMinMax() {
    SizeClassification.values().fastForEach {
      assertThat(it.min < it.max)
    }
  }

  @Test
  fun testBreakpoints() {
    var previousSizeClassification: SizeClassification? = null
    SizeClassification.values().fastForEach { sizeClassification ->
      previousSizeClassification?.let {
        assertThat(sizeClassification.min).isEqualTo(it.max)
      }
      previousSizeClassification = sizeClassification
    }
  }
}

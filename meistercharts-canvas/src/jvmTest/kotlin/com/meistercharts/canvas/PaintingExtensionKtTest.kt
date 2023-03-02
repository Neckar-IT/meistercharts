package com.meistercharts.canvas

import assertk.*
import assertk.assertions.*
import com.meistercharts.model.HorizontalAlignment
import org.junit.jupiter.api.Test

/**
 *
 */
class PaintingExtensionKtTest {
  @Test
  fun testLeft() {
    HorizontalAlignment.Left.calculateOffsetXForGap(100.0).let {
      assertThat(it).isEqualTo(100.0)
    }

    HorizontalAlignment.Center.calculateOffsetXForGap(100.0).let {
      assertThat(it).isEqualTo(0.0)
    }

    HorizontalAlignment.Right.calculateOffsetXForGap(100.0).let {
      assertThat(it).isEqualTo(-100.0)
    }
  }
}

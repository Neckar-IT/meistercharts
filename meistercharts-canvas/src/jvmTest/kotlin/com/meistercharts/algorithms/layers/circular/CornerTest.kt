package com.meistercharts.algorithms.layers.circular

import assertk.*
import assertk.assertions.*
import com.meistercharts.model.Corner
import org.junit.jupiter.api.Test

/**
 */
internal class CornerTest {
  @Test
  internal fun testGetStability() {
    Corner.values().forEachIndexed { index, corner ->
      assertThat(Corner.get(index)).isSameAs(corner)
    }
  }
}

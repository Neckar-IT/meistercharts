package com.meistercharts.algorithms

import assertk.*
import assertk.assertions.*
import com.meistercharts.algorithms.axis.AxisSelection
import org.junit.jupiter.api.Test

/**
 */
class AxisSelectionTest {
  @Test
  fun testNegate() {
    AxisSelection.values().forEach {
      val negated = it.negate()

      assertThat(negated.containsX).isNotEqualTo(it.containsX)
      assertThat(negated.containsY).isNotEqualTo(it.containsY)
    }
  }
}

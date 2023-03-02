package com.meistercharts.algorithms

import assertk.assertThat
import assertk.assertions.isEqualTo
import it.neckar.open.kotlin.lang.ifNaN
import it.neckar.open.kotlin.lang.or0ifNaN
import org.junit.jupiter.api.Test
import kotlin.test.fail

internal class ChartingUtilsTest {
  @Test
  internal fun avoidNaN() {
    assertThat(Double.MAX_VALUE.or0ifNaN()).isEqualTo(Double.MAX_VALUE)
    assertThat(Double.MIN_VALUE.or0ifNaN()).isEqualTo(Double.MIN_VALUE)
    assertThat(Double.NEGATIVE_INFINITY.or0ifNaN()).isEqualTo(Double.NEGATIVE_INFINITY)
    assertThat(Double.POSITIVE_INFINITY.or0ifNaN()).isEqualTo(Double.POSITIVE_INFINITY)
    assertThat(Double.NaN.or0ifNaN()).isEqualTo(0.0)
    assertThat(Double.NaN.ifNaN(999.0)).isEqualTo(999.0)
  }

  @Test
  internal fun lineWithin() {
    try {
      ChartingUtils.lineWithin(0.0, 2.0, 1.0, 0.5)
      fail("IllegalArgumentException expected because min > max")
    } catch (e: IllegalArgumentException) {
    }

    // max - min < lineWidth
    assertThat(ChartingUtils.lineWithin(0.0, 1.0, 2.0, 2.0)).isEqualTo(0.0)

    assertThat(ChartingUtils.lineWithin(0.0, -1.0, 1.0, 1.0)).isEqualTo(0.0)
    assertThat(ChartingUtils.lineWithin(0.0, -0.5, 0.5, 1.0)).isEqualTo(0.0)
    assertThat(ChartingUtils.lineWithin(0.0, 0.0, 1.0, 1.0)).isEqualTo(0.5)
    assertThat(ChartingUtils.lineWithin(0.0, 1.0, 2.0, 1.0)).isEqualTo(1.5)
    assertThat(ChartingUtils.lineWithin(0.0, -2.0, -1.0, 1.0)).isEqualTo(-1.5)
  }
}

package com.meistercharts.algorithms

import assertk.*
import assertk.assertions.*
import com.meistercharts.algorithms.axis.LogarithmicAxisTickCalculator
import org.junit.jupiter.api.Test
import kotlin.math.log10
import kotlin.math.pow

/**
 */
class LogarithmicAxisTickCalculatorTest {
  @Test
  fun testIt() {
    val start = 0.1
    val end = 100.0

    LogarithmicAxisTickCalculator.calculateTickValues(start, end, 10).let {
      assertThat(it).containsExactly(0.1, 1.0, 10.0, 100.0)
    }
  }

  @Test
  fun testDebug() {
    val logLower: Double = log10(1000.0)
    val logUpper: Double = log10(10_000.0)

    assertThat(logLower).isEqualTo(3.0)
    assertThat(logUpper).isEqualTo(4.0)

    assertThat(LogarithmicAxisTickCalculator.calculateExponents(logLower, logUpper, 10, 1.0)).containsExactly(3.0, 4.0)
    assertThat(LogarithmicAxisTickCalculator.calculateExponents(logLower, logUpper, 10, 0.0)).containsExactly(3.0, 3.1, 3.2, 3.3, 3.4, 3.5, 3.6, 3.7, 3.8, 3.9, 4.0)
  }

  @Test
  fun testPow() {
    assertThat(10.0.pow(3.0)).isEqualTo(1000.0)
    assertThat(10.0.pow(3.5)).isEqualTo(3162.2776601683795) //this tick does make some sense, but is not nice
  }

  @Test
  fun testLog() {
    assertThat(log10(10.0)).isEqualTo(1.0)
    assertThat(log10(100.0)).isEqualTo(2.0)
    assertThat(log10(1000.0)).isEqualTo(3.0)
    assertThat(log10(10_000.0)).isEqualTo(4.0)
  }
}

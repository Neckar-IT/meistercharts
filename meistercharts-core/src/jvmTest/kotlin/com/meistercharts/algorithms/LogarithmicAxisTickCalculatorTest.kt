/**
 * Copyright 2023 Neckar IT GmbH, Mössingen, Germany
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.meistercharts.algorithms

import assertk.*
import assertk.assertions.*
import com.meistercharts.axis.AxisEndConfiguration
import com.meistercharts.axis.IntermediateValuesMode
import com.meistercharts.axis.LinearAxisTickCalculator
import com.meistercharts.axis.LogarithmicAxisTickCalculator
import org.junit.jupiter.api.Test
import kotlin.math.log10
import kotlin.math.pow

/**
 */
class LogarithmicAxisTickCalculatorTest {

  @Test
  fun testPossibleEdgeCase() {
    val lower = 38.60
    val upper = 10_000_000_000.0

    LogarithmicAxisTickCalculator.calculateTickValues(lower, upper, 9).let {
      assertThat(it).containsExactly(38.599999999999994, 1.0E10)
    }
  }

  @Test
  fun testLogAxisWith2Ticks() {
    // tick distance of exponents: 3.0 --> 4.0
    LinearAxisTickCalculator.calculateTickDistance(1.0, 1.0, 2, intermediateValuesMode = IntermediateValuesMode.Only10).let {
      assertThat(it).isEqualTo(1.0)
    }

    // Linear of exponents: 3.0 -> 4.0
    LinearAxisTickCalculator.calculateTickValues(3.0, 4.0, AxisEndConfiguration.Default, 2, 1.0, intermediateValuesMode = IntermediateValuesMode.Only10).let {
      assertThat(it).containsExactly(3.0, 4.0)
    }

    // Log: 10^3, 10^4
    LogarithmicAxisTickCalculator.calculateTickValues(1000.0, 1_0000.0, 2).let {
      assertThat(it).containsExactly(1000.0, 1_0000.0)
    }
  }

  @Test
  fun testStrangeStartEndValues() {
    LogarithmicAxisTickCalculator.calculateTickValues(3.0, 4.0, 11).let {
      assertThat(it).containsExactly(3.0, 4.0) //no value 10 exp between lower and upper
    }
  }

  @Test
  fun testTicksBugMinMax() {
    LogarithmicAxisTickCalculator.calculateTickValues(0.1, 1.0, 2).let {
      assertThat(it).containsExactly(0.1, 1.0)
    }

    LogarithmicAxisTickCalculator.calculateTickValues(1_0000.0, 10_0000.0, 2).let {
      assertThat(it).containsExactly(1_0000.0, 10_0000.0)
    }
  }

  @Test
  fun `Issue #11`() {
    val lower = 1_0000.0
    val upper = 10_0000.0

    val logLower: Double = log10(lower)
    val logUpper: Double = log10(upper)

    assertThat(logLower).isEqualTo(4.0)
    assertThat(logUpper).isEqualTo(5.0)

    LogarithmicAxisTickCalculator.calculateExponents(logLower, logUpper, 11, 0.0).let {
      assertThat(it).containsExactly(4.0, 4.1, 4.2, 4.3, 4.4, 4.5, 4.6, 4.7, 4.8, 4.9, 5.0)
    }

    LogarithmicAxisTickCalculator.calculateExponents(logLower, logUpper, 10, 1.0).let {
      assertThat(it).containsExactly(4.0, 5.0)
    }

    LogarithmicAxisTickCalculator.calculateTickValues(lower, upper, 10).let {
      assertThat(it).containsExactly(1_0000.0, 10_0000.0)
    }
  }

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

    assertThat(LogarithmicAxisTickCalculator.calculateExponents(logLower, logUpper, 11, 1.0)).containsExactly(3.0, 4.0)
    assertThat(LogarithmicAxisTickCalculator.calculateExponents(logLower, logUpper, 11, 0.0)).containsExactly(3.0, 3.1, 3.2, 3.3, 3.4, 3.5, 3.6, 3.7, 3.8, 3.9, 4.0)
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

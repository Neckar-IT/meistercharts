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
package com.meistercharts.algorithms.layers

import assertk.*
import assertk.assertions.*
import com.meistercharts.axis.AxisEndConfiguration
import com.meistercharts.axis.IntermediateValuesMode
import com.meistercharts.axis.LinearAxisTickCalculator
import com.meistercharts.axis.OffsetTickCalculator
import it.neckar.open.collections.fastForEachIndexed
import it.neckar.open.formatting.decimalFormat
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

@Disabled
class ValueAxisWithOffsetLayerTest {
  @Test
  fun testBigRange() {
    val calculateTickValues = LinearAxisTickCalculator.calculateTickValues(-40_000.0, 140_000.0, maxTickCount = 10, intermediateValuesMode = IntermediateValuesMode.Also5and2)

    verify(
      2, 0, calculateTickValues,
      arrayOf(
        "0",
        "0",
        "0",
        "0",
        "0",
        "0",
        "0",
        "0",
        "0",
        "0",
      ),
    )
    verify(
      3, 0, calculateTickValues,
      arrayOf(
        "0",
        "0",
        "0",
        "0",
        "0",
        "0",
        "0",
        "0",
        "0",
        "0",
      ),
    )
    verify(
      4, 0, calculateTickValues,
      arrayOf(
        "0",
        "0",
        "0",
        "0",
        "0",
        "0",
        "0",
        "0",
        "0",
        "0",
      ),
    )
    verify(
      5, 0, calculateTickValues,
      arrayOf(
        "-40,000",
        "-20,000",
        "0",
        "20,000",
        "40,000",
        "60,000",
        "80,000",
        "0",
        "20,000",
        "40,000",
      ),
    )
    verify(
      6, 0, calculateTickValues,
      arrayOf(
        "-40,000",
        "-20,000",
        "0",
        "20,000",
        "40,000",
        "60,000",
        "80,000",
        "100,000",
        "120,000",
        "140,000",
      ),
    )
  }

  @Test
  fun testSmallRange() {
    val calculateTickValues = LinearAxisTickCalculator.calculateTickValues(147_528.12567, 147_537.57831, maxTickCount = 10, intermediateValuesMode = IntermediateValuesMode.Also5and2)
    val ticksOut = Array(calculateTickValues.size) { "" }

    verify(
      1, 1, calculateTickValues,
      arrayOf(
        "8.1",
        "0.0",
        "1.0",
        "2.0",
        "3.0",
        "4.0",
        "5.0",
        "6.0",
        "7.6",
      ),
    )
    verify(
      2, 1, calculateTickValues,
      arrayOf(
        "28.1",
        "30.0",
        "31.0",
        "32.0",
        "33.0",
        "34.0",
        "35.0",
        "36.0",
        "37.6",
      ),
    )
    verify(
      3, 1, calculateTickValues,
      arrayOf(
        "528.1",
        "530.0",
        "531.0",
        "532.0",
        "533.0",
        "534.0",
        "535.0",
        "536.0",
        "537.6",
      ),
    )
    verify(
      4, 1, calculateTickValues,
      arrayOf(
        "7,528.1",
        "7,530.0",
        "7,531.0",
        "7,532.0",
        "7,533.0",
        "7,534.0",
        "7,535.0",
        "7,536.0",
        "7,537.6",
      ),
    )
    verify(
      5, 1, calculateTickValues,
      arrayOf(
        "47,528.1",
        "47,530.0",
        "47,531.0",
        "47,532.0",
        "47,533.0",
        "47,534.0",
        "47,535.0",
        "47,536.0",
        "47,537.6",
      ),
    )
  }

  @Test
  fun testIt() {
    val calculateTickValues = LinearAxisTickCalculator.calculateTickValues(18_756_000.12567, 18_784_000.57831, maxTickCount = 10, intermediateValuesMode = IntermediateValuesMode.Also5and2, axisEndConfiguration = AxisEndConfiguration.Default)
    val ticksOut = Array(calculateTickValues.size) { "" }

    verify(
      6, 0, calculateTickValues,
      arrayOf(
        "760,000",
      )
    )
  }

  @Test
  fun testThat() {
    assertThat(OffsetTickCalculator.offsetForNumber(value = 231.458, integerDigits = 2)).isEqualTo(200.0) // 200 + 31.458
    assertThat(OffsetTickCalculator.offsetForNumber(value = 1234.765, integerDigits = 3)).isEqualTo(1000.0) // 1000 + 234.765
    assertThat(OffsetTickCalculator.offsetForNumber(value = 11.0, integerDigits = 3)).isEqualTo(0.0)
    assertThat(OffsetTickCalculator.offsetForNumber(value = 11.0, integerDigits = 2)).isEqualTo(0.0)
    assertThat(OffsetTickCalculator.offsetForNumber(value = 11.0, integerDigits = 1)).isEqualTo(10.0)

    assertThat(OffsetTickCalculator.offsetForNumber(value = 17_150_605.0, integerDigits = 1)).isEqualTo(17_150_600.0)
    assertThat(OffsetTickCalculator.offsetForNumber(value = 17_150_605.0, integerDigits = 2)).isEqualTo(17_150_600.0)
    assertThat(OffsetTickCalculator.offsetForNumber(value = 17_150_605.0, integerDigits = 3)).isEqualTo(17_150_000.0)
    assertThat(OffsetTickCalculator.offsetForNumber(value = 17_150_605.0, integerDigits = 4)).isEqualTo(17_150_000.0)
    assertThat(OffsetTickCalculator.offsetForNumber(value = 17_150_605.0, integerDigits = 5)).isEqualTo(17_100_000.0)
    assertThat(OffsetTickCalculator.offsetForNumber(value = 17_150_605.0, integerDigits = 6)).isEqualTo(17_000_000.0)
    assertThat(OffsetTickCalculator.offsetForNumber(value = 17_150_605.0, integerDigits = 7)).isEqualTo(10_000_000.0)

    assertThat(OffsetTickCalculator.offsetForNumber(value = 0.0, integerDigits = 1)).isEqualTo(0.0)
    assertThat(OffsetTickCalculator.offsetForNumber(value = 0.0, integerDigits = 1)).isEqualTo(0.0)
    assertThat(OffsetTickCalculator.offsetForNumber(value = 0.0, integerDigits = 15)).isEqualTo(0.0)
    assertThat(OffsetTickCalculator.offsetForNumber(value = 0.0, integerDigits = 15)).isEqualTo(0.0)

    val delta = 0.00000001

    assertThat(OffsetTickCalculator.offsetForNumber(value = 0.5, integerDigits = 7)).isEqualTo(0.0)
    assertThat(OffsetTickCalculator.offsetForNumber(value = 0.7618155, integerDigits = -1)).isCloseTo(0.7, delta)
    assertThat(OffsetTickCalculator.offsetForNumber(value = 0.7618155, integerDigits = -2)).isCloseTo(0.76, delta)
    assertThat(OffsetTickCalculator.offsetForNumber(value = 0.7618155, integerDigits = -3)).isCloseTo(0.761, delta)
    assertThat(OffsetTickCalculator.offsetForNumber(value = 0.7618155, integerDigits = -4)).isCloseTo(0.7618, delta)
    assertThat(OffsetTickCalculator.offsetForNumber(value = 0.7618155, integerDigits = -5)).isCloseTo(0.76181, delta)

    assertThat(OffsetTickCalculator.offsetForNumber(value = -0.7618155, integerDigits = -1)).isCloseTo(-0.7, delta)
  }


  private fun verify(integerDigits: Int, fractionDigits: Int, tickValues: DoubleArray, expected: Array<String>) {
    val offsetTicksFormat = decimalFormat(integerDigits, fractionDigits)

    tickValues.fastForEachIndexed { index, tickValue ->
      assertThat(offsetTicksFormat.format(tickValue)).isEqualTo(expected[index])
    }
  }
}

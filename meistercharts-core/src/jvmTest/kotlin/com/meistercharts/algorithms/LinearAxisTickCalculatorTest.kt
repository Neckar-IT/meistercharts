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
import com.meistercharts.axis.LinearAxisTickCalculator.calculateTickValues
import org.junit.jupiter.api.Test

/**
 */
class LinearAxisTickCalculatorTest {
  @Test
  fun testPossibleBug() {
    val lower = 1.586587304671755
    val upper = 10.0
    LinearAxisTickCalculator.calculateTickValues(
      lower, upper, AxisEndConfiguration.Default,
      9, 1.0, IntermediateValuesMode.Only10
    ).let {
      assertThat(it).containsExactly(lower, upper)
    }
  }

  @Test
  fun testExactTickCount() {
    LinearAxisTickCalculator.calculateTickValues(
      3.1,
      3.2,
      maxTickCount = 11,
      intermediateValuesMode = IntermediateValuesMode.Only10
    ).let {
      assertThat(it).containsExactly(3.1, 3.11, 3.12, 3.13, 3.14, 3.15, 3.16, 3.17, 3.18, 3.19, 3.2)
    }
  }

  @Test
  fun testVerySmallValues() {
    LinearAxisTickCalculator.calculateTickValues(
      3.1,
      3.2,
      maxTickCount = 10,
      intermediateValuesMode = IntermediateValuesMode.Only10
    ).let {
      assertThat(it).containsExactly(3.1, 3.2)
    }
  }

  @Test
  fun testTickDistance() {
    LinearAxisTickCalculator.calculateTickDistance(
      3.0,
      3.0,
      maxTickCount = 10,
      intermediateValuesMode = IntermediateValuesMode.Only10
    ).let {
      assertThat(it).isEqualTo(1.0)
    }

    LinearAxisTickCalculator.calculateTickDistance(
      3.0,
      3.0,
      maxTickCount = 2,
      intermediateValuesMode = IntermediateValuesMode.Only10
    ).let {
      assertThat(it).isEqualTo(10.0)
    }

    LinearAxisTickCalculator.calculateTickDistance(
      3.0,
      3.0,
      maxTickCount = 3,
      intermediateValuesMode = IntermediateValuesMode.Only10
    ).let {
      assertThat(it).isEqualTo(10.0)
    }

    LinearAxisTickCalculator.calculateTickDistance(
      3.0,
      3.0,
      maxTickCount = 1,
      intermediateValuesMode = IntermediateValuesMode.Only10
    ).let {
      assertThat(it).isEqualTo(10.0)
    }
  }

  @Test
  fun testBugCalculateTickDistance() {
    LinearAxisTickCalculator.calculateTickDistance(
      4.0,
      4.0,
      maxTickCount = 2,
      intermediateValuesMode = IntermediateValuesMode.Only10
    ).let {
      assertThat(it).isEqualTo(10.0)
    }

    LinearAxisTickCalculator.calculateTickDistance(
      1.0,
      1.0,
      maxTickCount = 2,
      intermediateValuesMode = IntermediateValuesMode.Only10
    ).let {
      assertThat(it).isEqualTo(1.0)
    }
  }

  @Test
  fun testIt2() {
    assertThat(
      LinearAxisTickCalculator.calculateTickValues(
        0.0,
        4.0,
        AxisEndConfiguration.Default,
        maxTickCount = 20,
        minTickDistance = 0.0,
        intermediateValuesMode = IntermediateValuesMode.Only10
      )
    ).containsExactly(0.0, 1.0, 2.0, 3.0, 4.0)
  }

  @Test
  fun testBug() {
    assertThat(
      LinearAxisTickCalculator.calculateTickValues(
        0.0,
        4.0,
        AxisEndConfiguration.Default,
        maxTickCount = 2,
        minTickDistance = 1.0,
        intermediateValuesMode = IntermediateValuesMode.Only10
      )
    ).containsExactly(0.0, 4.0) //Special case

    assertThat(
      LinearAxisTickCalculator.calculateTickValues(
        -1.0,
        0.0,
        AxisEndConfiguration.Default,
        maxTickCount = 2,
        minTickDistance = 1.0,
        intermediateValuesMode = IntermediateValuesMode.Only10
      )
    ).containsExactly(-1.0, 0.0) //4.0 is not allowed because of [IntermediateValuesMode.Only10]
  }

  @Test
  fun testFlicker() {
    assertThat(calculateTickValues(0.0, 100.0, AxisEndConfiguration.Default, 20, minTickDistance = 0.01, intermediateValuesMode = IntermediateValuesMode.Also5and2)).hasSize(11)
    //the 99.9999 are rounded to 100.0
    assertThat(calculateTickValues(0.0, 99.99999999999999, AxisEndConfiguration.Default, 20, minTickDistance = 0.01, intermediateValuesMode = IntermediateValuesMode.Also5and2)).hasSize(11)
  }

  @Test
  internal fun testBinary() {
    assertThat(calculateTickValues(0.0, 1.0, AxisEndConfiguration.Default, 16, 0.0, IntermediateValuesMode.Also5and2)).containsExactly(0.0, 0.1, 0.2, 0.30000000000000004, 0.4, 0.5, 0.6000000000000001, 0.7000000000000001, 0.8, 0.9, 1.0)
    assertThat(calculateTickValues(0.0, 1.0, AxisEndConfiguration.Exact, 16, 0.0, IntermediateValuesMode.Also5and2)).containsExactly(0.0, 0.1, 0.2, 0.30000000000000004, 0.4, 0.5, 0.6000000000000001, 0.7000000000000001, 0.8, 0.9, 1.0)
  }

  @Test
  internal fun testMore() {
    assertThat(calculateTickValues(2.914642609299098, 97.08535739070089, AxisEndConfiguration.Default, 11, 0.0, IntermediateValuesMode.Also5and2)).containsExactly(10.0, 20.0, 30.0, 40.0, 50.0, 60.0, 70.0, 80.0, 90.0)
    assertThat(calculateTickValues(2.914642609299098, 97.08535739070089, AxisEndConfiguration.Exact, 11, 0.0, IntermediateValuesMode.Also5and2)).containsExactly(2.914642609299098, 20.0, 30.0, 40.0, 50.0, 60.0, 70.0, 80.0, 97.08535739070089)
    assertThat(calculateTickValues(0.0, 100.0, AxisEndConfiguration.Exact, 11, 0.0, IntermediateValuesMode.Also5and2)).containsExactly(0.0, 10.0, 20.0, 30.0, 40.0, 50.0, 60.0, 70.0, 80.0, 90.0, 100.0)
    assertThat(calculateTickValues(-0.0, 100.0, AxisEndConfiguration.Exact, 11, 0.0, IntermediateValuesMode.Also5and2)).containsExactly(-0.0, 10.0, 20.0, 30.0, 40.0, 50.0, 60.0, 70.0, 80.0, 90.0, 100.0)
  }

  @Test
  fun testBothNegative() {
    assertThat(calculateTickValues(-10.0, -1.0, AxisEndConfiguration.Exact, 10, 0.0, IntermediateValuesMode.Also5and2)).containsOnly(-10.0, -9.0, -8.0, -7.0, -6.0, -5.0, -4.0, -3.0, -2.0, -1.0)
    assertThat(calculateTickValues(-30.0, -20.0, AxisEndConfiguration.Default, 10, 0.0, IntermediateValuesMode.Also5and2)).containsAll(-30.0, -28.0, -26.0, -24.0, -22.0, -20.0)
  }

  @Test
  fun testShowFirstLastTick() {
    assertThat(calculateTickValues(9.0, 101.19, AxisEndConfiguration.Exact, 10, 0.0, IntermediateValuesMode.Also5and2)).containsAll(9.0, 101.19)
    assertThat(calculateTickValues(9.0, 101.19, AxisEndConfiguration.Default, 10, 0.0, IntermediateValuesMode.Also5and2)).containsAll(20.0, 40.0, 60.0, 80.0, 100.0)
  }

  @Test
  fun testMinTickDistance() {
    assertThat(calculateTickValues(1.0, 10.0, AxisEndConfiguration.Default, 10, 1.0, IntermediateValuesMode.Also5and2)).containsExactly(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0)
    assertThat(calculateTickValues(1.0, 10.0, AxisEndConfiguration.Default, 10, 2.0, IntermediateValuesMode.Also5and2)).containsExactly(2.0, 4.0, 6.0, 8.0, 10.0)
    assertThat(calculateTickValues(1.0, 10.0, AxisEndConfiguration.Default, 10, 1.5, IntermediateValuesMode.Also5and2)).containsExactly(1.5, 3.0, 4.5, 6.0, 7.5, 9.0)
    assertThat(calculateTickValues(1.0, 10.0, AxisEndConfiguration.Default, 10, 5.0, IntermediateValuesMode.Also5and2)).containsExactly(5.0, 10.0)
    assertThat(calculateTickValues(-10.0, 10.0, AxisEndConfiguration.Default, 100, 5.0, IntermediateValuesMode.Also5and2)).containsExactly(-10.0, -5.0, 0.0, 5.0, 10.0)
  }

  @Test
  internal fun calculateFirstTick() {
    assertThat(LinearAxisTickCalculator.calculateTickBase(0.0, 10.0)).isEqualTo(0.0)
    assertThat(LinearAxisTickCalculator.calculateTickBase(0.0, 10.0)).isEqualTo(0.0)
    assertThat(LinearAxisTickCalculator.calculateTickBase(0.0, 10.0)).isEqualTo(0.0)
    assertThat(LinearAxisTickCalculator.calculateTickBase(0.0, 10.0)).isEqualTo(0.0)
    assertThat(LinearAxisTickCalculator.calculateTickBase(0.0, 10.0)).isEqualTo(0.0)
    assertThat(LinearAxisTickCalculator.calculateTickBase(0.0, 10.0)).isEqualTo(0.0)
    assertThat(LinearAxisTickCalculator.calculateTickBase(0.0, 10.0)).isEqualTo(0.0)
    assertThat(LinearAxisTickCalculator.calculateTickBase(0.0, 10.0)).isEqualTo(0.0)
    assertThat(LinearAxisTickCalculator.calculateTickBase(0.0, 10.0)).isEqualTo(0.0)

    assertThat(LinearAxisTickCalculator.calculateTickBase(9.0, 10.0)).isEqualTo(10.0)
    assertThat(LinearAxisTickCalculator.calculateTickBase(10.0, 10.0)).isEqualTo(10.0)
    assertThat(LinearAxisTickCalculator.calculateTickBase(11.0, 10.0)).isEqualTo(20.0)
    assertThat(LinearAxisTickCalculator.calculateTickBase(11.0, 10.0)).isEqualTo(20.0)
    assertThat(LinearAxisTickCalculator.calculateTickBase(11.0, 10.0)).isEqualTo(20.0)
  }

  @Test
  internal fun testUpperValue() {
    assertThat(calculateTickValues(0.0, 11.0, AxisEndConfiguration.Exact, 15, 0.0, IntermediateValuesMode.Also5and2)).containsExactly(0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0, 11.0)
    assertThat(calculateTickValues(-0.5, 11.0, AxisEndConfiguration.Exact, 15, 0.0, IntermediateValuesMode.Also5and2)).containsExactly(-0.5, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0, 11.0)
    assertThat(calculateTickValues(0.5, 10.5, AxisEndConfiguration.Exact, 15, 0.0, IntermediateValuesMode.Also5and2)).containsExactly(0.5, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.5)
  }

  @Test
  fun testIntermediate() {
    assertThat(LinearAxisTickCalculator.calculateTickValues(0.0, 10.0, AxisEndConfiguration.Default, 11, 1.0, IntermediateValuesMode.Also5and2)).containsExactly(0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0)
    assertThat(LinearAxisTickCalculator.calculateTickValues(0.0, 10.0, AxisEndConfiguration.Default, 11, 2.0, IntermediateValuesMode.Also5and2)).containsExactly(0.0, 2.0, 4.0, 6.0, 8.0, 10.0)
    assertThat(LinearAxisTickCalculator.calculateTickValues(0.0, 10.0, AxisEndConfiguration.Default, 11, 2.5, IntermediateValuesMode.Also5and2)).containsExactly(0.0, 2.5, 5.0, 7.5, 10.0)

    assertThat(LinearAxisTickCalculator.calculateTickValues(0.0, 10.0, AxisEndConfiguration.Default, 11, 1.0, IntermediateValuesMode.Also5)).containsExactly(0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0)
    assertThat(LinearAxisTickCalculator.calculateTickValues(0.0, 10.0, AxisEndConfiguration.Default, 11, 2.0, IntermediateValuesMode.Also5)).containsExactly(0.0, 2.0, 4.0, 6.0, 8.0, 10.0)
    assertThat(LinearAxisTickCalculator.calculateTickValues(0.0, 10.0, AxisEndConfiguration.Default, 11, 2.5, IntermediateValuesMode.Also5)).containsExactly(0.0, 2.5, 5.0, 7.5, 10.0)

    assertThat(LinearAxisTickCalculator.calculateTickValues(0.0, 10.0, AxisEndConfiguration.Default, 11, 1.0, IntermediateValuesMode.Also2)).containsExactly(0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0)
    assertThat(LinearAxisTickCalculator.calculateTickValues(0.0, 10.0, AxisEndConfiguration.Default, 11, 2.0, IntermediateValuesMode.Also2)).containsExactly(0.0, 2.0, 4.0, 6.0, 8.0, 10.0)
    assertThat(LinearAxisTickCalculator.calculateTickValues(0.0, 10.0, AxisEndConfiguration.Default, 11, 2.5, IntermediateValuesMode.Also2)).containsExactly(0.0, 2.5, 5.0, 7.5, 10.0)

    assertThat(LinearAxisTickCalculator.calculateTickValues(0.0, 10.0, AxisEndConfiguration.Default, 11, 1.0, IntermediateValuesMode.Only10)).containsExactly(0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0)
    assertThat(LinearAxisTickCalculator.calculateTickValues(0.0, 10.0, AxisEndConfiguration.Default, 11, 2.0, IntermediateValuesMode.Only10)).containsExactly(0.0, 2.0, 4.0, 6.0, 8.0, 10.0)
    assertThat(LinearAxisTickCalculator.calculateTickValues(0.0, 10.0, AxisEndConfiguration.Default, 11, 2.5, IntermediateValuesMode.Only10)).containsExactly(0.0, 2.5, 5.0, 7.5, 10.0)


    //Shows the difference
    assertThat(LinearAxisTickCalculator.calculateTickValues(0.0, 35.0, AxisEndConfiguration.Default, 11, 0.0, IntermediateValuesMode.Only10)).containsExactly(0.0, 10.0, 20.0, 30.0)
    assertThat(LinearAxisTickCalculator.calculateTickValues(0.0, 35.0, AxisEndConfiguration.Default, 11, 0.0, IntermediateValuesMode.Also5)).containsExactly(0.0, 5.0, 10.0, 15.0, 20.0, 25.0, 30.0, 35.0)
    assertThat(LinearAxisTickCalculator.calculateTickValues(0.0, 35.0, AxisEndConfiguration.Default, 11, 0.0, IntermediateValuesMode.Also2)).containsExactly(0.0, 10.0, 20.0, 30.0)
    assertThat(LinearAxisTickCalculator.calculateTickValues(0.0, 35.0, AxisEndConfiguration.Default, 11, 0.0, IntermediateValuesMode.Also5and2)).containsExactly(0.0, 5.0, 10.0, 15.0, 20.0, 25.0, 30.0, 35.0)
  }
}

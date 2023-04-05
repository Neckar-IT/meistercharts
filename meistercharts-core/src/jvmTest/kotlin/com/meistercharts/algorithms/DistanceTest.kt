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
import com.meistercharts.algorithms.axis.AxisSelection
import com.meistercharts.model.Distance
import it.neckar.open.kotlin.lang.sqrt
import it.neckar.open.test.utils.isNaN
import org.junit.jupiter.api.Test

internal class DistanceTest {
  @Test
  fun testDirect() {
    assertThat(Distance(0.0, 0.0).direct()).isEqualTo(0.0.sqrt())
    assertThat(Distance(1.0, 1.0).direct()).isEqualTo(2.0.sqrt())
    assertThat(Distance(2.0, 2.0).direct()).isEqualTo(8.0.sqrt())

    assertThat(Distance(3.0, 4.0).direct()).isEqualTo(5.0)
    assertThat(Distance(-3.0, 4.0).direct()).isEqualTo(5.0)
    assertThat(Distance(3.0, -4.0).direct()).isEqualTo(5.0)
    assertThat(Distance(-3.0, -4.0).direct()).isEqualTo(5.0)
  }

  @Test
  fun testNan() {
    assertThat(Distance(2.0, 1.0).plus(Double.NaN, Double.NaN)).isEqualTo(Distance(Double.NaN, Double.NaN))
  }

  @Test
  fun testWithMaxAxisSelection() {
    assertThat(Distance(1.0, 1.0).withMax(0.5, 0.5, AxisSelection.Both)).isEqualTo(Distance(0.5, 0.5))
    assertThat(Distance(1.0, 1.0).withMax(0.5, 0.5, AxisSelection.None)).isEqualTo(Distance(1.0, 1.0))
    assertThat(Distance(1.0, 1.0).withMax(0.5, 0.5, AxisSelection.X)).isEqualTo(Distance(0.5, 1.0))
    assertThat(Distance(1.0, 1.0).withMax(0.5, 0.5, AxisSelection.Y)).isEqualTo(Distance(1.0, 0.5))
  }

  @Test
  fun testWithMinAxisSelection() {
    assertThat(Distance(1.0, 1.0).withMin(9.5, 9.5, AxisSelection.Both)).isEqualTo(Distance(9.5, 9.5))
    assertThat(Distance(1.0, 1.0).withMin(9.5, 9.5, AxisSelection.None)).isEqualTo(Distance(1.0, 1.0))
    assertThat(Distance(1.0, 1.0).withMin(9.5, 9.5, AxisSelection.X)).isEqualTo(Distance(9.5, 1.0))
    assertThat(Distance(1.0, 1.0).withMin(9.5, 9.5, AxisSelection.Y)).isEqualTo(Distance(1.0, 9.5))
  }

  @Test
  internal fun plusDelta() {
    assertThat(Distance(2.0, 1.0).plus(0.0, 0.0)).isEqualTo(Distance(2.0, 1.0))
    assertThat(Distance(2.0, 1.0).plus(1.0, 0.0)).isEqualTo(Distance(3.0, 1.0))
    assertThat(Distance(2.0, 1.0).plus(0.0, 1.0)).isEqualTo(Distance(2.0, 2.0))
    assertThat(Distance(2.0, 1.0).plus(1.0, 1.0)).isEqualTo(Distance(3.0, 2.0))
    assertThat(Distance(2.0, 1.0).plus(-1.3, -5.6)).isEqualTo(Distance(0.7, -4.6))
  }

  @Test
  internal fun withMax() {
    assertThat(Distance(3.3, 4.5).withMax(4.0, 5.0)).isEqualTo(Distance(3.3, 4.5))
    assertThat(Distance(3.3, 4.5).withMax(4.0, 4.0)).isEqualTo(Distance(3.3, 4.0))
    assertThat(Distance(3.3, 4.5).withMax(3.0, 5.0)).isEqualTo(Distance(3.0, 4.5))
    assertThat(Distance(3.3, 4.5).withMax(3.0, 4.0)).isEqualTo(Distance(3.0, 4.0))
  }

  @Test
  internal fun withMin() {
    assertThat(Distance(3.3, 4.5).withMin(4.0, 5.0)).isEqualTo(Distance(4.0, 5.0))
    assertThat(Distance(3.3, 4.5).withMin(4.0, 4.0)).isEqualTo(Distance(4.0, 4.5))
    assertThat(Distance(3.3, 4.5).withMin(3.0, 5.0)).isEqualTo(Distance(3.3, 5.0))
    assertThat(Distance(3.3, 4.5).withMin(3.0, 4.0)).isEqualTo(Distance(3.3, 4.5))
  }

  @Test
  internal fun withX() {
    assertThat(Distance(3.5, -2.7).withX(1.0)).isEqualTo(Distance(1.0, -2.7))
    assertThat(Distance(3.5, -2.7).withX(-1.0)).isEqualTo(Distance(-1.0, -2.7))
    assertThat(Distance(3.5, -2.7).withX(Double.POSITIVE_INFINITY)).isEqualTo(Distance(Double.POSITIVE_INFINITY, -2.7))
    assertThat(Distance(3.5, -2.7).withX(Double.NEGATIVE_INFINITY)).isEqualTo(Distance(Double.NEGATIVE_INFINITY, -2.7))
    assertThat(Distance(3.5, -2.7).withX(Double.MAX_VALUE)).isEqualTo(Distance(Double.MAX_VALUE, -2.7))
    assertThat(Distance(3.5, -2.7).withX(Double.MIN_VALUE)).isEqualTo(Distance(Double.MIN_VALUE, -2.7))
  }

  @Test
  internal fun withY() {
    assertThat(Distance(3.5, -2.7).withY(1.0)).isEqualTo(Distance(3.5, 1.0))
    assertThat(Distance(3.5, -2.7).withY(-1.0)).isEqualTo(Distance(3.5, -1.0))
    assertThat(Distance(3.5, -2.7).withY(Double.POSITIVE_INFINITY)).isEqualTo(Distance(3.5, Double.POSITIVE_INFINITY))
    assertThat(Distance(3.5, -2.7).withY(Double.NEGATIVE_INFINITY)).isEqualTo(Distance(3.5, Double.NEGATIVE_INFINITY))
    assertThat(Distance(3.5, -2.7).withY(Double.MAX_VALUE)).isEqualTo(Distance(3.5, Double.MAX_VALUE))
    assertThat(Distance(3.5, -2.7).withY(Double.MIN_VALUE)).isEqualTo(Distance(3.5, Double.MIN_VALUE))
  }

  @Test
  internal fun testNaN() {
    val added = Distance.zero.plus(Double.NaN, Double.NaN)
    assertThat(added.x).isNaN()
    assertThat(added.y).isNaN()
  }

  @Test
  fun `coerceXWithin should return a new Distance with x value within given range`() {
    val result = Distance(5.0, 2.0).coerceXWithin(3.0, 7.0)
    assertThat(result).isEqualTo(Distance(5.0, 2.0))
  }

  @Test
  fun `coerceXWithin should return a new Distance with x value equal to min when x is less than min`() {
    val result = Distance(1.0, 2.0).coerceXWithin(3.0, 7.0)
    assertThat(result).isEqualTo(Distance(3.0, 2.0))
  }

  @Test
  fun `coerceXWithin should return a new Distance with x value equal to max when x is greater than max`() {
    val result = Distance(9.0, 2.0).coerceXWithin(3.0, 7.0)
    assertThat(result).isEqualTo(Distance(7.0, 2.0))
  }

  @Test
  fun `coerceYWithin should return a new Distance with y value within given range`() {
    val result = Distance(5.0, 4.0).coerceYWithin(3.0, 7.0)
    assertThat(result).isEqualTo(Distance(5.0, 4.0))
  }

  @Test
  fun `coerceYWithin should return a new Distance with y value equal to min when y is less than min`() {
    val result = Distance(5.0, 1.0).coerceYWithin(3.0, 7.0)
    assertThat(result).isEqualTo(Distance(5.0, 3.0))
  }
}

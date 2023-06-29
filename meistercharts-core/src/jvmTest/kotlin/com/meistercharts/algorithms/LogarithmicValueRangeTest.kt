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
import com.meistercharts.model.ValueRange
import org.junit.jupiter.api.Test

/**
 *
 */
class LogarithmicValueRangeTest {
  @Test
  fun testAdditionalProperties() {
    val valueRange = ValueRange.logarithmic(0.1, 100.0)

    assertThat(valueRange.start).isEqualTo(0.1)
    assertThat(valueRange.logStart).isEqualTo(-1.0)
    assertThat(valueRange.end).isEqualTo(100.0)
    assertThat(valueRange.logEnd).isEqualTo(2.0)
  }

  @Test
  fun testDoDomainRelative() {
    val valueRange = ValueRange.logarithmic(0.1, 100.0)

    assertThat(valueRange.start).isEqualTo(0.1)
    assertThat(valueRange.end).isEqualTo(100.0)

    assertThat(valueRange.toDomainRelative(0.1)).isEqualTo(0.0)
    assertThat(valueRange.toDomainRelative(100.0)).isEqualTo(1.0)

    assertThat(valueRange.toDomainRelative(50.0)).isEqualTo(0.8996566681120063)
  }

  @Test
  fun test2Domain() {
    val valueRange = ValueRange.logarithmic(0.1, 100.0)

    assertThat(valueRange.start).isEqualTo(0.1)
    assertThat(valueRange.end).isEqualTo(100.0)

    assertThat(valueRange.toDomain(0.0)).isEqualTo(0.1)
    assertThat(valueRange.toDomain(1.0)).isEqualTo(100.0)

    assertThat(valueRange.toDomain(0.8996566681120063)).isCloseTo(50.0, 0.00001)
  }

  @Test
  fun testTicksCalculator() {
    val valueRange = ValueRange.logarithmic(0.1, 100_000.0)
    LinearAxisTickCalculator.calculateTickValues(valueRange.logStart, valueRange.logEnd, AxisEndConfiguration.Default, 10, intermediateValuesMode = IntermediateValuesMode.Only10).let { ticks ->
      assertThat(ticks).hasSize(7)
      assertThat(ticks).containsExactly(-1.0, 0.0, 1.0, 2.0, 3.0, 4.0, 5.0)
    }
  }
}

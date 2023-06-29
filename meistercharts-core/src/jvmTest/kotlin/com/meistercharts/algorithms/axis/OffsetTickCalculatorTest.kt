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
package com.meistercharts.algorithms.axis

import assertk.*
import assertk.assertions.*
import com.meistercharts.axis.OffsetTickCalculator
import com.meistercharts.model.ValueRange
import it.neckar.open.kotlin.lang.ceil
import it.neckar.open.kotlin.lang.floor
import org.junit.jupiter.api.Test
import kotlin.math.pow


class OffsetTickCalculatorTest {
  @Test
  fun `test offsetForNumber`() {
    assertThat(OffsetTickCalculator.offsetForNumber(100.0, -1)).isEqualTo(100.0)
    assertThat(OffsetTickCalculator.offsetForNumber(100.0, 0)).isEqualTo(100.0)
    assertThat(OffsetTickCalculator.offsetForNumber(100.0, 1)).isEqualTo(100.0)
    assertThat(OffsetTickCalculator.offsetForNumber(100.0, 2)).isEqualTo(100.0)

    assertThat(OffsetTickCalculator.offsetForNumber(100.0, 3)).isEqualTo(0.0)
    assertThat(OffsetTickCalculator.offsetForNumber(100.0, 4)).isEqualTo(0.0)

    assertThat(OffsetTickCalculator.offsetForNumber(0.0, 1)).isEqualTo(0.0)
    assertThat(OffsetTickCalculator.offsetForNumber(0.0, 2)).isEqualTo(0.0)
    assertThat(OffsetTickCalculator.offsetForNumber(0.0, 3)).isEqualTo(0.0)
    assertThat(OffsetTickCalculator.offsetForNumber(0.0, 4)).isEqualTo(0.0)

    assertThat(OffsetTickCalculator.offsetForNumber(1.0, 4)).isEqualTo(0.0)
    assertThat(OffsetTickCalculator.offsetForNumber(1.0, 3)).isEqualTo(0.0)
    assertThat(OffsetTickCalculator.offsetForNumber(1.0, 2)).isEqualTo(0.0)
    assertThat(OffsetTickCalculator.offsetForNumber(1.0, 1)).isEqualTo(0.0)
    assertThat(OffsetTickCalculator.offsetForNumber(1.0, 0)).isEqualTo(1.0)
    assertThat(OffsetTickCalculator.offsetForNumber(1.0, -1)).isEqualTo(1.0)
    assertThat(OffsetTickCalculator.offsetForNumber(1.0, -2)).isEqualTo(1.0)
  }

  @Test
  fun basicWithPlus() {
    /**
     * 1 offset border visible:
     * 17_000_000 - 17_001_000
     *
     * Precision: 3 decimal places
     *
     * Ticks: 3 ints + 3 decimals
     */

    //The currently visible value range
    val visibleValueRange = ValueRange.linear(17_000_992.992, 17_001_000.012)
    val exponentForTicks = 3

    OffsetTickCalculator.calculateOffsets(visibleValueRange, exponentForTicks).let {
      assertThat(it).containsExactly(
        17_000_000.0,
        17_001_000.0,
      )
    }

    //Calculate the offsets!
    assertThat(10.0.pow(exponentForTicks)).isEqualTo(1_000.0)
    assertThat((visibleValueRange.start / 1_000.0).floor()).isEqualTo(17_000.0)
    assertThat((visibleValueRange.start / 1_000.0).ceil()).isEqualTo(17_001.0)

    assertThat((visibleValueRange.end / 1_000.0).floor()).isEqualTo(17_001.0)
    assertThat((visibleValueRange.end / 1_000.0).ceil()).isEqualTo(17_002.0)

    //Calculate the precision

    assertThat((visibleValueRange.start % 1_000.0)).isCloseTo(992.992, 0.000001)
    assertThat((visibleValueRange.end % 1_000.0)).isCloseTo(0.012, 0.000001)

    assertThat(OffsetTickCalculator.calculateTickValueForOffset(visibleValueRange.start, exponentForTicks)).isCloseTo(992.992, 0.000001)
    assertThat(OffsetTickCalculator.calculateTickValueForOffset(visibleValueRange.end, exponentForTicks)).isCloseTo(0.012, 0.000001)
  }

  @Test
  fun veryLargeRange() {
    /**
     * Precision: 3 decimal places
     *
     * Ticks: 3 ints + 3 decimals
     */

    //The currently visible value range
    val visibleValueRange = ValueRange.linear(17_000_992.992, 99_001_000.012)
    val exponentForTicks = 3

    OffsetTickCalculator.calculateOffsets(visibleValueRange, exponentForTicks).let {
      assertThat(it).containsExactly(
        17_000_000.0,
        99_001_000.0,
      )
    }

    //Calculate the offsets!
    assertThat(10.0.pow(exponentForTicks)).isEqualTo(1_000.0)
    assertThat((visibleValueRange.start / 1_000.0).floor()).isEqualTo(17_000.0)
    assertThat((visibleValueRange.start / 1_000.0).ceil()).isEqualTo(17_001.0)

    assertThat((visibleValueRange.end / 1_000.0).floor()).isEqualTo(99_001.0)
    assertThat((visibleValueRange.end / 1_000.0).ceil()).isEqualTo(99_002.0)

    //Calculate the precision

    assertThat((visibleValueRange.start % 1_000.0)).isCloseTo(992.992, 0.000001)
    assertThat((visibleValueRange.end % 1_000.0)).isCloseTo(0.012, 0.000001)

    assertThat(OffsetTickCalculator.calculateTickValueForOffset(visibleValueRange.start, exponentForTicks)).isCloseTo(992.992, 0.000001)
    assertThat(OffsetTickCalculator.calculateTickValueForOffset(visibleValueRange.end, exponentForTicks)).isCloseTo(0.012, 0.000001)
  }

}

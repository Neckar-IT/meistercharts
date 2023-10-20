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
import it.neckar.open.collections.fastForEach
import it.neckar.open.formatting.format
import it.neckar.open.kotlin.lang.fastFor
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

class LinearAxisCalculationExpectationsTest {
  @Disabled
  @Test
  fun testIteration() {
    AxisEndConfiguration.entries.fastForEach { axisEndConfiguration ->
      IntermediateValuesMode.entries.fastForEach { intermediateValuesMode ->

        println("------------------------------------------------------")
        println("$axisEndConfiguration - $intermediateValuesMode")

        12.fastFor { maxTickCount ->
          println("max $maxTickCount ticks")

          LinearAxisTickCalculator.calculateTickValues(
            1.0,
            4.0,
            axisEndConfiguration,
            maxTickCount = maxTickCount,
            minTickDistance = 0.0,
            intermediateValuesMode = intermediateValuesMode
          ).let {
            println(it.joinToString(" ") { it.format(2) })
          }
        }

      }
    }
  }

  @Test
  fun testMore() {
    LinearAxisTickCalculator.calculateTickValues(0.0, 10.0, AxisEndConfiguration.Default, 99, 0.0, intermediateValuesMode = IntermediateValuesMode.Only10).let {
      assertThat(it).containsExactly(0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0)
    }
    LinearAxisTickCalculator.calculateTickValues(0.0, 4.0, AxisEndConfiguration.Default, 2, 0.0, intermediateValuesMode = IntermediateValuesMode.Only10).let {
      assertThat(it).containsExactly(0.0, 4.0)
    }
  }

  @Test
  fun testExpectations0_10() {
    assertThat(
      LinearAxisTickCalculator.calculateTickValues(
        0.0,
        10.0,
        AxisEndConfiguration.Default,
        maxTickCount = 20,
        minTickDistance = 0.0,
        intermediateValuesMode = IntermediateValuesMode.Only10
      )
    ).containsExactly(0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0)

    assertThat(
      LinearAxisTickCalculator.calculateTickValues(
        0.0,
        10.0,
        AxisEndConfiguration.Default,
        maxTickCount = 4,
        minTickDistance = 0.0,
        intermediateValuesMode = IntermediateValuesMode.Only10
      )
    ).containsExactly(0.0, 10.0)

    assertThat(
      LinearAxisTickCalculator.calculateTickValues(
        0.0,
        10.0,
        AxisEndConfiguration.Default,
        maxTickCount = 3,
        minTickDistance = 0.0,
        intermediateValuesMode = IntermediateValuesMode.Only10
      )
    ).containsExactly(0.0, 10.0)

    assertThat(
      LinearAxisTickCalculator.calculateTickValues(
        0.0,
        10.0,
        AxisEndConfiguration.Default,
        maxTickCount = 2,
        minTickDistance = 0.0,
        intermediateValuesMode = IntermediateValuesMode.Only10
      )
    ).containsExactly(0.0, 10.0)

    assertThat(
      LinearAxisTickCalculator.calculateTickValues(
        0.0,
        10.0,
        AxisEndConfiguration.Default,
        maxTickCount = 1,
        minTickDistance = 0.0,
        intermediateValuesMode = IntermediateValuesMode.Only10
      )
    ).containsExactly(0.0)
  }

  @Test
  fun testExpectations1_4() {
    assertThat(
      LinearAxisTickCalculator.calculateTickValues(
        1.0,
        4.0,
        AxisEndConfiguration.Default,
        maxTickCount = 20,
        minTickDistance = 0.0,
        intermediateValuesMode = IntermediateValuesMode.Only10
      )
    ).containsExactly(1.0, 2.0, 3.0, 4.0)

    assertThat(
      LinearAxisTickCalculator.calculateTickValues(
        1.0,
        4.0,
        AxisEndConfiguration.Default,
        maxTickCount = 4,
        minTickDistance = 0.0,
        intermediateValuesMode = IntermediateValuesMode.Only10
      )
    ).containsExactly(1.0, 2.0, 3.0, 4.0)

    assertThat(
      LinearAxisTickCalculator.calculateTickValues(
        1.0,
        4.0,
        AxisEndConfiguration.Default,
        maxTickCount = 3,
        minTickDistance = 0.0,
        intermediateValuesMode = IntermediateValuesMode.Only10
      )
    ).containsExactly(1.0, 4.0)

    assertThat(
      LinearAxisTickCalculator.calculateTickValues(
        1.0,
        4.0,
        AxisEndConfiguration.Default,
        maxTickCount = 2,
        minTickDistance = 0.0,
        intermediateValuesMode = IntermediateValuesMode.Only10
      )
    ).containsExactly(1.0, 4.0) //potential candidates: 0.0 and 10.0

    assertThat(
      LinearAxisTickCalculator.calculateTickValues(
        1.0,
        4.0,
        AxisEndConfiguration.Default,
        maxTickCount = 1,
        minTickDistance = 0.0,
        intermediateValuesMode = IntermediateValuesMode.Only10
      )
    ).containsExactly(1.0) //potential candidates: 0.0 and 10.0
  }
}

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
package com.meistercharts.demo

import assertk.*
import assertk.assertions.*
import com.meistercharts.algorithms.ValueRange
import com.meistercharts.animation.Easing
import it.neckar.open.kotlin.lang.random
import it.neckar.open.test.utils.RandomWithSeed
import org.junit.jupiter.api.Test

class TimeBasedValueGeneratorBuilderTest {

  @RandomWithSeed(123)
  @Test
  fun testVerifyStable() {
    val generator = TimeBasedValueGeneratorBuilder {
      val dataSeriesValueRange = ValueRange.percentage
      startValue = dataSeriesValueRange.center() + (random.nextDouble() - 0.5).coerceAtMost(0.2).coerceAtLeast(-0.2) * dataSeriesValueRange.delta
      minDeviation = dataSeriesValueRange.delta * (0.025 * (1)).coerceAtMost(0.25)
      maxDeviation = (dataSeriesValueRange.delta * (0.05 * (1)).coerceAtMost(0.25)).coerceAtLeast(minDeviation * 1.001)
      period = 2_000.0 * (1)
      valueRange = dataSeriesValueRange.reduced(0.25)
      easing = Easing.inOut
    }.build()

    assertThat(generator.generate(123123.0)).isEqualTo(0.3)
    assertThat(generator.generate(123123.0)).isEqualTo(0.3)
    assertThat(generator.generate(1231_4_3.0)).isEqualTo(0.2999998578489613)
    assertThat(generator.generate(123123.0)).isEqualTo(0.3)
  }
}

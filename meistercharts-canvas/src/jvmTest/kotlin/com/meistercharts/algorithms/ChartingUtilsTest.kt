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

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.meistercharts.calc.ChartingUtils
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

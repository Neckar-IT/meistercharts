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
package com.meistercharts.algorithms.time

import assertk.*
import assertk.assertions.*
import com.meistercharts.algorithms.time.DataPointFilter.DomainValueExtractor
import org.junit.jupiter.api.Test

/**
 */
internal class DataPointFilterTest {
  @Test
  internal fun testBasics() {
    val dataPoint = DataPoint(123.0, doubleArrayOf(1.0, 2.0))
    val dataPoint1 = DataPoint(123.0, doubleArrayOf(4.0, 5.0))

    val filter = DataPointFilter(0.0, 1000.0, 0.0, 100.0, DomainValueExtractor<DoubleArray> { it.value[0] })

    assertThat(filter.findRelevantForPaint(listOf(dataPoint))).hasSize(1)
    assertThat(filter.findRelevantForPaint(listOf(dataPoint, dataPoint1))).hasSize(2)
  }
}

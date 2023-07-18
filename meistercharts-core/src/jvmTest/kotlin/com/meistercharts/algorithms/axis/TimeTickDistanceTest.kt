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

import assertk.assertThat
import assertk.assertions.*
import com.meistercharts.axis.DistanceDays
import com.meistercharts.axis.DistanceMillis
import com.meistercharts.axis.DistanceMonths
import com.meistercharts.axis.DistanceYears
import org.junit.jupiter.api.Test

class TimeTickDistanceTest {
  @Test
  fun testMillis() {
    assertThat(DistanceMonths(1).dateTimeSpan.months).isEqualTo(1)
    assertThat(DistanceMonths(1).dateTimeSpan.days).isEqualTo(0)
  }

  @Test
  fun testOffset2ticks() {
    DistanceYears(1).smallestPossibleTickDistance().let {
      assertThat(it).isInstanceOf(DistanceDays::class)
    }
    DistanceMonths(1).smallestPossibleTickDistance().let {
      assertThat(it).isInstanceOf(DistanceDays::class)
    }
  }

  @Test
  fun testCoerceAtLeast() {
    DistanceMonths(1).coerceAtLeast(DistanceMillis(123.0)).let {
      assertThat(it).isInstanceOf(DistanceMonths::class)
    }
  }
}

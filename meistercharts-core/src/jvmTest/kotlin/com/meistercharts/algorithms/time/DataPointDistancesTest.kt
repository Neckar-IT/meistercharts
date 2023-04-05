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
import org.junit.jupiter.api.Test


class DataPointDistancesTest {
  @Test
  fun testOrder() {
    val values = DataPointDistances.entries
    for (i in 1 until values.size) {
      assertThat(values[i - 1].distance).isLessThan(values[i].distance)
    }
  }

  @Test
  fun testNextGreaterDistance() {
    val values = DataPointDistances.entries
    assertThat(values.last().getNextGreaterDistance()).isNull()
    for (i in 0 until values.size - 1) {
      assertThat(values[i].getNextGreaterDistance()).isEqualTo(values[i + 1])
    }
  }

  @Test
  fun testPreviousSmallerDistance() {
    val values = DataPointDistances.entries
    assertThat(values.first().getPreviousSmallerDistance()).isNull()
    for (i in 1 until values.size) {
      assertThat(values[i].getPreviousSmallerDistance()).isEqualTo(values[i - 1])
    }
  }

  @Test
  fun testGreatestDistance() {
    assertThat(DataPointDistances.greatestDistance.getNextGreaterDistance()).isNull()
  }

  @Test
  fun testSmallestDistance() {
    assertThat(DataPointDistances.smallestDistance.getPreviousSmallerDistance()).isNull()
  }

  @Test
  fun testValuesAscending() {
    val values = DataPointDistances.valuesAscending
    for (i in 1 until values.size) {
      assertThat(values[i - 1].distance).isLessThan(values[i].distance)
    }
  }

  @Test
  fun testValuesDescending() {
    val values = DataPointDistances.valuesDescending
    for (i in 1 until values.size) {
      assertThat(values[i - 1].distance).isGreaterThan(values[i].distance)
    }
  }
}

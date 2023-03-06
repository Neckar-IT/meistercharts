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
package com.meistercharts.algorithms.layout

import org.assertj.core.api.Java6Assertions.assertThat
import org.junit.jupiter.api.Test

class LayouterTest {
  @Test
  fun testCalculateCenter() {
    val layouter = Layouter()
    val sizes = doubleArrayOf(10.0, 20.0, 10.0)

    layouter.calculateCenters(sizes).let {
      assertThat(it).hasSize(3)
      assertThat(it).containsExactly(5.0, 20.0, 35.0)
    }

    layouter.calculateCenters(sizes, 2.0).let {
      assertThat(it).hasSize(3)
      assertThat(it).containsExactly(5.0, 20.0 + 2.0, 35.0 + 2.0 * 2)
    }
  }

  @Test
  fun testCalculateCenter2() {
    val layouter = Layouter()
    val sizes = doubleArrayOf(10.0, 20.0, 10.0, 12.0)

    layouter.calculateCenters(sizes).let {
      assertThat(it).hasSize(4)
      assertThat(it).containsExactly(5.0, 20.0, 35.0, 46.0)
    }

    layouter.calculateCenters(sizes, 2.0).let {
      assertThat(it).hasSize(4)
      assertThat(it).containsExactly(5.0, 20.0 + 2.0, 35.0 + 2.0 * 2, 46.0 + 2.0 * 3)
    }
  }

  @Test
  internal fun testfindMinLargestDistanceBetweenCenters() {
    assertThat(doubleArrayOf(10.0, 20.0, 10.0).findLargestDistanceBetweenCenters()).isEqualTo(15.0)
    assertThat(doubleArrayOf(10.0, 20.0, 10.0, 5.0, 20.0).findLargestDistanceBetweenCenters()).isEqualTo(15.0)
    assertThat(doubleArrayOf(10.0, 20.0, 10.0, 5.0, 28.0).findLargestDistanceBetweenCenters()).isEqualTo(16.5)
    assertThat(doubleArrayOf(10.0, 20.0, 20.0, 5.0, 30.0).findLargestDistanceBetweenCenters()).isEqualTo(20.0)
  }

  @Test
  fun testEquidistanceSimple() {
    val layouter = Layouter()
    val sizes = doubleArrayOf(10.0, 20.0, 10.0)

    layouter.calculateEquidistantCenters(sizes).let {
      assertThat(it).hasSize(3)
      assertThat(it).containsExactly(5.0, 20.0, 35.0)
    }
  }
}

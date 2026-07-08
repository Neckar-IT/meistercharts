/*
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
package com.meistercharts.canvas.layout.buffer

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import org.junit.jupiter.api.Test

class CoordinatesArrayListTest {
  @Test
  fun testAddAndAccess() {
    val coordinates = CoordinatesArrayList()
    assertThat(coordinates.isEmpty()).isTrue()

    coordinates.add(1.0, 2.0)
    coordinates.add(3.0, 4.0)

    assertThat(coordinates.isEmpty()).isFalse()
    assertThat(coordinates.size).isEqualTo(2)
    assertThat(coordinates.xAt(0)).isEqualTo(1.0)
    assertThat(coordinates.yAt(0)).isEqualTo(2.0)
    assertThat(coordinates.xAt(1)).isEqualTo(3.0)
    assertThat(coordinates.yAt(1)).isEqualTo(4.0)
    assertThat(coordinates.lastX()).isEqualTo(3.0)
    assertThat(coordinates.lastY()).isEqualTo(4.0)
  }

  @Test
  fun testClear() {
    val coordinates = CoordinatesArrayList()
    coordinates.add(1.0, 2.0)

    coordinates.clear()

    assertThat(coordinates.size).isEqualTo(0)
    assertThat(coordinates.isEmpty()).isTrue()

    //usable again after clear
    coordinates.add(5.0, 6.0)
    assertThat(coordinates.xAt(0)).isEqualTo(5.0)
    assertThat(coordinates.yAt(0)).isEqualTo(6.0)
  }

  @Test
  fun testToDoubleArrays() {
    val coordinates = CoordinatesArrayList()
    coordinates.add(1.0, 2.0)
    coordinates.add(3.0, 4.0)

    //exactly sized - no capacity slack
    assertThat(coordinates.toXDoubleArray()).isEqualTo(doubleArrayOf(1.0, 3.0))
    assertThat(coordinates.toYDoubleArray()).isEqualTo(doubleArrayOf(2.0, 4.0))
  }

  @Test
  fun testFastForEachIndexed() {
    val coordinates = CoordinatesArrayList()
    coordinates.add(1.0, 2.0)
    coordinates.add(3.0, 4.0)

    val collected = mutableListOf<String>()
    coordinates.fastForEachIndexed { index, x, y ->
      collected.add("$index:$x/$y")
    }
    assertThat(collected).isEqualTo(mutableListOf("0:1.0/2.0", "1:3.0/4.0"))
  }
}

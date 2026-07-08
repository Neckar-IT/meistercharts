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
import assertk.assertions.isTrue
import org.junit.jupiter.api.Test

class AreaBandArrayListTest {
  @Test
  fun testAddAndAccess() {
    val bandPoints = AreaBandArrayList()
    assertThat(bandPoints.isEmpty()).isTrue()

    bandPoints.add(1.0, 2.0, 3.0)
    bandPoints.add(4.0, 5.0, 6.0)

    assertThat(bandPoints.size).isEqualTo(2)
    assertThat(bandPoints.xAt(0)).isEqualTo(1.0)
    assertThat(bandPoints.y1At(0)).isEqualTo(2.0)
    assertThat(bandPoints.y2At(0)).isEqualTo(3.0)
    assertThat(bandPoints.xAt(1)).isEqualTo(4.0)
    assertThat(bandPoints.y1At(1)).isEqualTo(5.0)
    assertThat(bandPoints.y2At(1)).isEqualTo(6.0)
  }

  @Test
  fun testClear() {
    val bandPoints = AreaBandArrayList()
    bandPoints.add(1.0, 2.0, 3.0)

    bandPoints.clear()

    assertThat(bandPoints.size).isEqualTo(0)
    assertThat(bandPoints.isEmpty()).isTrue()

    //usable again after clear
    bandPoints.add(7.0, 8.0, 9.0)
    assertThat(bandPoints.xAt(0)).isEqualTo(7.0)
  }

  @Test
  fun testIteration() {
    val bandPoints = AreaBandArrayList()
    bandPoints.add(1.0, 2.0, 3.0)
    bandPoints.add(4.0, 5.0, 6.0)

    val forward = mutableListOf<String>()
    bandPoints.fastForEachIndexed { index, x, y1, y2 ->
      forward.add("$index:$x/$y1/$y2")
    }
    assertThat(forward).isEqualTo(mutableListOf("0:1.0/2.0/3.0", "1:4.0/5.0/6.0"))

    val reversed = mutableListOf<String>()
    bandPoints.fastForEachIndexedReversed { index, x, y1, y2 ->
      reversed.add("$index:$x/$y1/$y2")
    }
    assertThat(reversed).isEqualTo(mutableListOf("1:4.0/5.0/6.0", "0:1.0/2.0/3.0"))
  }
}

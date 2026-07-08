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

import assertk.*
import assertk.assertions.*
import it.neckar.open.test.utils.isNaN
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class CoordinatesBufferTest {

  private lateinit var coordinatesMultiBuffer: CoordinatesMultiBuffer

  @BeforeEach
  fun setUp() {
    coordinatesMultiBuffer = CoordinatesMultiBuffer()
  }

  @Test
  fun `check size after initialization`() {
    assertThat(coordinatesMultiBuffer.size).isEqualTo(0)
  }

  @Test
  fun `ensure size`() {
    coordinatesMultiBuffer.prepare(5)
    assertThat(coordinatesMultiBuffer.size).isEqualTo(5)
  }

  @Test
  fun `set and get x and y coordinates`() {
    coordinatesMultiBuffer.prepare(1)
    assertThat(coordinatesMultiBuffer.size).isEqualTo(1)
    coordinatesMultiBuffer.set(0, 2.0, 3.0)
    assertThat(coordinatesMultiBuffer.x(0)).isEqualTo(2.0)
    assertThat(coordinatesMultiBuffer.y(0)).isEqualTo(3.0)
  }

  @Test
  fun `add coordinates and check size and values`() {
    assertThat(coordinatesMultiBuffer.size).isEqualTo(0)
    coordinatesMultiBuffer.add(5.0, 6.0)
    assertThat(coordinatesMultiBuffer.size).isEqualTo(1)
    assertThat(coordinatesMultiBuffer.size).isEqualTo(1)
    assertThat(coordinatesMultiBuffer.x(0)).isEqualTo(5.0)
    assertThat(coordinatesMultiBuffer.y(0)).isEqualTo(6.0)
  }

  @Test
  fun `coordinates provider returns correct values`() {
    coordinatesMultiBuffer.add(7.0, 8.0)
    val provider = coordinatesMultiBuffer.asCoordinatesProvider()
    assertThat(provider).isNotNull()
    assertThat(provider.size()).isEqualTo(1)
    assertThat(provider.xAt(0)).isEqualTo(7.0)
    assertThat(provider.yAt(0)).isEqualTo(8.0)
  }

  @Test
  fun `add two pairs at once`() {
    coordinatesMultiBuffer.add(1.0, 2.0)
    coordinatesMultiBuffer.add(3.0, 4.0, 5.0, 6.0)

    assertThat(coordinatesMultiBuffer.size).isEqualTo(3)
    assertThat(coordinatesMultiBuffer.x(1)).isEqualTo(3.0)
    assertThat(coordinatesMultiBuffer.y(1)).isEqualTo(4.0)
    assertThat(coordinatesMultiBuffer.x(2)).isEqualTo(5.0)
    assertThat(coordinatesMultiBuffer.y(2)).isEqualTo(6.0)
  }

  @Test
  fun `add three pairs at once`() {
    coordinatesMultiBuffer.add(1.0, 2.0, 3.0, 4.0, 5.0, 6.0)

    assertThat(coordinatesMultiBuffer.size).isEqualTo(3)
    assertThat(coordinatesMultiBuffer.x(0)).isEqualTo(1.0)
    assertThat(coordinatesMultiBuffer.y(0)).isEqualTo(2.0)
    assertThat(coordinatesMultiBuffer.x(1)).isEqualTo(3.0)
    assertThat(coordinatesMultiBuffer.y(1)).isEqualTo(4.0)
    assertThat(coordinatesMultiBuffer.x(2)).isEqualTo(5.0)
    assertThat(coordinatesMultiBuffer.y(2)).isEqualTo(6.0)
  }

  @Test
  fun `last values or NaN`() {
    assertThat(coordinatesMultiBuffer.lastXOrNaN()).isNaN()
    assertThat(coordinatesMultiBuffer.lastYOrNaN()).isNaN()

    coordinatesMultiBuffer.add(1.0, 2.0)
    coordinatesMultiBuffer.add(3.0, 4.0)

    assertThat(coordinatesMultiBuffer.lastXOrNaN()).isEqualTo(3.0)
    assertThat(coordinatesMultiBuffer.lastYOrNaN()).isEqualTo(4.0)
  }

  @Test
  fun testMoreAdd() {
    assertThat(coordinatesMultiBuffer.size).isEqualTo(0)
    coordinatesMultiBuffer.add(1.0, 2.0)
    assertThat(coordinatesMultiBuffer.size).isEqualTo(1)

    assertThat(coordinatesMultiBuffer.x(0)).isEqualTo(1.0)
    assertThat(coordinatesMultiBuffer.y(0)).isEqualTo(2.0)

    coordinatesMultiBuffer.add(3.0, 4.0)
    assertThat(coordinatesMultiBuffer.size).isEqualTo(2)

    assertThat(coordinatesMultiBuffer.x(0)).isEqualTo(1.0)
    assertThat(coordinatesMultiBuffer.y(0)).isEqualTo(2.0)

    assertThat(coordinatesMultiBuffer.x(1)).isEqualTo(3.0)
    assertThat(coordinatesMultiBuffer.y(1)).isEqualTo(4.0)
  }
}

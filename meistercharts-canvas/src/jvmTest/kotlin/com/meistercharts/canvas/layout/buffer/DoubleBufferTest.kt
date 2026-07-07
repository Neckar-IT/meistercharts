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
import it.neckar.open.provider.MultiDoublesProvider
import it.neckar.open.unit.other.Index
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class DoubleBufferTest {
  private lateinit var doubleMultiBuffer: DoubleMultiBuffer

  @BeforeEach
  fun setUp() {
    doubleMultiBuffer = DoubleMultiBuffer()
  }

  @Test
  fun `check size after initialization`() {
    assertThat(doubleMultiBuffer.size).isEqualTo(0)
  }

  @Test
  fun `ensure size`() {
    doubleMultiBuffer.prepare(5)
    assertThat(doubleMultiBuffer.size).isEqualTo(5)
  }

  @Test
  fun `resize`() {
    doubleMultiBuffer.prepare(2)
    assertThat(doubleMultiBuffer.size).isEqualTo(2)

    doubleMultiBuffer.setAll(17.0)
    assertThat(doubleMultiBuffer[0]).isEqualTo(17.0)
    assertThat(doubleMultiBuffer[1]).isEqualTo(17.0)

    doubleMultiBuffer.resize(5)
    assertThat(doubleMultiBuffer.size).isEqualTo(5)
    assertThat(doubleMultiBuffer[0]).isEqualTo(17.0)
    assertThat(doubleMultiBuffer[1]).isEqualTo(17.0)

    assertThat(doubleMultiBuffer[2]).isEqualTo(0.0)
    assertThat(doubleMultiBuffer[3]).isEqualTo(0.0)
    assertThat(doubleMultiBuffer[4]).isEqualTo(0.0)

    doubleMultiBuffer.setAll(17.0)

    assertThat(doubleMultiBuffer[0]).isEqualTo(17.0)
    assertThat(doubleMultiBuffer[1]).isEqualTo(17.0)

    assertThat(doubleMultiBuffer[2]).isEqualTo(17.0)
    assertThat(doubleMultiBuffer[3]).isEqualTo(17.0)
    assertThat(doubleMultiBuffer[4]).isEqualTo(17.0)
  }

  @Test
  fun `set and get values`() {
    doubleMultiBuffer.prepare(1)
    doubleMultiBuffer[0] = 2.0
    assertThat(doubleMultiBuffer[0]).isEqualTo(2.0)
  }

  @Test
  fun `set all values and check`() {
    doubleMultiBuffer.prepare(3)
    doubleMultiBuffer.setAll(2.0)
    assertThat(doubleMultiBuffer[0]).isEqualTo(2.0)
    assertThat(doubleMultiBuffer[1]).isEqualTo(2.0)
    assertThat(doubleMultiBuffer[2]).isEqualTo(2.0)
  }

  @Test
  fun `get or else returns correct value`() {
    doubleMultiBuffer.prepare(2)
    doubleMultiBuffer[0] = 3.0
    doubleMultiBuffer[1] = 4.0
    val defaultValue = MultiDoublesProvider<Index> { 99.0 }
    assertThat(doubleMultiBuffer.getOrElse(0, defaultValue)).isEqualTo(3.0)
    assertThat(doubleMultiBuffer.getOrElse(1, defaultValue)).isEqualTo(4.0)
    assertThat(doubleMultiBuffer.getOrElse(2, defaultValue)).isEqualTo(99.0)
  }

  @Test
  fun `last or fallback returns correct value`() {
    doubleMultiBuffer.prepare(2)
    doubleMultiBuffer[0] = 5.0
    doubleMultiBuffer[1] = 6.0
    assertThat(doubleMultiBuffer.lastOr(7.0)).isEqualTo(6.0)
  }

  @Test
  fun `last or NaN returns correct value`() {
    doubleMultiBuffer.prepare(2)
    doubleMultiBuffer[0] = 5.0
    doubleMultiBuffer[1] = 6.0
    assertThat(doubleMultiBuffer.lastOrNaN()).isEqualTo(6.0)
  }

  @Test
  fun `fast any returns correct value`() {
    doubleMultiBuffer.prepare(3)
    doubleMultiBuffer[0] = 5.0
    doubleMultiBuffer[1] = 6.0
    doubleMultiBuffer[2] = 7.0
    assertThat(doubleMultiBuffer.fastAny { value -> value == 6.0 }).isTrue()
  }
}

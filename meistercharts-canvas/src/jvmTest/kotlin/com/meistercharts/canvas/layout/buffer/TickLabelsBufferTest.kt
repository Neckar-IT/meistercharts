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
import it.neckar.open.test.utils.isNaN
import org.junit.jupiter.api.Test

class TickLabelsBufferTest {
  @Test
  fun testSetAndGet() {
    val buffer = TickLabelsBuffer()
    assertThat(buffer.isEmpty()).isTrue()

    buffer.prepare(2)
    buffer.set(0, 10.0, "10")
    buffer.set(1, 20.5, "20.5")

    assertThat(buffer.size).isEqualTo(2)
    assertThat(buffer.valueAt(0)).isEqualTo(10.0)
    assertThat(buffer.formattedAt(0)).isEqualTo("10")
    assertThat(buffer.valueAt(1)).isEqualTo(20.5)
    assertThat(buffer.formattedAt(1)).isEqualTo("20.5")
  }

  @Test
  fun testPrepareResets() {
    val buffer = TickLabelsBuffer()
    buffer.prepare(1)
    buffer.set(0, 10.0, "10")

    buffer.prepare(1)

    assertThat(buffer.valueAt(0)).isNaN()
    assertThat(buffer.formattedAt(0)).isEqualTo(StringMultiBuffer.Uninitialized)
  }

  @Test
  fun testFastForEachIndexed() {
    val buffer = TickLabelsBuffer()
    buffer.prepare(2)
    buffer.set(0, 1.0, "one")
    buffer.set(1, 2.0, "two")

    val collected = mutableListOf<String>()
    buffer.fastForEachIndexed { index, value, formatted ->
      collected.add("$index:$value:$formatted")
    }
    assertThat(collected).isEqualTo(mutableListOf("0:1.0:one", "1:2.0:two"))

    val values = mutableListOf<Double>()
    buffer.fastForEachValue { values.add(it) }
    assertThat(values).isEqualTo(mutableListOf(1.0, 2.0))
  }
}

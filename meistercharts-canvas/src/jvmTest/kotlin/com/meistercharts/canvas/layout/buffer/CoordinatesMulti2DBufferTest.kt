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
import org.junit.jupiter.api.Test

/**
 *
 */
class CoordinatesMulti2DBufferTest {
  @Test
  fun testBuffer() {
    val buffer = CoordinatesMulti2DBuffer()
    assertThat(buffer.size).isEqualTo(0)

    buffer.prepare(3)
    assertThat(buffer.size).isEqualTo(3)

    buffer[0].let { buffer0 ->
      assertThat(buffer0.size).isEqualTo(0)
      assertFailure {
        buffer0.set(0, 1.0, 2.0)
      }.messageContains("is out of bounds")

      buffer0.prepare(2)
      buffer0.set(0, 1.0, 2.0)
      buffer0.set(1, 3.0, 4.0)

      assertThat(buffer0.size).isEqualTo(2)
    }

    buffer.prepare(3)
    assertThat(buffer[0].size).isEqualTo(2) //retains size
  }
}

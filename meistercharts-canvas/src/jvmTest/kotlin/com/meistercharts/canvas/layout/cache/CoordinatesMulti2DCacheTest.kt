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
package com.meistercharts.canvas.layout.cache

import assertk.*
import assertk.assertions.*
import org.junit.jupiter.api.Test

/**
 *
 */
class CoordinatesMulti2DCacheTest {
  @Test
  fun testCache() {
    val cache = CoordinatesMulti2DCache()
    assertThat(cache.size).isEqualTo(0)

    cache.prepare(3)
    assertThat(cache.size).isEqualTo(3)

    cache[0].let { cache0 ->
      assertThat(cache0.size).isEqualTo(0)
      assertFailure {
        cache0.set(0, 1.0, 2.0)
      }.messageContains("is out of bounds")

      cache0.prepare(2)
      cache0.set(0, 1.0, 2.0)
      cache0.set(1, 3.0, 4.0)

      assertThat(cache0.size).isEqualTo(2)
    }

    cache.prepare(3)
    assertThat(cache[0].size).isEqualTo(2) //retains size
  }
}

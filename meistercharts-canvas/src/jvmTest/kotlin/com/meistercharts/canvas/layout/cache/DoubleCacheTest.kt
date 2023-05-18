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
import it.neckar.open.provider.MultiDoublesProvider
import it.neckar.open.unit.other.Index
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class DoubleCacheTest {
  private lateinit var doubleCache: DoubleCache

  @BeforeEach
  fun setUp() {
    doubleCache = DoubleCache()
  }

  @Test
  fun `check size after initialization`() {
    assertThat(doubleCache.size).isEqualTo(0)
  }

  @Test
  fun `ensure size`() {
    doubleCache.prepare(5)
    assertThat(doubleCache.size).isEqualTo(5)
  }

  @Test
  fun `resize`() {
    doubleCache.prepare(2)
    assertThat(doubleCache.size).isEqualTo(2)

    doubleCache.setAll(17.0)
    assertThat(doubleCache[0]).isEqualTo(17.0)
    assertThat(doubleCache[1]).isEqualTo(17.0)

    doubleCache.resize(5)
    assertThat(doubleCache.size).isEqualTo(5)
    assertThat(doubleCache[0]).isEqualTo(17.0)
    assertThat(doubleCache[1]).isEqualTo(17.0)

    assertThat(doubleCache[2]).isEqualTo(0.0)
    assertThat(doubleCache[3]).isEqualTo(0.0)
    assertThat(doubleCache[4]).isEqualTo(0.0)

    doubleCache.setAll(17.0)

    assertThat(doubleCache[0]).isEqualTo(17.0)
    assertThat(doubleCache[1]).isEqualTo(17.0)

    assertThat(doubleCache[2]).isEqualTo(17.0)
    assertThat(doubleCache[3]).isEqualTo(17.0)
    assertThat(doubleCache[4]).isEqualTo(17.0)
  }

  @Test
  fun `set and get values`() {
    doubleCache.prepare(1)
    doubleCache[0] = 2.0
    assertThat(doubleCache[0]).isEqualTo(2.0)
  }

  @Test
  fun `set all values and check`() {
    doubleCache.prepare(3)
    doubleCache.setAll(2.0)
    assertThat(doubleCache[0]).isEqualTo(2.0)
    assertThat(doubleCache[1]).isEqualTo(2.0)
    assertThat(doubleCache[2]).isEqualTo(2.0)
  }

  @Test
  fun `get or else returns correct value`() {
    doubleCache.prepare(2)
    doubleCache[0] = 3.0
    doubleCache[1] = 4.0
    val defaultValue = MultiDoublesProvider<Index> { 99.0 }
    assertThat(doubleCache.getOrElse(0, defaultValue)).isEqualTo(3.0)
    assertThat(doubleCache.getOrElse(1, defaultValue)).isEqualTo(4.0)
    assertThat(doubleCache.getOrElse(2, defaultValue)).isEqualTo(99.0)
  }

  @Test
  fun `last or fallback returns correct value`() {
    doubleCache.prepare(2)
    doubleCache[0] = 5.0
    doubleCache[1] = 6.0
    assertThat(doubleCache.lastOr(7.0)).isEqualTo(6.0)
  }

  @Test
  fun `last or NaN returns correct value`() {
    doubleCache.prepare(2)
    doubleCache[0] = 5.0
    doubleCache[1] = 6.0
    assertThat(doubleCache.lastOrNaN()).isEqualTo(6.0)
  }

  @Test
  fun `fast any returns correct value`() {
    doubleCache.prepare(3)
    doubleCache[0] = 5.0
    doubleCache[1] = 6.0
    doubleCache[2] = 7.0
    assertThat(doubleCache.fastAny { value -> value == 6.0 }).isTrue()
  }
}

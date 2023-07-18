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
package com.cedarsoft.collections

import assertk.*
import assertk.assertions.*
import com.meistercharts.algorithms.tile.TileIdentifier
import com.meistercharts.tile.TileIndex
import com.meistercharts.charts.ChartId
import com.meistercharts.model.Zoom
import it.neckar.open.collections.Cache
import org.junit.jupiter.api.Test

@Suppress("DEPRECATION")
class CacheTest {
  val chartId = ChartId(17)

  @Test
  fun store() {
    val cacheCapacity = 100
    val cache = Cache<TileIdentifier, String>(cacheCapacity)

    for (i in 1..cacheCapacity) {
      val initialCacheContent = TileIdentifier(chartId, TileIndex.of(1000 * i, 1000 * i), Zoom(2.0, 3.0))
      cache[initialCacheContent] = "val-initial-$i"
    }

    for (i in 1..100) {
      for (j in 1..100) {
        val tileIdentifier = TileIdentifier(chartId, TileIndex.of(i, j), Zoom(2.0, 3.0))
        cache[tileIdentifier] = "val-$i-$j"
        assert(cache.size == cacheCapacity)
      }
    }

    for (j in 1..100) {
      val tileIdentifier = TileIdentifier(chartId, TileIndex.of(100, j), Zoom(2.0, 3.0))
      assert(cache[tileIdentifier] == "val-100-$j")
    }
  }


  @Test
  fun storeOne() {
    val cache = Cache<TileIdentifier, String>(1)

    cache[TileIdentifier(chartId, TileIndex.of(0, 0), Zoom(2.0, 3.0))] = "val0"

    for (i in 1..1000) {
      val initialCacheContent = TileIdentifier(chartId, TileIndex.of(i, i), Zoom(2.0, 3.0))
      cache[initialCacheContent] = "val$i"
      assert(cache.size == 1)
    }
  }

  @Test
  fun testZeroCheck() {
    assertThat {
      val cache = Cache<Int, String>(0)
      cache[7] = "asdf"
    }.isFailure()
  }
}

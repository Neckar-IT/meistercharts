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
package com.meistercharts.algorithms.tile

import assertk.*
import assertk.assertions.*
import com.meistercharts.charts.ChartId
import com.meistercharts.model.Size
import com.meistercharts.model.Zoom
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 *
 */
class CachedTileProviderTest {
  val chartId: ChartId = ChartId(17)
  val tileIdentifier: TileIdentifier = TileIdentifier(chartId, TileIndex.of(7, 8), Zoom.default)

  @BeforeEach
  internal fun setUp() {
    GlobalTilesCache.clearAll()
  }

  @AfterEach
  internal fun tearDown() {
    GlobalTilesCache.clearAll()
  }

  @Test
  fun testRenew() {
    val cachedTileProvider = CachedTileProvider(ChartId(77), DebugTileProvider(Size.PX_120))
    val cache = GlobalTilesCache.cache

    assertThat(cache.size).isEqualTo(0)

    val id0 = TileIdentifier(chartId, TileIndex.of(1, 1), Zoom.default)
    val id1 = TileIdentifier(chartId, TileIndex.of(2, 1), Zoom.default)
    val id3 = TileIdentifier(chartId, TileIndex.of(3, 1), Zoom.default)

    cachedTileProvider.getTile(id0)
    cachedTileProvider.getTile(id1)

    assertThat(cache.keys).containsAll(id0, id1)

    cachedTileProvider.getTile(id0)

    assertThat(cache.keys).containsAll(id0, id1)

    //request another
    cachedTileProvider.getTile(id3)

    assertThat(cachedTileProvider.getTileFromCache(id0)).isNotNull()
    assertThat(cachedTileProvider.getTileFromCache(id3)).isNotNull()

    assertThat(cache.keys).containsAll(id0, id1, id3)

    GlobalTilesCache.clear(chartId)
    assertThat(cache.keys).containsNone(id0, id1, id3)
  }

  @Test
  fun testClear() {
    val cachedTileProvider = CachedTileProvider(ChartId(77), DebugTileProvider(Size.PX_120))
    val cache = GlobalTilesCache.cache

    assertThat(cache.size).isEqualTo(0)

    cachedTileProvider.getTileFromCache(tileIdentifier).let {
      assertThat(it).isNull()
    }

    assertThat(cache.size).isEqualTo(0)
    val tile = cachedTileProvider.getTile(tileIdentifier)
    assertThat(tile).isNotNull()
    assertThat(cache.size).isEqualTo(1)

    //Check cache
    assertThat(cachedTileProvider.getTile(tileIdentifier)).isSameAs(tile)

    var count = 0

    GlobalTilesCache.cache.removeIf {
      assertThat(count).isEqualTo(0)
      assertThat(it).isEqualTo(tileIdentifier)
      count++
      true
    }

    assertThat(cache.size).isEqualTo(0)

    //*NOT* cached anymore
    assertThat(cachedTileProvider.getTile(tileIdentifier)).isNotSameAs(tile)
  }
}

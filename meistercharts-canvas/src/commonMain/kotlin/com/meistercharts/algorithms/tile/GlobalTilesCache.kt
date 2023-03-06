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

import com.meistercharts.charts.ChartId
import it.neckar.open.collections.Cache
import it.neckar.open.collections.cache
import it.neckar.open.annotations.Slow

/**
 * Global cache for tiles.
 *
 * This class should be used to ensure maximum amount of memory used even if many charts are used
 */
object GlobalTilesCache {
  /**
   * The tiles cache
   */
  internal val cache: Cache<TileIdentifier, Tile> = cache("GlobalTilesCache", 200) { _, tile ->
    tile.dispose()
  }

  val size: Int
    get() {
      return cache.size
    }

  /**
   * Sets the cache size.
   *
   * ATTENTION: use with care! Usually it is not required to modify this value
   */
  fun setCacheSize(newMaxSize: Int) {
    cache.updateMaxSize(newMaxSize)
  }

  /**
   * Returns the tile for the given identifier
   */
  operator fun get(tileIdentifier: TileIdentifier): Tile? {
    return cache[tileIdentifier]
  }

  operator fun set(identifier: TileIdentifier, value: Tile) {
    cache[identifier] = value
  }

  fun markAsNew(identifier: TileIdentifier) {
    cache.markAsNew(identifier)
  }

  /**
   * Clears all entries for the given chart id
   */
  fun clear(chartId: ChartId) {
    cache.removeIf {
      it.chartId == chartId
    }
  }

  /**
   * Clears the complete cache for *all* charts.
   *
   * USE WITH CARE!
   */
  fun clearAll() {
    cache.clear()
  }

  /**
   * Returns all tiles for the given chart ID
   */
  @Slow
  fun tiles(chartId: ChartId): Collection<Tile> {
    return cache.values.filter {
      it.identifier.chartId == chartId
    }
  }
}

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

import com.meistercharts.algorithms.tile.GlobalTilesCache.cache
import com.meistercharts.annotations.Zoomed
import com.meistercharts.charts.ChartId
import com.meistercharts.model.Size
import it.neckar.open.annotations.Slow

/**
 * A [TileProvider] that stores tiles in a cache.
 * It uses the global tiles cache ([GlobalTilesCache]).
 */
class CachedTileProvider(
  /**
   * The chart ID.
   * This property is used to clear the cache
   */
  val chartId: ChartId,
  /**
   * Is used to create the tiles
   */
  val delegate: TileProvider,
) : TileProvider {

  @Zoomed
  override val tileSize: Size
    get() = delegate.tileSize

  override fun getTile(identifier: TileIdentifier): Tile? {
    //Returns the cached tile - if found
    GlobalTilesCache[identifier]?.let { cachedTile ->
      //Mark the tile as "new" to avoid premature eviction from the cache
      GlobalTilesCache.markAsNew(identifier)
      return cachedTile
    }

    return delegate.getTile(identifier)?.also { newTile ->
      //Store in cache
      GlobalTilesCache[identifier] = newTile
    }
  }

  /**
   * Retrieves the tile from the cache if present
   */
  fun getTileFromCache(identifier: TileIdentifier): Tile? = cache[identifier]

  /**
   * Clears the complete cache.
   * All tiles are removed completely
   */
  fun clear() {
    GlobalTilesCache.clear(chartId)
  }

  /**
   * All tiles currently held in the cache
   */
  @Slow
  fun tiles(): Collection<Tile> {
    return GlobalTilesCache.tiles(chartId)
  }

  override fun toString(): String {
    return "CachedTileProvider"
  }
}

/**
 * Wraps the tile provider in a [CachedTileProvider]
 */
fun TileProvider.cached(chartId: ChartId): CachedTileProvider {
  return CachedTileProvider(chartId, this)
}

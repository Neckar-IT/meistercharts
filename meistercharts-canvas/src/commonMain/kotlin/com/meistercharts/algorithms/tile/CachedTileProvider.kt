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

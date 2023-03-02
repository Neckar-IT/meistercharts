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

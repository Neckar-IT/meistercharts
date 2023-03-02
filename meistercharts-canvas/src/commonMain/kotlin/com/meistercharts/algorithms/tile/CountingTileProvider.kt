package com.meistercharts.algorithms.tile

import com.meistercharts.annotations.Zoomed
import com.meistercharts.canvas.currentFrameTimestamp
import com.meistercharts.model.Size
import it.neckar.open.collections.incr
import it.neckar.open.unit.si.ms

/**
 * Collects debug information about when and how often tiles are provided
 */
class CountingTileProvider(val delegate: TileProvider) : TileProvider {
  override val tileSize: @Zoomed Size
    get() = delegate.tileSize

  override fun getTile(identifier: TileIdentifier): Tile? {
    lastPaintTimes[identifier] = currentFrameTimestamp
    providedCount.incr(identifier)
    return delegate.getTile(identifier)
  }

  /**
   * Contains the last time the tile for the given identifier has been provided
   */
  private val lastPaintTimes = mutableMapOf<TileIdentifier, @ms Double>()

  /**
   * Returns the last paint time
   */
  fun lastPaintTime(tileIdentifier: TileIdentifier): @ms Double? {
    return lastPaintTimes[tileIdentifier]
  }

  /**
   * How often a tile has been provided
   */
  private val providedCount = mutableMapOf<TileIdentifier, Int>()

  /**
   * How often a tile has been provided for the given identifier
   */
  fun providedCount(tileIdentifier: TileIdentifier): Int {
    return providedCount[tileIdentifier] ?: 0
  }

}

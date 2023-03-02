package com.meistercharts.algorithms.tile

import com.meistercharts.annotations.Zoomed
import com.meistercharts.model.Size

/**
 * Provides tiles
 */
interface TileProvider {
  /**
   * The size of the tiles this provider returns
   */
  val tileSize: @Zoomed Size

  /**
   * Returns a tile identifier
   */
  fun getTile(identifier: TileIdentifier): Tile?
}


package com.meistercharts.algorithms.tile

import com.meistercharts.annotations.Tile
import com.meistercharts.model.Coordinates

/**
 * Contains a tile index to identify the tile and coordinates relative to the tile origin
 */
data class TileCoordinates(
  val tileIndex: TileIndex,
  /**
   * The coordinates relative to the origin of the tile
   */
  @Tile val coordinates: Coordinates
) {

  companion object {
    fun of(tileIndex: TileIndex, coordinatesInTile: Coordinates): TileCoordinates {
      return TileCoordinates(tileIndex, coordinatesInTile)
    }
  }
}

package com.meistercharts.algorithms.tile

import com.meistercharts.charts.ChartId
import com.meistercharts.model.Zoom

/**
 * Identifies a single tile for a single chart
 */
data class TileIdentifier(
  /**
   * The chart the tile identifier is valid for
   */
  val chartId: ChartId,

  /**
   * The index of the tile
   */
  val tileIndex: TileIndex,

  /**
   * The zoom this tile is valid for
   */
  val zoom: Zoom,
) {

  val x: Int
    get() = tileIndex.x

  val y: Int
    get() = tileIndex.y

  override fun toString(): String {
    return "TileIdentifier(x=$x, y=$y, zoomX=${zoom.scaleX}, zoomY=${zoom.scaleY})"
  }

  companion object {
    fun of(chartId: ChartId, x: Int, y: Int, zoom: Zoom): TileIdentifier {
      return TileIdentifier(chartId, TileIndex(x, y), zoom)
    }
  }
}

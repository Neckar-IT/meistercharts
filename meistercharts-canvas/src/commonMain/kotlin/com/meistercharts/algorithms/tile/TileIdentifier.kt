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
import com.meistercharts.model.Zoom
import com.meistercharts.tile.MainIndex
import com.meistercharts.tile.SubIndex
import com.meistercharts.tile.TileIndex

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

  val mainX: MainIndex
    get() = tileIndex.mainX

  val subX: SubIndex
    get() = tileIndex.subX

  val mainY: MainIndex
    get() = tileIndex.mainY

  val subY: SubIndex
    get() = tileIndex.subY

  override fun toString(): String {
    return "TileIdentifier(tileIndex: $tileIndex, zoomX=${zoom.scaleX}, zoomY=${zoom.scaleY})"
  }

  /**
   * Returns true if this tile index is within the provided top left / bottom right tile index
   */
  fun isWithin(topLeft: TileIndex, bottomRight: TileIndex): Boolean {
    return tileIndex.isWithin(topLeft, bottomRight)
  }

  companion object {
    /**
     * Creates a new instance
     */
    fun of(
      chartId: ChartId,
      mainX: MainIndex,
      subX: SubIndex,
      mainY: MainIndex,
      subY: SubIndex,
      zoom: Zoom,
    ): TileIdentifier {
      val tileIndex = TileIndex(mainX, subX, mainY, subY)
      return TileIdentifier(chartId, tileIndex, zoom)
    }

    val compareByX: Comparator<in TileIdentifier> = compareBy(TileIndex.compareByX) { it.tileIndex }
    val compareByY: Comparator<in TileIdentifier> = compareBy(TileIndex.compareByY) { it.tileIndex }
    val compareByRow: Comparator<in TileIdentifier> = compareBy(TileIndex.compareByRow) { it.tileIndex }
  }
}

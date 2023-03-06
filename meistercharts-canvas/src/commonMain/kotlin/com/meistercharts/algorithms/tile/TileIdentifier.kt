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

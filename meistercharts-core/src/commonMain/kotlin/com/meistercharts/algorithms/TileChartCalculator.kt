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
package com.meistercharts.algorithms

import com.meistercharts.algorithms.tile.TileIndex
import com.meistercharts.annotations.ContentArea
import com.meistercharts.annotations.ContentAreaRelative
import com.meistercharts.annotations.DomainRelative
import com.meistercharts.annotations.Tile
import com.meistercharts.annotations.TileRelative
import com.meistercharts.annotations.TimeRelative
import com.meistercharts.annotations.Window
import com.meistercharts.model.Coordinates
import com.meistercharts.model.Size
import it.neckar.open.unit.other.px
import it.neckar.open.unit.si.ms

/**
 * The `TileChartCalculator` class provides a calculator for charts displayed in tiled form.
 *
 * @param chartState The chart state
 * @param tileIndex The tile index for this tile
 * @param tileSize The size of the tiles
 */
class TileChartCalculator(
  chartState: ChartState,
  /**
   * The tile index for this tile
   */
  val tileIndex: TileIndex,
  /**
   * The size of the tiles
   */
  val tileSize: @px Size
) : ChartCalculator(chartState) {

  val tileWidth: @px Double
    get() {
      return tileSize.width
    }

  val tileHeight: @px Double
    get() {
      return tileSize.height
    }

  /**
   * Returns the origin for the given tile index
   */
  fun tileOrigin2contentAreaX(): @ContentArea Double {
    return InternalCalculations.calculateTileOriginX(tileIndex, zoomed2contentAreaX(tileWidth))
  }

  /**
   * Returns the origin for the given tile index
   */
  fun tileOrigin2contentAreaY(): @ContentArea Double {
    return InternalCalculations.calculateTileOriginY(tileIndex, zoomed2contentAreaY(tileHeight))
  }

  /**
   * Returns the origin of the tile in content area relative
   */
  fun tileOrigin2contentAreaRelativeX(): @ContentAreaRelative Double {
    return contentArea2contentAreaRelativeX(tileOrigin2contentAreaX())
  }

  fun tileOrigin2contentAreaRelativeY(): @ContentAreaRelative Double {
    return contentArea2contentAreaRelativeY(tileOrigin2contentAreaY())
  }

  /**
   * Returns the origin of the tile in content area relative
   */
  fun origin2contentAreaRelativeX(): @ContentAreaRelative Double {
    return contentArea2contentAreaRelativeX(tileOrigin2contentAreaX())
  }

  fun origin2contentAreaRelativeY(): @ContentAreaRelative Double {
    return contentArea2contentAreaRelativeY(tileOrigin2contentAreaY())
  }

  /**
   * Returns the content are relative x value of the tile origin
   */
  fun tile2contentAreaRelativeX(tileX: @Tile Double): @ContentArea Double {
    @ContentArea val contentArea = tile2contentAreaX(tileX)
    return contentArea2contentAreaRelativeX(contentArea)
  }

  fun contentAreaRelative2tileX(contentAreaRelative: @ContentAreaRelative Double): @Tile Double {
    @ContentArea val contentArea = contentAreaRelative2contentAreaX(contentAreaRelative)
    return contentArea2tileX(contentArea)
  }

  fun tile2contentAreaRelativeY(tileY: @Tile Double): @ContentArea Double {
    @ContentArea val contentArea = tile2contentAreaY(tileY)
    return contentArea2contentAreaRelativeY(contentArea)
  }

  fun contentAreaRelative2tileY(contentAreaRelative: @ContentAreaRelative Double): @Tile Double {
    @ContentArea val contentArea = contentAreaRelative2contentAreaY(contentAreaRelative)
    return contentArea2tileY(contentArea)
  }

  fun domainRelative2tileX(domainRelative: @DomainRelative Double): @Tile Double {
    @ContentAreaRelative val contentAreaRelative = domainRelative2contentAreaRelativeX(domainRelative)
    return contentAreaRelative2tileX(contentAreaRelative)
  }

  fun domainRelative2tileY(domainRelative: @DomainRelative Double): @Tile Double {
    @ContentAreaRelative val contentAreaRelative = domainRelative2contentAreaRelativeY(domainRelative)
    return contentAreaRelative2tileY(contentAreaRelative)
  }

  /**
   * Returns a tile relative value
   */
  fun tile2tileRelativeX(tileX: @Tile Double): @TileRelative Double {
    return 1.0 / tileSize.width * tileX
  }

  fun tileRelative2tileX(tileRelative: @TileRelative Double): @Tile Double {
    return tileSize.width * tileRelative
  }

  /**
   * Returns a tile relative value
   */
  fun tile2tileRelativeY(tileY: @Tile Double): @TileRelative Double {
    return 1.0 / tileSize.height * tileY
  }

  fun tileRelative2tileY(tileRelative: @TileRelative Double): @Tile Double {
    return tileSize.height * tileRelative
  }

  /**
   * Returns the content area value
   */
  fun tile2contentAreaX(tileX: @Tile Double): @ContentArea Double {
    return tileOrigin2contentAreaX() + zoomed2contentAreaX(tileX)
  }

  /**
   * Returns the time value at origin of the tile
   */
  fun tileOrigin2timeX(contentAreaTimeRange: TimeRange): @ms Double {
    @ContentAreaRelative val contentAreaRelative = origin2contentAreaRelativeX()
    @DomainRelative val domainRelative = contentAreaRelative2domainRelativeX(contentAreaRelative)

    return contentAreaTimeRange.relative2time(domainRelative)
  }

  fun tile2timeX(tile: @Tile @px Double, contentAreaTimeRange: TimeRange): @ms Double {
    return tileOrigin2timeX(contentAreaTimeRange) + zoomed2timeDeltaX(tile, contentAreaTimeRange)
  }

  fun time2tileX(time: @ms Double, contentAreaTimeRange: TimeRange): @Tile Double {
    @TimeRelative val timeRelative = contentAreaTimeRange.time2relative(time)

    @ContentAreaRelative val contentAreaRelative = domainRelative2contentAreaRelativeX(timeRelative)
    return contentAreaRelative2tileX(contentAreaRelative)
  }

  fun time2tileY(time: @ms Double, contentAreaTimeRange: TimeRange): @Tile Double {
    @TimeRelative val timeRelative = contentAreaTimeRange.time2relative(time)

    @ContentAreaRelative val contentAreaRelative = domainRelative2contentAreaRelativeY(timeRelative)
    return contentAreaRelative2tileY(contentAreaRelative)
  }

  fun tileOrigin2timeY(contentAreaTimeRange: TimeRange): @ms Double {
    @ContentAreaRelative val contentAreaRelative = origin2contentAreaRelativeY()
    @DomainRelative val domainRelative = contentAreaRelative2domainRelativeY(contentAreaRelative)

    return contentAreaTimeRange.relative2time(domainRelative)
  }

  fun tile2timeY(tile: @px Double, contentAreaTimeRange: TimeRange): @ms Double {
    return tileOrigin2timeY(contentAreaTimeRange) + zoomed2timeDeltaY(tile, contentAreaTimeRange)
  }

  fun tile2contentAreaY(tileY: @Tile Double): @ContentArea Double {
    return tileOrigin2contentAreaY() + zoomed2contentAreaY(tileY)
  }

  /**
   * Converts a content area value to a tile value
   */
  fun contentArea2tileX(contentArea: @ContentArea Double): @Tile Double {
    return contentArea2zoomedX(contentArea - tileOrigin2contentAreaX())
  }

  /**
   * Converts a content area value to a tile value
   */
  fun contentArea2tileY(contentArea: @ContentArea Double): @Tile Double {
    return contentArea2zoomedY(contentArea - tileOrigin2contentAreaY())
  }

  /**
   * Converts the given tile value to a content area value
   */
  fun tile2contentArea(tile: @Tile Coordinates): @ContentArea Coordinates {
    val relativeInContentArea = zoomed2contentArea(tile)

    return tileIndex2contentArea(tileIndex, tileSize)
      .plus(
        relativeInContentArea.x, relativeInContentArea.y
      )
  }

  fun tile2contentArea(tileX: @Tile Double, tileY: @Tile Double): @ContentArea Coordinates {
    val relativeInContentAreaX = zoomed2contentAreaX(tileX)
    val relativeInContentAreaY = zoomed2contentAreaY(tileY)

    return tileIndex2contentArea(tileIndex, tileSize)
      .plus(
        relativeInContentAreaX, relativeInContentAreaY
      )
  }

  fun tile2window(tile: @Tile Coordinates): @Window Coordinates {
    return tileIndex2window(tileIndex, tileSize)
      .plus(
        tile.x, tile.y
      )
  }

  fun tile2window(tileX: @Tile Double, tileY: @Tile Double): @Window Coordinates {
    return tileIndex2window(tileIndex, tileSize)
      .plus(
        tileX, tileY
      )
  }

  fun visibleTimeRangeXinTile(contentAreaTimeRange: TimeRange): TimeRange {
    return TimeRange.fromUnsorted(
      tile2timeX(0.0, contentAreaTimeRange), //left side
      tile2timeX(tileWidth, contentAreaTimeRange) //right side
    )
  }

  fun visibleTimeRangeYinTile(contentAreaTimeRange: TimeRange): TimeRange {
    return TimeRange.fromUnsorted(
      tile2timeY(0.0, contentAreaTimeRange), //top
      tile2timeY(tileHeight, contentAreaTimeRange) //bottom
    )
  }
}

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
package com.meistercharts.calc

import it.neckar.geometry.AxisInversionInformation
import com.meistercharts.tile.MainIndex
import com.meistercharts.tile.SubIndex
import com.meistercharts.tile.TileCoordinates
import com.meistercharts.tile.TileIndex
import com.meistercharts.annotations.ContentArea
import com.meistercharts.annotations.ContentAreaRelative
import com.meistercharts.annotations.DomainRelative
import com.meistercharts.annotations.Tile
import com.meistercharts.annotations.Window
import com.meistercharts.annotations.Zoomed
import it.neckar.geometry.Coordinates
import it.neckar.geometry.Size
import it.neckar.open.unit.other.pct
import it.neckar.open.unit.other.px
import kotlin.math.floor

/**
 * Contains calculation methods that are usually not used directly.
 * These calculations often take unexpected parameters.
 *
 * Therefore these methods should not be called directly.
 *
 */
object InternalCalculations {
  //
  // Forward conversion: Domain to Window
  //
  // DomainRelative --> ContentAreaRelative --> ContentArea --> Zoomed --> Window
  //

  @ContentAreaRelative
  fun domainRelative2contentAreaRelative(@DomainRelative domainRelative: Double, axisInversionInformation: AxisInversionInformation): Double {
    return if (axisInversionInformation.axisInverted) {
      1.0 - domainRelative
    } else domainRelative

  }

  // ContentAreaRelative --> ContentArea

  @ContentArea
  @px
  fun contentAreaRelative2contentArea(@ContentAreaRelative @pct value: Double, factor: Double): Double {
    return value * factor
  }

  // ContentArea --> Zoomed

  @Zoomed
  @px
  fun contentArea2zoomed(@px @ContentArea value: Double, zoomFactor: Double): Double {
    return value * zoomFactor
  }

  // Zoomed --> Window

  @Window
  @px
  fun zoomed2window(value: @Zoomed Double, translate: @Zoomed Double): Double {
    return value + translate
  }

  //
  // Backward conversion: Window to Domain
  //
  // Window --> Zoomed --> ContentArea --> ContentAreaRelative --> DomainRelative
  //

  // Window --> Zoomed

  @Zoomed
  @px
  fun window2zoomed(value: @Window Double, translate: @Zoomed Double): Double {
    return value - translate
  }

  // Zoomed --> ContentArea

  @ContentArea
  @px
  fun zoomed2contentArea(@Zoomed @px value: Double, zoomFactor: Double): Double {
    return value / zoomFactor
  }

  // ContentArea --> ContentAreaRelative

  @ContentAreaRelative
  @pct
  fun contentArea2contentAreaRelative(@ContentArea @px value: Double, factor: Double): Double {
    return value / factor
  }

  // ContentArea Relative --> DomainRelative

  @DomainRelative
  fun contentAreaRelative2domainRelative(@ContentAreaRelative contentAreaRelative: Double, axisInversionInformation: AxisInversionInformation): Double {
    return if (axisInversionInformation.axisInverted) {
      1 - contentAreaRelative
    } else contentAreaRelative
  }

  // TODO vielleicht den Kachel-spezfischen Teil in eine eigene Klasse?

  /**
   * Calculates the origin of a tile.
   * The origin of the tile is returned relative to the contentArea.
   */
  fun calculateTileOrigin(tileIndex: TileIndex, @ContentArea tileSize: Size): @ContentArea Coordinates {
    val x = calculateTileOriginX(tileIndex, tileSize.width)
    val y = calculateTileOriginY(tileIndex, tileSize.height)
    return Coordinates.of(x, y)
  }

  fun calculateTileOriginX(tileIndex: TileIndex, @ContentArea tileWidth: Double): @ContentArea Double {
    return tileIndex.mainX.value.toDouble() * TileIndex.SubIndexFactor * tileWidth + tileIndex.subX.value * tileWidth
  }

  fun calculateTileOriginY(tileIndex: TileIndex, @ContentArea tileHeight: Double): @ContentArea Double {
    return tileIndex.mainY.value.toDouble() * TileIndex.SubIndexFactor * tileHeight + tileIndex.subY.value * tileHeight
  }

  /**
   * Returns the tile at the given contentArea coordinates
   */
  fun calculateTileIndex(contentAreaCoordinates: @ContentArea Coordinates, tileSize: @ContentArea Size): TileIndex {
    val contentAreaX = contentAreaCoordinates.x
    val contentAreaY = contentAreaCoordinates.y
    return calculateTileIndex(contentAreaX, contentAreaY, tileSize)
  }

  /**
   * Calculates the tile index based on the given content area coordinates and tile size.
   *
   * @param contentAreaX The x-coordinate within the content area.
   * @param contentAreaY The y-coordinate within the content area.
   * @param tileSize The size of a single tile.
   * @return A TileIndex representing the calculated tile index.
   */
  fun calculateTileIndex(contentAreaX: @ContentArea Double, contentAreaY: @ContentArea Double, tileSize: @ContentArea Size): TileIndex {
    val mainX = calculateMainTileIndexX(contentAreaX, tileSize.width)
    val subX = calculateSubTileIndexX(contentAreaX, tileSize.width)
    val mainY = calculateMainTileIndexY(contentAreaY, tileSize.height)
    val subY = calculateSubTileIndexY(contentAreaY, tileSize.height)
    return TileIndex.of(mainX, subX, mainY, subY)
  }

  /**
   * Calculates the main tile index on the x-axis based on the given content area x-coordinate and tile width.
   *
   * @param contentAreaX The x-coordinate within the content area.
   * @param tileWidth The width of a single tile.
   * @return The main index as an Int.
   */
  fun calculateMainTileIndexX(contentAreaX: @ContentArea Double, tileWidth: @ContentArea Double): Int {
    return MainIndex.calculateMainTileIndexPart(contentAreaX / tileWidth)
  }

  /**
   * Calculates the sub tile index on the x-axis based on the given content area x-coordinate and tile width.
   *
   * @param contentAreaCoordinateX The x-coordinate within the content area.
   * @param tileWidth The width of a single tile.
   * @return The sub-index as an Int.
   */
  fun calculateSubTileIndexX(contentAreaCoordinateX: @ContentArea Double, tileWidth: @ContentArea Double): Int {
    return SubIndex.calculateSubTileIndexPart(contentAreaCoordinateX / tileWidth)
  }

  /**
   * Calculates the main tile index on the y-axis based on the given content area y-coordinate and tile height.
   *
   * @param contentAreaY The y-coordinate within the content area.
   * @param tileHeight The height of a single tile.
   * @return The main index as an Int.
   */
  fun calculateMainTileIndexY(contentAreaY: @ContentArea Double, tileHeight: @ContentArea Double): Int {
    return MainIndex.calculateMainTileIndexPart(contentAreaY / tileHeight)
  }

  /**
   * Calculates the sub tile index on the y-axis based on the given content area y-coordinate and tile height.
   *
   * @param contentAreaY The y-coordinate within the content area.
   * @param tileHeight The height of a single tile.
   * @return The sub-index as an Int.
   */
  fun calculateSubTileIndexY(contentAreaY: @ContentArea Double, tileHeight: @ContentArea Double): Int {
    return SubIndex.calculateSubTileIndexPart(contentAreaY / tileHeight)
  }

  @Deprecated("do not use anymore")
  fun calculateTileIndexX(contentAreaCoordinateX: @ContentArea Double, tileWidth: @ContentArea Double): Int {
    return floor(contentAreaCoordinateX / tileWidth).toInt()
  }

  @Deprecated("do not use anymore")
  fun calculateTileIndexY(contentAreaCoordinateY: @ContentArea Double, tileHeight: @ContentArea Double): Int {
    return floor(contentAreaCoordinateY / tileHeight).toInt()
  }

  /**
   * Converts the contentArea coordinates to tile relative coordinates
   */
  @Tile
  fun calculateCoordsInTile(contentAreaCoordinates: @ContentArea Coordinates, tileSize: @ContentArea Size): Coordinates {
    val x = contentAreaCoordinates.x % tileSize.width
    val y = contentAreaCoordinates.y % tileSize.height

    return Coordinates(x, y)
  }

  /**
   * Converts a contentArea coordinate to a tile coordinate relative to the tile with the given tile-index
   */
  fun contentArea2TileCoordinates(tileIndex: TileIndex, @ContentArea contentAreaCoordinates: Coordinates, @ContentArea tileSize: Size): TileCoordinates {
    val tileOrigin = calculateTileOrigin(tileIndex, tileSize)
    return TileCoordinates.of(tileIndex, contentAreaCoordinates.minusAsCoordinates(tileOrigin))
  }

  /**
   * Converts a contentArea coordinate to a tile coordinate that includes the tile index and the coordinate within the tile
   */
  fun contentArea2TileCoordinates(@ContentArea contentAreaCoordinates: Coordinates, @ContentArea tileSize: Size): TileCoordinates {
    val tileIndex = calculateTileIndex(contentAreaCoordinates, tileSize)
    @Tile val coordinatesInTile = calculateCoordsInTile(contentAreaCoordinates, tileSize)

    return TileCoordinates.of(tileIndex, coordinatesInTile)
  }
}

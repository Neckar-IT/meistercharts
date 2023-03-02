package com.meistercharts.algorithms

import com.meistercharts.algorithms.axis.AxisInversionInformation
import com.meistercharts.algorithms.tile.TileCoordinates
import com.meistercharts.algorithms.tile.TileIndex
import com.meistercharts.annotations.ContentArea
import com.meistercharts.annotations.ContentAreaRelative
import com.meistercharts.annotations.DomainRelative
import com.meistercharts.annotations.Tile
import com.meistercharts.annotations.Window
import com.meistercharts.annotations.Zoomed
import com.meistercharts.model.Coordinates
import com.meistercharts.model.Size
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
    return Coordinates.of(tileIndex.x * tileSize.width, tileIndex.y * tileSize.height)
  }

  fun calculateTileOriginX(tileIndex: TileIndex, @ContentArea tileWidth: Double): @ContentArea Double {
    return tileIndex.x * tileWidth
  }

  fun calculateTileOriginY(tileIndex: TileIndex, @ContentArea tileWidth: Double): @ContentArea Double {
    return tileIndex.y * tileWidth
  }

  /**
   * Returns the tile at the given contentArea coordinates
   */
  fun calculateTileIndex(contentAreaCoordinates: @ContentArea Coordinates, tileSize: @ContentArea Size): TileIndex {
    val contentAreaX = contentAreaCoordinates.x
    val contentAreaY = contentAreaCoordinates.y
    return calculateTileIndex(contentAreaX, contentAreaY, tileSize)
  }

  fun calculateTileIndex(contentAreaX: @ContentArea Double, contentAreaY: @ContentArea Double, tileSize: @ContentArea Size): TileIndex {
    val x = calculateTileIndexX(contentAreaX, tileSize.width)
    val y = calculateTileIndexY(contentAreaY, tileSize.height)
    return TileIndex.of(x, y)
  }

  fun calculateTileIndexX(contentAreaCoordinateX: @ContentArea Double, tileWidth: @ContentArea Double): Int {
    return floor(contentAreaCoordinateX / tileWidth).toInt()
  }

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

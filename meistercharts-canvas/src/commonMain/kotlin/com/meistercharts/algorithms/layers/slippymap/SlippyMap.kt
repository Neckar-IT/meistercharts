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
package com.meistercharts.algorithms.layers.slippymap

import com.meistercharts.calc.ChartCalculator
import com.meistercharts.zoom.ZoomAndTranslationModifier
import com.meistercharts.environment
import com.meistercharts.zoom.ZoomAndTranslationModifiersBuilder
import com.meistercharts.tile.SubIndex
import com.meistercharts.tile.TileIndex
import com.meistercharts.annotations.DomainRelative
import com.meistercharts.annotations.PhysicalPixel
import com.meistercharts.annotations.Zoomed
import com.meistercharts.geometry.Distance
import com.meistercharts.geometry.geo.Latitude
import com.meistercharts.geometry.geo.Longitude
import com.meistercharts.geometry.geo.MapCoordinates
import com.meistercharts.model.Size
import com.meistercharts.model.Zoom
import it.neckar.open.kotlin.lang.atan
import it.neckar.open.kotlin.lang.sinh
import it.neckar.open.kotlin.lang.toDegrees
import it.neckar.open.kotlin.lang.toRadians
import it.neckar.open.kotlin.lang.wrapAround
import it.neckar.open.unit.si.rad
import kotlin.math.PI
import kotlin.math.asinh
import kotlin.math.atan
import kotlin.math.floor
import kotlin.math.log2
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sinh
import kotlin.math.tan

/**
 * The default zoom when using slippy map.
 * This is the zoom level that is used when the canvas has a zoom factor of 1.0
 */
const val SlippyMapDefaultZoom: Int = 9

/**
 * The longitude at the left edge of a map
 */
val LongitudeLeftEdge: Longitude = Longitude(-180.0)

/**
 * The longitude at the right edge of a map
 */
val LongitudeRightEdge: Longitude = Longitude(180.0)

/**
 * The latitude at the top edge of a map
 */
val LatitudeTopEdge: Latitude = Latitude(atan(sinh(PI)).toDegrees()) //~ 85.0511

/**
 * The latitude at the bottom edge of a map
 */
val LatitudeBottomEdge: Latitude = Latitude(atan(sinh(-PI)).toDegrees()) //~ -85.0511

/**
 * The tile size of a slippy map tile - physical size!
 */
val SlippyMapTilePhysicalSize: Size = @PhysicalPixel Size(256.0, 256.0)

/**
 * Returns the tile size for slippy map - depending on the current device pixel ratio
 */
fun calculateSlippyMapTileSize(): Size {
  return Size(
    SlippyMapTilePhysicalSize.width / environment.devicePixelRatio,
    SlippyMapTilePhysicalSize.height / environment.devicePixelRatio
  )
}

/**
 * The content area size for slippy maps - depending on the current device pixel ratio
 */
fun calculateSlippyMapContentAreaSize(): Size {
  return calculateSlippyMapTileSize().times(2.0.pow(SlippyMapDefaultZoom))
}

/**
 * Calculates the slippy map tile index for the given latitude/longitude
 * [https://wiki.openstreetmap.org](https://wiki.openstreetmap.org/wiki/Slippy_map_tilenames#Kotlin)
 */
fun computeSlippyMapTileIndex(latitude: Latitude, longitude: Longitude, zoom: Int): TileIndex {
  @rad val latRad = latitude.value.toRadians()
  val tilesPerRowOrColumn = tilesPerRowOrColumn(zoom)
  var xTile = floor((longitude.value + 180) / 360 * tilesPerRowOrColumn).toInt()
  var yTile = floor((1.0 - asinh(tan(latRad)) / PI) / 2 * tilesPerRowOrColumn).toInt()

  if (xTile < 0) {
    xTile = 0
  }
  if (xTile >= tilesPerRowOrColumn) {
    xTile = tilesPerRowOrColumn - 1
  }
  if (yTile < 0) {
    yTile = 0
  }
  if (yTile >= tilesPerRowOrColumn) {
    yTile = tilesPerRowOrColumn - 1
  }

  return TileIndex.of(xTile, yTile)
}

/**
 * Computes the longitude for the given tile-index and zoom
 */
fun computeLongitude(tileIndexX: SubIndex, zoom: Int): Longitude {
  return Longitude((tileIndexX.value.toDouble() / tilesPerRowOrColumn(zoom)) * 360.0 - 180.0)
}

/**
 * Computes the latitude for the given tile-index and zoom
 */
fun computeLatitude(tileIndexY: SubIndex, zoom: Int): Latitude {
  return Latitude(((PI - tileIndexY.value.toDouble() * 2.0 * PI / tilesPerRowOrColumn(zoom)).sinh()).atan() * 180.0 / PI)
}

/**
 * Converts the given longitude into a domain-relative value (horizontal)
 */
fun longitude2DomainRelative(longitude: Longitude): @DomainRelative Double {
  // While the width (longitude) in degrees is constant, given a zoom level, for all tiles, this
  // does not happen for the height. In general, tiles belonging to the same row have equal height
  // in degrees, but it decreases moving from the equator to the poles.
  // Thus, we may use a linear approach to compute the domain relative value for the longitude.
  return (longitude.value - LongitudeLeftEdge.value) / (LongitudeRightEdge.value - LongitudeLeftEdge.value)
}

/**
 * Converts a domain relative value (usually the x axis) to a longitude
 */
fun domainRelative2longitude(domainRelativeX: @DomainRelative Double): Longitude {
  return Longitude(domainRelativeX * (LongitudeRightEdge.value - LongitudeLeftEdge.value) + LongitudeLeftEdge.value)
}

/**
 * Returns the longitude in domain relative
 */
fun MapCoordinates.longitudeDomainRelative(): @DomainRelative Double {
  // While the width (longitude) in degrees is constant, given a zoom level, for all tiles, this
  // does not happen for the height. In general, tiles belonging to the same row have equal height
  // in degrees, but it decreases moving from the equator to the poles.
  // Thus, we may use a linear approach to compute the domain relative value for the longitude.
  return (longitude.value - LongitudeLeftEdge.value) / (LongitudeRightEdge.value - LongitudeLeftEdge.value)
}

fun MapCoordinates.latitudeDomainRelative(): @DomainRelative Double {
  // While the width (longitude) in degrees is constant, given a zoom level, for all tiles, this
  // does not happen for the height. In general, tiles belonging to the same row have equal height
  // in degrees, but it decreases moving from the equator to the poles.

  // asinh(tan(latitude.toRadians())) => [PI..-PI] for latitude in [LatitudeTopEdge..LatitudeBottomEdge]
  // asinh(tan(latitude.toRadians())) / (2 * PI) - 0.5 => [0..-1] for latitude in [LatitudeTopEdge..LatitudeBottomEdge]
  // -(asinh(tan(latitude.toRadians())) / (2 * PI) - 0.5) => [0..1] for latitude in [LatitudeTopEdge..LatitudeBottomEdge]
  return -(asinh(tan(latitude.value.coerceAtLeast(LatitudeBottomEdge.value).coerceAtMost(LatitudeTopEdge.value).toRadians())) / (2 * PI) - 0.5) // equivalent to -(ln(tan((45.0 + latitude * 0.5).toRadians())) / (2 * PI) - 0.5)
}

/**
 * Converts the given latitude into a domain-relative value (vertical)
 */
fun latitude2DomainRelative(latitude: Latitude): @DomainRelative Double {
  // While the width (longitude) in degrees is constant, given a zoom level, for all tiles, this
  // does not happen for the height. In general, tiles belonging to the same row have equal height
  // in degrees, but it decreases moving from the equator to the poles.

  // asinh(tan(latitude.toRadians())) => [PI..-PI] for latitude in [LatitudeTopEdge..LatitudeBottomEdge]
  // asinh(tan(latitude.toRadians())) / (2 * PI) - 0.5 => [0..-1] for latitude in [LatitudeTopEdge..LatitudeBottomEdge]
  // -(asinh(tan(latitude.toRadians())) / (2 * PI) - 0.5) => [0..1] for latitude in [LatitudeTopEdge..LatitudeBottomEdge]
  return -(asinh(tan(latitude.value.coerceAtLeast(LatitudeBottomEdge.value).coerceAtMost(LatitudeTopEdge.value).toRadians())) / (2 * PI) - 0.5) // equivalent to -(ln(tan((45.0 + latitude * 0.5).toRadians())) / (2 * PI) - 0.5)
}

/**
 * Converts a domain relative value (usually y value) to latitude
 */
fun domainRelative2latitude(domainRelativeY: @DomainRelative Double): Latitude {
  return Latitude(atan(sinh((-domainRelativeY + 0.5) * (2 * PI))).toDegrees())
}

/**
 * Computes the zoom parameter for slippy maps.
 *
 * The zoom parameter is an integer between 0 (zoomed out) and 18 (zoomed in).
 * 18 is normally the maximum, but some tile servers might go beyond that.
 * ```
 * | Zoom  |       Tile Coverage       |
 * |:-----:|:-------------------------:|
 * | 0     | 1 tile covers whole world |
 * | 1     | 2 x 2 tiles               |
 * | 2     | 4 x 4 tiles               |
 * | n     | 2^n x 2^n tiles           |
 * ```
 * [https://wiki.openstreetmap.org](https://wiki.openstreetmap.org/wiki/Slippy_map_tilenames#Zoom_levels)
 *
 * Note that this function assumes that [SlippyMapDefaultZoom] is the default zoom level!
 */
fun Zoom.toSlippyMapZoom(): Int {
  // Technically, the openstreetmap tile server supports zoom levels from 0 to 19

  // We consider only scaleY; 9 lies in the middle of 0..18 and is mapped to zoom 1.0
  var slippyMapZoom = SlippyMapDefaultZoom + log2(scaleY).roundToInt()

  if (slippyMapZoom < 0) {
    slippyMapZoom = 0
  }
  if (slippyMapZoom > 19) {
    slippyMapZoom = 19
  }
  return slippyMapZoom
}

/**
 * Converts the given slippy-map zoom level to a [Zoom] instance.
 *
 * Note that this function assumes that [SlippyMapDefaultZoom] is the default zoom level!
 */
fun toCanvasZoom(slippyMapZoom: Int): Zoom {
  val source = when {
    slippyMapZoom < 0  -> 0
    slippyMapZoom > 19 -> 19
    else               -> slippyMapZoom
  }

  val scale = 2.0.pow(source - SlippyMapDefaultZoom)
  return Zoom(scale, scale)
}

/**
 * Sets the minimal and maximal zoom so that zoom does not exceed the slippy map zoom levels
 *
 * [https://wiki.openstreetmap.org](https://wiki.openstreetmap.org/wiki/Slippy_map_tilenames#Zoom_levels)
 */
fun ZoomAndTranslationModifiersBuilder.withSlippyMapZoom(): ZoomAndTranslationModifiersBuilder {
  val minZoomFactor = 1.0 / 2.0.pow(9)
  minZoom(minZoomFactor, minZoomFactor)

  val maxZoomFactor = 1.0 * 2.0.pow(9)
  maxZoom(maxZoomFactor, maxZoomFactor)

  val delegate: ZoomAndTranslationModifier = current
  current = object : ZoomAndTranslationModifier {
    override fun modifyTranslation(translation: @Zoomed Distance, calculator: ChartCalculator): @Zoomed Distance {
      return delegate.modifyTranslation(translation, calculator)
    }

    override fun modifyZoom(zoom: Zoom, calculator: ChartCalculator): Zoom {
      return delegate.modifyZoom(Zoom(zoom.scaleX, zoom.scaleX), calculator)
    }
  }

  return this
}

/**
 * Computes the number of slippy map tiles in any direction for the given [zoom].
 *
 * This implies that the total number of tiles is [tilesPerRowOrColumn] x [tilesPerRowOrColumn].
 */
fun tilesPerRowOrColumn(zoom: Int): Int {
  return 1 shl zoom
}

/**
 * Ensures that this slippy map [TileIndex] is within valid bounds.
 *
 * Note:
 * * X goes from 0 (left edge is 180 °W) to 2^[zoom] − 1 (right edge is 180 °E)
 * * Y goes from 0 (top edge is 85.0511 °N) to 2^[zoom] − 1 (bottom edge is 85.0511 °S)
 *
 * @return a valid [TileIndex] that denotes the same latitude / longitude as this [TileIndex]
 */
fun TileIndex.ensureSlippyMapBounds(zoom: Int): TileIndex {
  val tileCount = tilesPerRowOrColumn(zoom)

  val x = xAsInt().wrapAround(tileCount)
  val y = yAsInt().wrapAround(tileCount)
  return TileIndex.of(x, y)
}


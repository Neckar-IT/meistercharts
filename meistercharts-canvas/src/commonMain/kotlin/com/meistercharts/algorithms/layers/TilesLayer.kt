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
package com.meistercharts.algorithms.layers

import com.meistercharts.algorithms.layers.slippymap.domainRelative2latitude
import com.meistercharts.algorithms.layers.slippymap.domainRelative2longitude
import com.meistercharts.algorithms.tile.CachedTileProvider
import com.meistercharts.algorithms.tile.Tile
import com.meistercharts.algorithms.tile.TileIdentifier
import com.meistercharts.algorithms.tile.TileIndex
import com.meistercharts.algorithms.tile.TileProvider
import com.meistercharts.annotations.ContentArea
import com.meistercharts.annotations.ContentAreaRelative
import com.meistercharts.annotations.Window
import com.meistercharts.annotations.Zoomed
import com.meistercharts.canvas.ChartSupport
import com.meistercharts.canvas.clipToContentViewport
import com.meistercharts.canvas.saved
import com.meistercharts.model.Coordinates
import com.meistercharts.model.MapCoordinates
import com.meistercharts.model.Size
import com.meistercharts.model.Zoom
import com.meistercharts.whatsat.ResultElementType
import com.meistercharts.whatsat.WhatsAtResultElement
import com.meistercharts.whatsat.WhatsAtSupport

/**
 * Paints [Tile]s provided by the given [TileProvider]
 */
class TilesLayer(
  /**
   * The tile provider that returns the tiles
   */
  val tileProvider: TileProvider
) : AbstractLayer() {

  override val type: LayerType
    get() = LayerType.Content

  override fun paintingVariables(): TilesLayerPaintingVariables {
    return paintingVariables
  }

  private val paintingVariables = object : TilesLayerPaintingVariables {
    /**
     * Coordinates of the upper left corner (of the content viewport)
     */
    override var contentUpperLeft: @ContentArea Coordinates = Coordinates.none

    /**
     * Coordinates of the lower right corner (of the content viewport)
     */
    override var contentLowerRight: @ContentArea Coordinates = Coordinates.none

    override var zoom: Zoom = Zoom.default
    override var tileSize: @Zoomed Size = Size.none

    override var tileIndexUpperLeft: TileIndex = TileIndex.Origin
    override var tileIndexLowerRight: TileIndex = TileIndex.Origin

    override fun calculate(paintingContext: LayerPaintingContext) {
      val chartCalculator = paintingContext.chartCalculator

      zoom = chartCalculator.chartState.zoom

      contentUpperLeft = chartCalculator.window2contentArea(chartCalculator.contentViewportMinX(), chartCalculator.contentViewportMinY())
      contentLowerRight = chartCalculator.window2contentArea(chartCalculator.contentViewportMaxX(), chartCalculator.contentViewportMaxY())

      tileSize = tileProvider.tileSize

      tileIndexUpperLeft = chartCalculator.contentArea2tileIndex(contentUpperLeft, tileSize)
      tileIndexLowerRight = chartCalculator.contentArea2tileIndex(contentLowerRight, tileSize)
    }
  }

  override fun paint(paintingContext: LayerPaintingContext) {
    val chartCalculator = paintingContext.chartCalculator
    val snapConfiguration = paintingContext.snapConfiguration

    val gc = paintingContext.gc
    gc.clipToContentViewport(chartCalculator)

    TileIndex.iterateOverTileIndices(paintingVariables.tileIndexUpperLeft, paintingVariables.tileIndexLowerRight) { mainX, subX, mainY, subY ->
      val tileIdentifier = TileIdentifier.of(paintingContext.chartId, mainX, subX, mainY, subY, paintingVariables.zoom)
      val tile: Tile = tileProvider.getTile(tileIdentifier) ?: return@iterateOverTileIndices

      gc.saved {
        @Window val tileOrigin = chartCalculator.tileIndex2window(tileIdentifier.tileIndex, tileProvider.tileSize)
        it.translate(snapConfiguration.snapXValue(tileOrigin.x), snapConfiguration.snapYValue(tileOrigin.y))
        tile.paint(it, paintingContext)
      }
    }
  }

  @Suppress("UNUSED_PARAMETER")
  fun whatsAt(where: @Window Coordinates, precision: WhatsAtSupport.Precision, chartSupport: ChartSupport): List<WhatsAtResultElement<*>> {
    val chartCalculator = chartSupport.chartCalculator

    val contentAreaX = chartCalculator.window2contentAreaX(where.x)
    val contentAreaY = chartCalculator.window2contentAreaY(where.y)

    @ContentAreaRelative val x = chartCalculator.contentArea2contentAreaRelativeX(contentAreaX)
    @ContentAreaRelative val y = chartCalculator.contentArea2contentAreaRelativeY(contentAreaY)

    val tileIndex = chartCalculator.contentArea2tileIndex(contentAreaX, contentAreaY, tileSize = paintingVariables.tileSize)

    val longitude = domainRelative2longitude(x)
    val latitude = domainRelative2latitude(y)

    val mapCoordinates = MapCoordinates(latitude, longitude)

    return listOf(
      WhatsAtResultElement(
        ResultElementType.mapCoordinates,
        label = mapCoordinates.toString(),
        data = mapCoordinates
      ),
      WhatsAtResultElement(
        ResultElementType.tileIndex,
        label = tileIndex.toString(),
        data = tileIndex
      ),
    )
  }
}

/**
 * Adds a [TilesLayer] to this [Layers]
 */
fun Layers.addTiles(tileProvider: TileProvider) {
  addLayer(TilesLayer(tileProvider))
}

/**
 * Clears the tiles cache.
 *
 * Attention: This method requires a [TilesLayer] containing a [CachedTileProvider].
 */
fun ChartSupport.clearTilesCache() {
  val tilesLayer = layerSupport.layers.byType<TilesLayer>() ?: throw IllegalStateException("No tiles layer found")
  (tilesLayer.tileProvider as CachedTileProvider).clear()
}

interface TilesLayerPaintingVariables : PaintingVariables {
  /**
   * Coordinates of the upper left corner
   */
  var contentUpperLeft: @ContentArea Coordinates

  /**
   * Coordinates of the lower right corner
   */
  var contentLowerRight: @ContentArea Coordinates
  var zoom: Zoom
  var tileSize: @Zoomed Size
  var tileIndexUpperLeft: TileIndex
  var tileIndexLowerRight: TileIndex
}

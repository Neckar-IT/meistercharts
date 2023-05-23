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

import com.meistercharts.algorithms.InternalCalculations
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.algorithms.tile.CachedTileProvider
import com.meistercharts.algorithms.tile.CanvasTile
import com.meistercharts.algorithms.tile.CountingTileProvider
import com.meistercharts.algorithms.tile.GlobalTilesCache
import com.meistercharts.algorithms.tile.HistoryCanvasTilePainter
import com.meistercharts.algorithms.tile.Tile
import com.meistercharts.algorithms.tile.TileIdentifier
import com.meistercharts.algorithms.tile.TileIndex
import com.meistercharts.algorithms.tile.TileProvider
import com.meistercharts.annotations.ContentArea
import com.meistercharts.annotations.Window
import com.meistercharts.annotations.Zoomed
import com.meistercharts.canvas.CanvasRenderingContext
import com.meistercharts.canvas.DebugConfiguration
import com.meistercharts.canvas.DebugFeature
import com.meistercharts.canvas.FontDescriptorFragment
import com.meistercharts.canvas.ConfigurationDsl
import com.meistercharts.canvas.paintTextBox
import com.meistercharts.canvas.saved
import com.meistercharts.model.Direction
import com.meistercharts.model.Rectangle
import com.meistercharts.model.Zoom
import it.neckar.open.formatting.formatUtc
import it.neckar.open.formatting.intFormat
import it.neckar.open.unit.other.px

/**
 * A layer that shows the bounds of the tiles of a [TilesLayer].
 *
 */
class TilesDebugLayer(
  /**
   * Returns the tile provider
   */
  val tileProviderProvider: (layers: Layers) -> TileProvider?,
  styleConfiguration: Style.() -> Unit = {}
) : AbstractLayer() {
  constructor(tilesLayer: TilesLayer) : this({
    tilesLayer.tileProvider
  })

  override val type: LayerType = LayerType.Content

  val style: Style = Style().also(styleConfiguration)

  override fun paint(paintingContext: LayerPaintingContext) {
    val gc = paintingContext.gc
    val chartCalculator = paintingContext.chartCalculator

    gc.fillText("Global Tiles Cache size: ${GlobalTilesCache.size}", 0.0, 0.0, Direction.TopLeft)

    val tileProvider = tileProviderProvider(paintingContext.chartSupport.layerSupport.layers) ?: return
    val cachedTileProvider = tileProvider as? CachedTileProvider


    @ContentArea val visibleTopLeft = chartCalculator.window2contentArea(0.0, 0.0)
    @ContentArea val visibleBottomRight = chartCalculator.window2contentArea(paintingContext.width, paintingContext.height)

    val zoom = chartCalculator.chartState.zoom
    @ContentArea val tileSizeInContentArea = chartCalculator.zoomed2contentArea(tileProvider.tileSize)

    val tileIndexVisibleTopLeft = InternalCalculations.calculateTileIndex(visibleTopLeft, tileSizeInContentArea)
    val tileIndexVisibleBottomRight = InternalCalculations.calculateTileIndex(visibleBottomRight, tileSizeInContentArea)

    val snapConfiguration = paintingContext.snapConfiguration

    for (x in tileIndexVisibleTopLeft.x..tileIndexVisibleBottomRight.x) {
      for (y in tileIndexVisibleTopLeft.y..tileIndexVisibleBottomRight.y) {
        val tileIdentifier = TileIdentifier.of(paintingContext.chartId, x, y, zoom)

        @Window val tileOrigin = chartCalculator.tileIndex2window(tileIdentifier.tileIndex, tileProvider.tileSize)

        @Window val x = snapConfiguration.snapXValue(tileOrigin.x)
        @Window val y = snapConfiguration.snapYValue(tileOrigin.y)
        @Zoomed val width = snapConfiguration.snapXSize(tileProvider.tileSize.width)
        @Zoomed val height = snapConfiguration.snapYSize(tileProvider.tileSize.height)

        @Window val tileRect = Rectangle(x, y, width, height)

        gc.stroke(style.tileBorderColor)
        gc.strokeRect(tileRect)

        if (style.showTileIndex) {
          gc.fill(style.tileIndexTextColor)
          gc.fillText(tileIdentifier.tileIndex.format(), tileRect.centerX, tileRect.centerY, Direction.Center)
        }

        val cachedTile = cachedTileProvider?.getTileFromCache(tileIdentifier) as? CanvasTile

        val countingTileProvider = when (tileProvider) {
          is CountingTileProvider -> tileProvider
          is CachedTileProvider -> tileProvider.delegate as? CountingTileProvider
          else -> null
        }

        if (cachedTile != null) {
          gc.font(FontDescriptorFragment.XS)

          cachedTile.creationInfo?.let { creationInfo ->
            gc.saved {
              val lines = buildList {
                add("Tile creation: ${creationInfo.creationTime.formatUtc()}")

                val age = paintingContext.frameTimestamp - creationInfo.creationTime
                add("Age: ${intFormat.format(age)} ms")

                countingTileProvider?.let {
                  add("Provide Count: ${it.providedCount(tileIdentifier)}")
                }

                val visibleTimeRange = creationInfo.get(HistoryCanvasTilePainter.visibleTimeRangeKey)
                visibleTimeRange?.let {
                  add("Visible Time Range:")
                  add("\t${it.start.formatUtc()}")
                  add("\t${it.end.formatUtc()}")
                }

                creationInfo.get(HistoryCanvasTilePainter.timeRangeToPaintKey)?.let {
                  val deltaString = if (visibleTimeRange != null) {
                    val deltaStart = it.start - visibleTimeRange.start
                    val deltaEnd = it.end - visibleTimeRange.end
                    "@start: $deltaStart ms, @end: +$deltaEnd ms"
                  } else {
                    ""
                  }

                  add("Time Range to paint $deltaString")
                  add("\t${it.start.formatUtc()}")
                  add("\t${it.end.formatUtc()}")
                }

                creationInfo.get(HistoryCanvasTilePainter.samplingPeriodKey)?.let {
                  add("Sampling Period:")
                  add("\t${it.label} (${it.distance} ms")
                }

                creationInfo.get(HistoryCanvasTilePainter.queryResultTimeRange)?.let {
                  add("Query Result Time Range:")
                  add("\t${it.start.formatUtc()}")
                  add("\t${it.end.formatUtc()}")
                }

                creationInfo.get(HistoryCanvasTilePainter.historyStorageBookKeepingStateKey)?.let {
                  add("Book Keeping Range:")
                  add("\t${it.start.formatUtc()}")
                  add("\t${it.end.formatUtc()}")
                }

                creationInfo.get(HistoryCanvasTilePainter.emptyReason)?.let {
                  add("Empty because: $it")
                }
              }

              gc.font(FontDescriptorFragment.XXS)
              gc.translate(x, y)
              gc.paintTextBox(lines, Direction.TopLeft)
            }
          }

        }
      }
    }

    if (cachedTileProvider != null) {
      paintCacheState(paintingContext, cachedTileProvider, zoom, tileIndexVisibleTopLeft, tileIndexVisibleBottomRight)
    }
  }

  private fun paintCacheState(
    paintingContext: LayerPaintingContext,
    cachedTileProvider: CachedTileProvider,
    currentZoom: Zoom,
    tileIndexVisibleTopLeft: TileIndex,
    tileIndexVisibleBottomRight: TileIndex
  ) {
    val cacheIconTileWidth = 5.0
    val cacheIconTileGap = 2.0

    val gc = paintingContext.gc
    val chartCalculator = paintingContext.chartCalculator


    val chartId = paintingContext.chartId
    val cachedTiles = GlobalTilesCache.tiles(chartId)
    val zoomGroups = cachedTiles.groupBy { it.identifier.zoom }


    val cachedTilesForCurrentZoom = zoomGroups[currentZoom]

    //Contains the tiles for the other zoom levels
    val otherZoomGroups = zoomGroups.filterKeys { it != currentZoom }


    val minX = cachedTiles.map {
      it.identifier.x
    }.minOrNull() ?: 0

    val minY = cachedTiles.map {
      it.identifier.y
    }.minOrNull() ?: 0

    val maxX = cachedTiles.map {
      it.identifier.x
    }.maxOrNull() ?: 0

    val maxY = cachedTiles.map {
      it.identifier.y
    }.maxOrNull() ?: 0


    //Paint the *other* zoom levels
    gc.saved {
      gc.translate(gc.width / 4.0, 0.0)

      @px val heightPerZoomGroup = gc.height / (otherZoomGroups.size)

      otherZoomGroups.keys.sortedBy { it.scaleY }.sortedBy { it.scaleX }.forEachIndexed { index, zoom ->
        gc.saved {
          gc.translate(0.0, heightPerZoomGroup * (index + 0.5))

          otherZoomGroups[zoom]?.let {
            visualizeTilesCache(gc, it, { Color.silver }, cacheIconTileGap, cacheIconTileWidth, zoom.format())
          }
        }
      }
    }

    //Paint the currently used zoom level
    gc.translateToCenter()
    if (cachedTilesForCurrentZoom != null) {
      visualizeTilesCache(gc, cachedTilesForCurrentZoom, { identifier: TileIdentifier ->
        when {
          identifier.x in tileIndexVisibleTopLeft.x..tileIndexVisibleBottomRight.x &&
            identifier.y in tileIndexVisibleTopLeft.y..tileIndexVisibleBottomRight.y -> {
            Color.blue
          }

          else -> Color.silver
        }
      }, cacheIconTileGap, cacheIconTileWidth, currentZoom.format())
    }
  }

  private fun visualizeTilesCache(
    gc: CanvasRenderingContext,
    tiles: List<Tile>,
    tileColorProvider: (identifier: TileIdentifier) -> Color,
    cacheIconTileGap: Double,
    cacheIconTileWidth: Double,
    label: String
  ) {
    gc.stroke(Color.rgba(255, 0, 0, 0.5))
    gc.strokeLine(0.0, -gc.width / 2.0, 0.0, gc.width / 2.0)
    gc.strokeLine(-gc.height / 2.0, 0.0, gc.height / 2.0, 0.0)

    tiles.forEach {
      val identifier = it.identifier

      gc.fill(tileColorProvider(identifier))
      gc.fillRect(identifier.x * (cacheIconTileGap + cacheIconTileWidth) + cacheIconTileGap / 2.0, identifier.y * (cacheIconTileGap + cacheIconTileWidth) + cacheIconTileGap / 2.0, cacheIconTileWidth, cacheIconTileWidth)
    }

    gc.stroke(Color.black)
    gc.strokeText(label, 0.0, 0.0, Direction.Center)
    gc.fill(Color.white)
    gc.fillText(label, 0.0, 0.0, Direction.Center)
  }

  @ConfigurationDsl
  class Style {
    /**
     * The color to be used for the border of a tile
     */
    var tileBorderColor: Color = Color.silver

    /**
     * The color to be used for the text that displays the index of a tile
     */
    var tileIndexTextColor: Color = Color.silver

    /**
     * If set to true the tile index is painted in the center of the tile
     */
    var showTileIndex: Boolean = true
  }

  companion object {
    /**
     * Returns a tiles debug layer that automatically returns the first tiles layer that has been added
     */
    fun automatic(): TilesDebugLayer {
      return TilesDebugLayer({ it.byType<TilesLayer>()?.tileProvider })
    }
  }
}


/**
 * Adds a tiles debug layer that is only visible if [DebugFeature.TilesDebug] is enabled
 */
fun Layers.addTilesDebugLayer(debug: DebugConfiguration) {
  addLayer(TilesDebugLayer.automatic().visibleIf { DebugFeature.TilesDebug.enabled(debug) })
}

fun Layers.addTilesDebugLayer(debug: () -> DebugConfiguration) {
  addLayer(TilesDebugLayer.automatic().visibleIf { DebugFeature.TilesDebug.enabled(debug()) })
}

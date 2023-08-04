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

import com.meistercharts.calc.InternalCalculations
import com.meistercharts.color.Color
import com.meistercharts.algorithms.tile.CachedTileProvider
import com.meistercharts.algorithms.tile.CanvasTile
import com.meistercharts.algorithms.tile.CountingTileProvider
import com.meistercharts.algorithms.tile.GlobalTilesCache
import com.meistercharts.algorithms.tile.HistoryCanvasTilePainter
import com.meistercharts.tile.MainIndex
import com.meistercharts.tile.SubIndex
import com.meistercharts.algorithms.tile.TileIdentifier
import com.meistercharts.tile.TileIndex
import com.meistercharts.algorithms.tile.TileProvider
import com.meistercharts.annotations.ContentArea
import com.meistercharts.annotations.Window
import com.meistercharts.annotations.Zoomed
import com.meistercharts.canvas.ConfigurationDsl
import com.meistercharts.canvas.DebugConfiguration
import com.meistercharts.canvas.DebugFeature
import com.meistercharts.font.FontDescriptorFragment
import com.meistercharts.canvas.paintTextBox
import com.meistercharts.canvas.saved
import it.neckar.geometry.Direction
import it.neckar.geometry.Rectangle
import it.neckar.open.formatting.formatUtc
import it.neckar.open.formatting.intFormat

/**
 * A layer that shows the bounds of the tiles of a [TilesLayer].
 *
 */
class TilesDebugLayer(
  /**
   * Returns the tile provider
   */
  val tileProviderProvider: (layers: Layers) -> TileProvider?,
  styleConfiguration: Style.() -> Unit = {},
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

    TileIndex.iterateOverTileIndices(tileIndexVisibleTopLeft, tileIndexVisibleBottomRight) { mainX: MainIndex, subX: SubIndex, mainY: MainIndex, subY: SubIndex ->
      val tileIndex = TileIndex(mainX, subX, mainY, subY)
      val tileIdentifier = TileIdentifier(paintingContext.chartId, tileIndex, zoom)

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

              creationInfo.get(HistoryCanvasTilePainter.queryResultTimeRangeKey)?.let {
                add("Query Result Time Range:")
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

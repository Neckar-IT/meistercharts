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

import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.Layers
import com.meistercharts.algorithms.layers.TilesLayer
import com.meistercharts.algorithms.painter.UrlPaintable
import com.meistercharts.algorithms.tile.CachedTileProvider
import com.meistercharts.algorithms.tile.Tile
import com.meistercharts.algorithms.tile.TileIdentifier
import com.meistercharts.algorithms.tile.TileProvider
import com.meistercharts.algorithms.tile.cached
import com.meistercharts.annotations.Window
import com.meistercharts.canvas.CanvasRenderingContext
import com.meistercharts.canvas.ConfigurationDsl
import com.meistercharts.font.FontDescriptorFragment
import com.meistercharts.canvas.whatsAt
import com.meistercharts.charts.ChartId
import it.neckar.geometry.Coordinates
import com.meistercharts.maps.calculateSlippyMapTileSize
import com.meistercharts.maps.computeLatitude
import com.meistercharts.maps.computeLongitude
import com.meistercharts.maps.ensureSlippyMapBounds
import com.meistercharts.maps.toSlippyMapZoom
import it.neckar.geometry.Direction
import it.neckar.geometry.Size
import com.meistercharts.style.Palette
import it.neckar.open.unit.other.deg
import it.neckar.open.unit.other.px
import kotlin.jvm.JvmOverloads

/**
 * Displays slippy map tiles
 *
 */
class SlippyMapLayer @JvmOverloads constructor(
  val configuration: Configuration,
  additionalConfiguration: Configuration.() -> Unit = {},
) : AbstractLayer() {

  constructor(
    chartId: ChartId,
    slippyMapProvider: SlippyMapProvider,
    additionalConfiguration: Configuration.() -> Unit = {},
  ): this(Configuration(chartId, slippyMapProvider), additionalConfiguration)

  init {
    configuration.additionalConfiguration()
  }

  override val type: LayerType
    get() = LayerType.Content

  // TODO use a slippy-map optimized cache. Such a cache should know that x and y always lie within 0 to 2^zoom - 1.
  // Furthermore there are 2^zoom x 2^zoom tiles at a certain zoom level. If we know which zoom level to target we
  // could choose a better cache size.
  val tileProvider: CachedTileProvider = MapTileProvider(configuration.slippyMapProvider, configuration).cached(configuration.chartId)

  /**
   * The tiles layer that itself paints the tiles
   */
  private val tilesLayer: TilesLayer = TilesLayer(
    tileProvider
  )

  override fun initialize(paintingContext: LayerPaintingContext) {
    super.initialize(paintingContext)

    paintingContext.chartSupport.whatsAt.registerResolverAsFirst { where: @Window Coordinates, precision, chartSupport ->
      tilesLayer.whatsAt(where, precision, chartSupport)
    }
  }

  override fun layout(paintingContext: LayerPaintingContext) {
    super.layout(paintingContext)
    tilesLayer.layout(paintingContext)
  }

  override fun paint(paintingContext: LayerPaintingContext) {
    tilesLayer.paint(paintingContext)
  }

  @ConfigurationDsl
  open class Configuration(
    val chartId: ChartId,
    var slippyMapProvider: SlippyMapProvider,
  ) {
    /**
     * Whether the values for latitude and longitude are painted on each tile
     */
    var showTileCoordinates: Boolean = false

    /**
     * Whether the url of each tile is painted
     */
    var showTileUrl: Boolean = false

    /**
     * Whether the index of each tile is painted
     */
    var showTileIndex: Boolean = false

    /**
     * Whether the border of each tile is painted
     */
    var showTileBorder: Boolean = false
  }
}

private class MapTileProvider(
  private val slippyMapProvider: SlippyMapProvider,
  private val configuration: SlippyMapLayer.Configuration,
) : TileProvider {

  override val tileSize: Size
    get() = calculateSlippyMapTileSize()

  override fun getTile(identifier: TileIdentifier): Tile {
    val slippyMapZoom = identifier.zoom.toSlippyMapZoom()

    val slippyMapTileIndex = identifier.tileIndex.ensureSlippyMapBounds(slippyMapZoom)

    val url = slippyMapProvider.url(slippyMapTileIndex, slippyMapZoom)
    val urlPaintable = UrlPaintable.naturalSize(url)

    return object : Tile {
      override val identifier: TileIdentifier
        get() = identifier

      override val tileSize: Size
        get() = this@MapTileProvider.tileSize

      override fun paint(gc: CanvasRenderingContext, paintingContext: LayerPaintingContext) {
        urlPaintable.paint(paintingContext, 0.0, 0.0)

        gc.fill(Palette.defaultGray)
        gc.font(FontDescriptorFragment(10.0))

        @px var y = 0.0
        if (configuration.showTileIndex) {
          gc.fillText("${slippyMapTileIndex.subX} / ${slippyMapTileIndex.subY}", 0.0, y, Direction.TopLeft, 5.0, 5.0)
          y += 14.0
        }
        if (configuration.showTileCoordinates) {
          @deg val latitude = computeLatitude(slippyMapTileIndex.subY, slippyMapZoom)
          @deg val longitude = computeLongitude(slippyMapTileIndex.subX, slippyMapZoom)

          gc.fillText("$latitude° / $longitude°", 0.0, y, Direction.TopLeft, 5.0, 5.0)
          y += 14.0
        }
        if (configuration.showTileUrl) {
          gc.fillText(url.value, 0.0, y, Direction.TopLeft, 5.0, 5.0)
          y += 14.0
        }
        if (configuration.showTileBorder) {
          gc.lineWidth = 1.0
          gc.stroke(Palette.defaultGray)
          gc.strokeRect(0.0, 0.0, tileSize.width, tileSize.height)
        }
      }
    }
  }
}

/**
 * Adds a background layer of the given color
 */
fun Layers.addSlippyMap(slippyMapProvider: SlippyMapProvider, configuration: SlippyMapLayer.Configuration.() -> Unit = {}): SlippyMapLayer {
  val slippyMapLayer = SlippyMapLayer(SlippyMapLayer.Configuration(chartId, slippyMapProvider), configuration)
  addLayer(slippyMapLayer)
  return slippyMapLayer
}



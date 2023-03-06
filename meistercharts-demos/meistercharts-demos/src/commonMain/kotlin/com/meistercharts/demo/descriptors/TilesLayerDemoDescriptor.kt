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
package com.meistercharts.demo.descriptors

import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.TilesLayer
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.algorithms.tile.DebugTileProvider
import com.meistercharts.algorithms.tile.Tile
import com.meistercharts.algorithms.tile.TileIdentifier
import com.meistercharts.algorithms.tile.TileProvider
import com.meistercharts.canvas.CanvasRenderingContext
import com.meistercharts.charts.ContentViewportGestalt
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableInsetsSeparate
import com.meistercharts.demo.configurableList
import com.meistercharts.model.Insets
import com.meistercharts.model.Size

/**
 */
class TilesLayerDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Tiles layer"
  override val description: String = "## A demo of the tiles layer"
  override val category: DemoCategory = DemoCategory.Layers

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      val possibleSizes = listOf(
        Size(200.0, 100.0),
        Size(100.0, 200.0),
        Size(100.0, 100.0),
        Size(101.0, 99.0)
      )

      meistercharts {
        val contentViewportGestalt = ContentViewportGestalt(Insets.all15)
        contentViewportGestalt.configure(this@meistercharts)

        configure {
          layers.addClearBackground()
          val tileProvider0 = DebugTileProvider(Size(200.0, 200.0))
          val tileProvider1 = FlatTileProvider()

          layers.addLayer(TilesLayer(tileProvider0))

          configurableList("tilesProvider", tileProvider0, listOf(tileProvider0, tileProvider1)) {
            converter = { tileProvider -> if (tileProvider == tileProvider0) "Debug" else "Flat" }

            onChange {
              layers.byType<TilesLayer>()?.let {
                layers.removeLayer(it)
              }

              layers.addLayer(TilesLayer(it))
              markAsDirty()
            }
          }

          configurableList("Tile Size", Size(200.0, 200.0), possibleSizes) {
            onChange {
              layers.byType<TilesLayer>()?.let {
                layers.removeLayer(it)
              }

              layers.addLayer(TilesLayer(DebugTileProvider(it)))
              tileProvider1.tileSize = it
              markAsDirty()
            }
          }

          configurableInsetsSeparate("Content Viewport", contentViewportGestalt::contentViewportMargin)
        }
      }
    }
  }
}

private class FlatTileProvider : TileProvider {
  override var tileSize: Size = Size.of(200.0, 200.0)

  override fun getTile(identifier: TileIdentifier): Tile {
    return object : Tile {
      override val identifier: TileIdentifier
        get() = identifier

      override val tileSize: Size
        get() = this@FlatTileProvider.tileSize

      override fun paint(gc: CanvasRenderingContext, paintingContext: LayerPaintingContext) {
        gc.fill(Color.darkgray)
        gc.fillRect(0.0, 0.0, tileSize.width, tileSize.height)
      }
    }
  }
}

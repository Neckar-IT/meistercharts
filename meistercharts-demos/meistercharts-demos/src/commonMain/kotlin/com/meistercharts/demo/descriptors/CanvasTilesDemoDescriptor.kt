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

import com.meistercharts.algorithms.layers.TilesLayer
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.tile.CachedTileProvider
import com.meistercharts.algorithms.tile.CanvasTileProvider
import com.meistercharts.algorithms.tile.DebugTilePainter
import com.meistercharts.algorithms.tile.TileProvider
import com.meistercharts.annotations.PhysicalPixel
import com.meistercharts.canvas.LayerSupport
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableList
import com.meistercharts.model.Size

/**
 */
class CanvasTilesDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Canvas Tiles layer"

  //language=HTML
  override val description: String = "## A demo of the canvas tiles layer"
  override val category: DemoCategory = DemoCategory.Layers

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    val key = "theKey"

    return ChartingDemo {
      val possibleSizes = listOf(
        Size(200.0, 100.0),
        Size(100.0, 200.0),
        Size(100.0, 100.0),
        Size(101.0, 99.0)
      )

      meistercharts {
        configure {
          layers.addClearBackground()

          layers.addLayer(TilesLayer(createTileProvider(Size(200.0, 200.0))))

          configurableList("Tile Size", Size(200.0, 200.0), possibleSizes) {
            onChange {
              //Remove the old tiles layer
              layers.removeAll { layer ->
                layer is TilesLayer
              }

              layers.addLayer(TilesLayer(createTileProvider(it)))
              markAsDirty()
            }
          }
        }
      }
    }
  }

  private fun LayerSupport.createTileProvider(physicalSize: @PhysicalPixel Size): TileProvider {
    val canvasTileProvider = CanvasTileProvider(physicalSize, DebugTilePainter())
    return CachedTileProvider(chartId, canvasTileProvider)
  }
}

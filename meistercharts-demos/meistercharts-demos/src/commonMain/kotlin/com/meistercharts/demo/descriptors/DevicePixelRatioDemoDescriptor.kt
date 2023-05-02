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
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.addTiles
import com.meistercharts.algorithms.layers.text.addText
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.algorithms.tile.CanvasTilePainter
import com.meistercharts.algorithms.tile.CanvasTileProvider
import com.meistercharts.algorithms.tile.DebugTileProvider
import com.meistercharts.algorithms.tile.TileCreationInfo
import com.meistercharts.algorithms.tile.TileIdentifier
import com.meistercharts.algorithms.tile.cached
import com.meistercharts.annotations.PhysicalPixel
import com.meistercharts.canvas.FontDescriptorFragment
import com.meistercharts.canvas.devicePixelRatio
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.model.Direction
import com.meistercharts.model.Size
import it.neckar.open.formatting.decimalFormat
import com.meistercharts.style.BoxStyle

class DevicePixelRatioDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Device Pixel Ratio"
  override val category: DemoCategory = DemoCategory.Calculations

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {
        configure {
          layers.addClearBackground()

          @PhysicalPixel val tileSize = Size(200.0, 200.0)
          layers.addTiles(DebugTileProvider(tileSize))

          val chartId = this@meistercharts.chartId
          val cachedTileProvider = CanvasTileProvider(tileSize, MyTilePainter()).cached(chartId)
          layers.addTiles(cachedTileProvider)

          layers.addText({ _, _ ->
            val size = chartSupport.canvas.size
            listOf(
              "devicePixelRatio: ${chartSupport.devicePixelRatio}",
              "Canvas size: ${decimalFormat.format(size.width)} / ${decimalFormat.format(size.height)}"
            )
          }) {
            boxStyle = BoxStyle(fill = Color.rgba(255, 255, 255, 0.9), borderColor = Color.gray)
            font = FontDescriptorFragment.DefaultSize
          }

          declare {
            button("Clear Cache") {
              cachedTileProvider.clear()
              markAsDirty()
            }
          }
        }
      }
    }
  }
}

private class MyTilePainter : CanvasTilePainter {
  override fun paint(identifier: TileIdentifier, paintingContext: LayerPaintingContext, tileSize: Size): TileCreationInfo {
    val gc = paintingContext.gc
    gc.stroke(Color.orange)
    gc.strokeRect(5.0, 5.0, gc.width - 10.0, gc.height - 10.0)

    gc.strokeOvalCenter(gc.center, Size.PX_120)

    gc.font(FontDescriptorFragment.DefaultSize)
    gc.fill(Color.black)
    gc.fillText("Size: ${tileSize.format()}", gc.center, Direction.BottomCenter, 5.0)
    gc.fillText("GC size: ${gc.canvasSize.format()}", gc.center, Direction.TopCenter, 5.0)
    return TileCreationInfo()
  }
}

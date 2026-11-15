/*
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


import kotlin.uuid.Uuid
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.addTiles
import com.meistercharts.algorithms.layers.text.addText
import com.meistercharts.color.Color
import com.meistercharts.algorithms.tile.CanvasTilePainter
import com.meistercharts.algorithms.tile.CanvasTileProvider
import com.meistercharts.algorithms.tile.DebugTileProvider
import com.meistercharts.algorithms.tile.TileCreationInfo
import com.meistercharts.algorithms.tile.TileIdentifier
import com.meistercharts.algorithms.tile.cached
import com.meistercharts.annotations.PhysicalPixel
import com.meistercharts.font.FontDescriptorFragment
import com.meistercharts.canvas.devicePixelRatio
import com.meistercharts.canvas.fill
import com.meistercharts.canvas.stroke
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.VariabilityType
import it.neckar.geometry.Direction
import it.neckar.geometry.Size
import it.neckar.open.formatting.decimalFormat
import com.meistercharts.style.BoxStyle
import it.neckar.open.kotlin.lang.asProvider

class DevicePixelRatioDemoDescriptor : MeisterchartsDemoDescriptor<Nothing> {
  override val uuid: Uuid = Uuid.parse("5d685ad4-83fc-4f4a-9981-a83bde6551ee")
  override val name: String = "Device Pixel Ratio"
  override val category: DemoCategory = DemoCategory.Calculations
  override val quality: DemoQuality = DemoQuality.High
  override val variabilityType: VariabilityType = VariabilityType.Stable

  override fun prepareDemo(configuration: PredefinedConfiguration<Nothing>?): MeisterchartsDemo {
    return MeisterchartsDemo(this) {
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
            boxStyle = BoxStyle(fill = Color.rgba(255, 255, 255, 0.9).asProvider(), borderColor = Color.gray)
            font = FontDescriptorFragment.DefaultSize.asProvider()
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

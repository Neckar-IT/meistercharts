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
package com.meistercharts.algorithms.tile

import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.canvas.CanvasRenderingContext
import com.meistercharts.canvas.FontDescriptorFragment
import com.meistercharts.canvas.devicePixelRatio
import com.meistercharts.model.Direction
import com.meistercharts.model.Size
import it.neckar.open.time.nowMillis
import it.neckar.open.formatting.CachedNumberFormat
import it.neckar.open.formatting.decimalFormat
import it.neckar.open.formatting.timeFormatWithMillis
import it.neckar.open.i18n.I18nConfiguration
import it.neckar.open.unit.other.px

/**
 * A [TileProvider] which provides light-weighted [Tile]s that paint tile-related debug information.
 */
class DebugTileProvider(
  override val tileSize: Size,
  style: Style.() -> Unit = {}
) : TileProvider {
  val style: Style = Style().also(style)

  val numberFormat: CachedNumberFormat = decimalFormat(3)

  override fun getTile(identifier: TileIdentifier): Tile? {
    return object : Tile {

      override val tileSize: Size
        get() = this@DebugTileProvider.tileSize

      //use nowMillis() in order to support tests that do not run in the context of a paint operation
      private val creationTime = timeFormatWithMillis.format(nowMillis(), I18nConfiguration.Germany)

      override val identifier: TileIdentifier
        get() = identifier

      override fun paint(gc: CanvasRenderingContext, paintingContext: LayerPaintingContext) {
        gc.stroke(style.borderColor)
        gc.lineWidth = 1.0
        gc.strokeRect(0.0, 0.0, tileSize.width, tileSize.height)

        //draw a inner rect (10px delta)
        gc.stroke(style.innerBorderColor)
        gc.strokeRect(10.0, 10.0, tileSize.width - 20.0, tileSize.height - 20.0)

        //stroke the diagonals
        gc.strokeLine(0.0, 0.0, tileSize.width, tileSize.height)
        gc.strokeLine(0.0, tileSize.height, tileSize.width, 0.0)

        gc.font(style.font)

        val rowHeight = gc.getFontMetrics().totalHeight * 1.7
        gc.fill(style.textColor)

        @px var y = 10.0
        gc.fillText("x/y=${identifier.tileIndex}", 10.0, y, Direction.TopLeft, 0.0, 0.0, tileSize.width - 20.0)
        y += rowHeight
        gc.fillText("zoom=${numberFormat.format(identifier.zoom.scaleX, paintingContext.i18nConfiguration)}/${numberFormat.format(identifier.zoom.scaleY, paintingContext.i18nConfiguration)}", 10.0, y, Direction.TopLeft, 0.0, 0.0, tileSize.width - 20.0)
        y += rowHeight
        gc.fillText("created=$creationTime", 10.0, y, Direction.TopLeft, 0.0, 0.0, tileSize.width - 20.0)
        y += rowHeight
        gc.fillText("size=${tileSize.format()}", 10.0, y, Direction.TopLeft, 0.0, 0.0, tileSize.width - 20.0)
        y += rowHeight

        val devicePixelRatio = paintingContext.chartSupport.devicePixelRatio
        gc.fillText("Physical size=${tileSize.times(devicePixelRatio, devicePixelRatio).format()}", 10.0, y, Direction.TopLeft, 0.0, 0.0, tileSize.width - 20.0)
      }
    }
  }

  open class Style(
    /**
     * The color to be used for the border of the tiles
     */
    var borderColor: Color = Color.lightgray,
    /**
     * The inner border (insets 10px)
     */
    var innerBorderColor: Color = Color.silver,

    /**
     * The color to be used for the debug text
     */
    var textColor: Color = Color.black,

    /**
     * The font to be used for the debug text
     */
    var font: FontDescriptorFragment = FontDescriptorFragment.empty
  )

}

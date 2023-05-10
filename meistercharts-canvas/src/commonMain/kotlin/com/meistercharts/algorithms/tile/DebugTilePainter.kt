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
import com.meistercharts.annotations.Zoomed
import com.meistercharts.canvas.FontDescriptorFragment
import com.meistercharts.model.Direction
import com.meistercharts.model.Size
import it.neckar.open.time.nowMillis
import it.neckar.open.formatting.dateTimeFormatWithMillis
import com.meistercharts.style.Palette.getChartColor
import it.neckar.open.unit.other.px
import it.neckar.logging.LoggerFactory
import it.neckar.logging.debug

/**
 * Paints debug information.
 */
class DebugTilePainter(
  styleConfiguration: Style.() -> Unit = {}
) : CanvasTilePainter {
  val style: Style = Style().also(styleConfiguration)

  override fun paint(identifier: TileIdentifier, paintingContext: LayerPaintingContext, tileSize: @Zoomed Size): TileCreationInfo {
    val gc = paintingContext.gc

    gc.fill(getChartColor(identifier.x + identifier.y * 100))
    gc.fillRect(0.0, 0.0, gc.width, gc.height)

    gc.stroke(Color.white)
    gc.lineWidth = 0.5
    gc.strokeOvalOrigin(0.0, 0.0, tileSize.width, tileSize.height)

    logger.debug { "GC size: ${gc.canvasSize}" }
    logger.debug { "tile size: $tileSize" }

    gc.stroke(style.borderColor)
    gc.lineWidth = 1.0
    gc.strokeRect(0.0, 0.0, gc.width, gc.height)

    gc.font(style.font)
    val rowHeight = gc.getFontMetrics().totalHeight * 1.7

    gc.fill(style.textColor)
    gc.font(style.font)
    @px var y = 10.0
    gc.fillText("x/y=${identifier.x}/${identifier.y}", 10.0, y, Direction.TopLeft)
    y += rowHeight
    gc.fillText("zoom=${identifier.zoom.scaleX}/${identifier.zoom.scaleY}", 10.0, y, Direction.TopLeft)
    y += rowHeight
    gc.fillText(dateTimeFormatWithMillis.format(nowMillis(), paintingContext.i18nConfiguration), 10.0, y, Direction.TopLeft)
    y += rowHeight
    gc.fillText("${gc.canvasSize}", 10.0, y, Direction.TopLeft)

    return TileCreationInfo()
  }

  open class Style(
    /**
     * The color to be used for the border
     */
    var borderColor: Color = Color.darkgray,
    /**
     * The color to be used for the text
     */
    var textColor: Color = Color.darkgray,
    /**
     * The font to be used for the text
     */
    var font: FontDescriptorFragment = FontDescriptorFragment.empty,
  )

  companion object {
    private val logger = LoggerFactory.getLogger("com.meistercharts.algorithms.tile.DebugTilePainter")
  }
}

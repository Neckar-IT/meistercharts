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

import com.meistercharts.algorithms.TimeRange
import com.meistercharts.algorithms.ValueRange
import com.meistercharts.algorithms.impl.FittingWithMargin
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.TilesLayer
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.addTimeAxis
import com.meistercharts.algorithms.layers.debug.ContentAreaDebugLayer
import com.meistercharts.algorithms.layers.tileCalculator
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.algorithms.tile.CanvasTilePainter
import com.meistercharts.algorithms.tile.CanvasTileProvider
import com.meistercharts.algorithms.tile.TileCreationInfo
import com.meistercharts.algorithms.tile.TileIdentifier
import com.meistercharts.annotations.ContentArea
import com.meistercharts.annotations.PhysicalPixel
import com.meistercharts.annotations.Zoomed
import com.meistercharts.canvas.paintTextBox
import com.meistercharts.canvas.saved
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.model.Coordinates
import com.meistercharts.model.Direction
import com.meistercharts.model.Insets
import com.meistercharts.model.Size
import it.neckar.open.formatting.dateTimeFormatWithMillis
import it.neckar.open.formatting.decimalFormat1digit
import it.neckar.open.formatting.percentageFormat
import com.meistercharts.style.BoxStyle

/**
 *
 */
class TileCalculatorDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Tile Calculator"

  override val category: DemoCategory = DemoCategory.Calculations

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {
        configureAsTimeChart()

        zoomAndTranslationDefaults {
          FittingWithMargin(Insets.of(50.0))
        }

        //The time range that spans the content area
        val contentAreaTimeRange = TimeRange.oneHourUntilNow()
        val valueRange = ValueRange.linear(-100.0, 100.0)

        configure {
          layers.addClearBackground()
          layers.addLayer(ContentAreaDebugLayer())

          //The size of the tile (physical size)
          @PhysicalPixel val tileSize = Size(800.0, 500.0)
          val tileProvider = CanvasTileProvider(tileSize, TileCalculatorDemoTilePainter(contentAreaTimeRange))
          layers.addLayer(TilesLayer(tileProvider))

          //layers.addLayer(ValueAxisLayer {
          //  valueRangeProvider = { valueRange }
          //  titleProvider = { _, _ -> "Sin" }
          //})
          layers.addTimeAxis(contentAreaTimeRange)
        }
      }

    }
  }
}

private class TileCalculatorDemoTilePainter(val contentAreaTimeRange: TimeRange) : CanvasTilePainter {
  override fun paint(identifier: TileIdentifier, paintingContext: LayerPaintingContext, tileSize: @Zoomed Size): TileCreationInfo {
    val gc = paintingContext.gc
    val calculator = paintingContext.tileCalculator(identifier.tileIndex, tileSize)

    gc.stroke(Color.blue)
    gc.strokeRect(Coordinates.origin, tileSize)

    @ContentArea val contentAreaOrigin = calculator.tile2contentArea(Coordinates.origin)
    @ContentArea val windowOrigin = calculator.tile2window(Coordinates.origin)

    val format = decimalFormat1digit

    gc.paintTextBox("Content Area: ${contentAreaOrigin.format(format)}", Direction.TopLeft, 0.0, 0.0, BoxStyle.gray, Color.red)
    gc.saved {
      gc.translate(0.0, 25.0)
      gc.paintTextBox("Window: ${windowOrigin.format(format)}", Direction.TopLeft, 0.0, 0.0, BoxStyle.gray, Color.green)
    }

    gc.saved {
      gc.translate(tileSize.width / 2.0, tileSize.height / 2.0)
      gc.paintTextBox("${identifier.tileIndex}", Direction.Center, 0.0, 0.0, BoxStyle.gray, Color.black)
      gc.paintTextBox("Tile Size: ${tileSize.format()}", Direction.TopCenter, 20.0, 20.0, BoxStyle.gray, Color.black)
    }

    @ContentArea val contentAreaBottomRight = calculator.tile2contentArea(tileSize.width, tileSize.height)
    @ContentArea val windowBottomRight = calculator.tile2window(tileSize.width, tileSize.height)
    gc.saved {
      gc.translate(tileSize.width, tileSize.height)
      gc.paintTextBox("Content Area: ${contentAreaBottomRight.format(format)}", Direction.BottomRight, 0.0, 0.0, BoxStyle.gray, Color.red)
      gc.translate(0.0, -25.0)
      gc.paintTextBox("Window: ${windowBottomRight.format(format)}", Direction.BottomRight, 0.0, 0.0, BoxStyle.gray, Color.green)
    }

    val contentAreaRelativeLeft = calculator.tile2contentAreaRelativeX(0.0)
    val contentAreaRelativeRight = calculator.tile2contentAreaRelativeX(tileSize.width)

    gc.saved {
      gc.translate(0.0, tileSize.height / 2.0 + 25.0)
      gc.paintTextBox(percentageFormat.format(contentAreaRelativeLeft), Direction.CenterLeft, 0.0, 0.0, BoxStyle.gray, Color.darkblue)
      gc.translate(tileSize.width, 0.0)
      gc.paintTextBox(percentageFormat.format(contentAreaRelativeRight), Direction.CenterRight, 0.0, 0.0, BoxStyle.gray, Color.darkblue)
    }


    val visibleTimeRange = calculator.visibleTimeRangeXinTile(contentAreaTimeRange)
    gc.saved {
      gc.translate(0.0, tileSize.height / 2.0)
      gc.paintTextBox(dateTimeFormatWithMillis.format(visibleTimeRange.start, paintingContext.i18nConfiguration), Direction.CenterLeft, 0.0, 0.0, BoxStyle.gray, Color.blue)
      gc.translate(tileSize.width, 0.0)
      gc.paintTextBox(dateTimeFormatWithMillis.format(visibleTimeRange.end, paintingContext.i18nConfiguration), Direction.CenterRight, 0.0, 0.0, BoxStyle.gray, Color.blue)
    }

    return TileCreationInfo()
  }
}

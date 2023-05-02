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
package com.meistercharts.demo.descriptors.history

import com.meistercharts.algorithms.TimeRange
import com.meistercharts.algorithms.ValueRange
import com.meistercharts.algorithms.impl.FittingWithMargin
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.TilesLayer
import com.meistercharts.algorithms.layers.ValueAxisLayer
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.addTimeAxis
import com.meistercharts.algorithms.layers.debug.ShowTimeRangeLayer
import com.meistercharts.algorithms.layers.tileCalculator
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.algorithms.tile.CanvasTilePainter
import com.meistercharts.algorithms.tile.CanvasTileProvider
import com.meistercharts.algorithms.tile.TileCreationInfo
import com.meistercharts.algorithms.tile.TileIdentifier
import com.meistercharts.algorithms.tile.cached
import com.meistercharts.annotations.Domain
import com.meistercharts.annotations.DomainRelative
import com.meistercharts.annotations.Tile
import com.meistercharts.annotations.TimeRelative
import com.meistercharts.annotations.Zoomed
import com.meistercharts.canvas.FontDescriptor
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.history.CachedRemoteHistoryStorage
import com.meistercharts.history.DecimalDataSeriesIndex
import com.meistercharts.history.HistoryBucket
import com.meistercharts.history.HistoryBucketDescriptor
import com.meistercharts.history.HistoryStorage
import com.meistercharts.history.SamplingPeriod
import com.meistercharts.history.TimestampIndex
import com.meistercharts.history.impl.MockSinusHistoryStorage
import com.meistercharts.history.AsyncHistoryAccess
import com.meistercharts.model.Direction
import com.meistercharts.model.Insets
import com.meistercharts.model.Size
import it.neckar.open.formatting.dateTimeFormatWithMillis
import it.neckar.open.formatting.timeFormatWithMillis
import com.meistercharts.style.Palette.getChartColor
import it.neckar.open.unit.si.ms
import it.neckar.logging.LoggerFactory

/**
 * Demos that visualizes the functionality of the FPS layer
 */
class HistoryLayerDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "History Layer - Tiles"
  override val description: String = "A history layer with tiles support"
  override val category: DemoCategory = DemoCategory.Calculations

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      val contentAreaTimeRange = TimeRange.oneMinuteUntilNow()

      meistercharts {
        configureAsTimeChart()
        configureAsTiledTimeChart()

        zoomAndTranslationDefaults {
          FittingWithMargin(Insets.of(50.0))
        }

        val historyStorage = MockSinusHistoryStorage()
        val valueRange = MockSinusHistoryStorage.valueRange

        logger.debug("Value range: $valueRange")
        logger.debug("Content area time range: ${contentAreaTimeRange.format()}")

        configure {
          val remoteHistoryStorage = CachedRemoteHistoryStorage { descriptor, consumer ->
            consumer(historyStorage.get(descriptor))
            markAsDirty()
          }

          layers.addClearBackground()

          //layers.addLayer(TilesLayer(HistoryTileProvider(historyAccess).cached(150)))

          val tilePainter = MyHistoryTilePainter(contentAreaTimeRange, valueRange, remoteHistoryStorage)
          val cachedTileProvider = CanvasTileProvider(Size.of(400.0, 400.0), tilePainter).cached(chartId)

          chartSupport.rootChartState.contentAreaSizeProperty.consume {
            cachedTileProvider.clear()
          }
          chartSupport.rootChartState.axisOrientationXProperty.consume {
            cachedTileProvider.clear()
          }
          chartSupport.rootChartState.axisOrientationYProperty.consume {
            cachedTileProvider.clear()
          }

          layers.addLayer(TilesLayer(cachedTileProvider))
          layers.addLayer(ShowTimeRangeLayer(contentAreaTimeRange))

          layers.addLayer(ValueAxisLayer("Sin", valueRange))
          layers.addTimeAxis(contentAreaTimeRange)
        }
      }
    }
  }

  companion object {
    private val logger = LoggerFactory.getLogger("com.meistercharts.demo.descriptors.history.HistoryLayerDemoDescriptor")
  }
}

private class MyHistoryTilePainter(val contentAreaTimeRange: TimeRange, val valueRange: ValueRange, val historyStorage: HistoryStorage) : CanvasTilePainter {
  override fun paint(identifier: TileIdentifier, paintingContext: LayerPaintingContext, tileSize: @Zoomed Size): TileCreationInfo {
    val gc = paintingContext.gc
    val layerSupport = paintingContext.layerSupport

    val calculator = paintingContext.tileCalculator(identifier.tileIndex, tileSize)

    @Tile val visibleTimeRange = calculator.visibleTimeRangeXinTile(contentAreaTimeRange)

    gc.stroke(getChartColor(identifier.x + identifier.y * 100))
    gc.strokeRect(0.0, 0.0, gc.width, gc.height)

    gc.font(FontDescriptor.Default)
    gc.fill(Color.black)
    gc.fillText(timeFormatWithMillis.format(visibleTimeRange.start, paintingContext.i18nConfiguration), 0.0, gc.centerY, Direction.CenterLeft, 5.0, 5.0)
    gc.fillText(timeFormatWithMillis.format(visibleTimeRange.end, paintingContext.i18nConfiguration), gc.width, gc.centerY, Direction.CenterRight, 5.0, 5.0)
    gc.fillText("Content Area Size: ${calculator.chartState.contentAreaSize}", gc.width / 2.0, 0.0, Direction.TopCenter, 5.0, 5.0)

    //calculate the ideal distance
    val idealTimestampCount = tileSize.width / 3.0
    @ms val idealDistance = visibleTimeRange.span / idealTimestampCount
    val resolution = SamplingPeriod.withMaxDistance(idealDistance)

    gc.fillText("idealTimestampCount: ${idealTimestampCount}", gc.centerX, gc.centerY, Direction.BottomCenter, 50.0, 50.0)
    gc.fillText("idealDistance: ${idealDistance} ms", gc.centerX, gc.centerY, Direction.BottomCenter, 25.0, 25.0)
    gc.fillText("resolution: ${resolution}", gc.centerX, gc.centerY, Direction.BottomCenter, 10.0, 10.0)

    val buckets = historyStorage.query(visibleTimeRange, resolution)
    gc.fillText("buckets.size(): ${buckets.size}", gc.centerX, gc.centerY, Direction.TopCenter, 40.0, 40.0)

    val timestampsCount = buckets.map {
      it.chunk.timeStampsCount
    }.sum()

    gc.fillText("timestamps count(): $timestampsCount", gc.centerX, gc.centerY, Direction.TopCenter, 60.0, 60.0)
    gc.fillText("now: ${dateTimeFormatWithMillis.format(paintingContext.frameTimestamp, paintingContext.i18nConfiguration)}", gc.centerX, gc.centerY, Direction.TopCenter, 80.0, 80.0)



    gc.beginPath()

    buckets.filter {
      it.overlaps(visibleTimeRange)
    }.forEach { bucket ->
      val chunk = bucket.chunk

      //Skip if there is no data in the chunk
      if (chunk.decimalDataSeriesCount == 0) {
        return@forEach
      }

      for (timeStampIndex in 0 until chunk.timeStampsCount) {
        val time = chunk.timestampCenter(TimestampIndex(timeStampIndex))

        if (!visibleTimeRange.contains(time)) {
          continue
        }

        @TimeRelative val timeRelative = contentAreaTimeRange.time2relative(time)
        @Tile val x = calculator.time2tileX(time, contentAreaTimeRange)

        @Domain val value = chunk.getDecimalValue(DecimalDataSeriesIndex(2), TimestampIndex(timeStampIndex))
        @DomainRelative val domainRelative = valueRange.toDomainRelative(value)
        @Tile val y = calculator.domainRelative2tileY(domainRelative)

        gc.lineTo(x, y)
      }
    }

    gc.lineWidth = 5.0
    gc.stroke(Color.red)
    gc.stroke()

    return TileCreationInfo()
  }
}

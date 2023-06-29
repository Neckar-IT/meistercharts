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

import com.meistercharts.calc.TileChartCalculator
import com.meistercharts.time.TimeRange
import com.meistercharts.model.ValueRange
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.PaintingPropertyKey
import com.meistercharts.algorithms.layers.retrieve
import com.meistercharts.algorithms.layers.tileCalculator
import com.meistercharts.color.Color
import com.meistercharts.annotations.ContentArea
import com.meistercharts.annotations.Tile
import com.meistercharts.canvas.DebugFeature
import com.meistercharts.canvas.FontDescriptorFragment
import com.meistercharts.canvas.currentFrameTimestamp
import com.meistercharts.canvas.fillRectCoordinates
import com.meistercharts.canvas.paintTextBox
import com.meistercharts.canvas.paintingProperties
import com.meistercharts.canvas.saved
import com.meistercharts.history.DecimalDataSeriesIndex
import com.meistercharts.history.DecimalDataSeriesIndexProvider
import com.meistercharts.history.HistoryBucket
import com.meistercharts.history.HistoryStorage
import com.meistercharts.history.SamplingPeriod
import com.meistercharts.history.fastForEach
import com.meistercharts.history.valueAt
import com.meistercharts.model.Direction
import com.meistercharts.model.Size
import com.meistercharts.provider.TimeRangeProvider
import it.neckar.open.provider.MultiProvider
import it.neckar.open.formatting.decimalFormat
import it.neckar.open.unit.si.ms

/**
 * Abstract base class for history canvas tile painters.
 */
abstract class HistoryCanvasTilePainter(private val configuration: Configuration) : CanvasTilePainter {
  override fun paint(identifier: TileIdentifier, paintingContext: LayerPaintingContext, tileSize: Size): TileCreationInfo {
    val gc = paintingContext.gc

    val tileCalculator = paintingContext.tileCalculator(identifier.tileIndex, tileSize)
    val contentAreaTimeRange = configuration.contentAreaTimeRange()

    @Tile val visibleTimeRange = tileCalculator.visibleTimeRangeXinTile(contentAreaTimeRange)

    //calculate the ideal sampling period
    val renderedSamplingPeriod = PaintingPropertyKey.SamplingPeriod.retrieve(paintingContext)

    //Expand the time range to avoid gaps between tiles
    @ms val minGapDistance = paintingContext.chartSupport.paintingProperties.retrieve(PaintingPropertyKey.MinGapDistance)
    @Tile val timeRangeToPaint = visibleTimeRange.extend(minGapDistance)

    val visibleDecimalSeriesIndices = configuration.visibleDecimalSeriesIndices()
    if (visibleDecimalSeriesIndices.isEmpty()) {
      return TileCreationInfo(
        isEmpty = true,
        values = mapOf(
          emptyReason to "No visible data series indices",
          visibleTimeRangeKey to visibleTimeRange,
          samplingPeriodKey to renderedSamplingPeriod,
          timeRangeToPaintKey to timeRangeToPaint,
        )
      )
    }

    val buckets = configuration.historyStorage.query(timeRangeToPaint, renderedSamplingPeriod)

    if (buckets.isEmpty()) {
      return TileCreationInfo(
        isEmpty = true,
        values = mapOf(
          emptyReason to "No buckets found",
          visibleTimeRangeKey to visibleTimeRange,
          samplingPeriodKey to renderedSamplingPeriod,
          timeRangeToPaintKey to timeRangeToPaint,
        )
      )
    }

    val dataSeriesCount = buckets.first().chunk.decimalDataSeriesCount

    //Iterate over data series first to avoid gaps between buckets
    visibleDecimalSeriesIndices.fastForEach { dataSeriesIndex: DecimalDataSeriesIndex ->
      if (dataSeriesIndex.value >= dataSeriesCount) {
        //the data series with the given index does not exist. Do not paint
        //Do not throw an exception here, because the defaults of the visible data series indices might contain data series that do not exist
        return@fastForEach
      }

      val valueRange = configuration.valueRanges.valueAt(dataSeriesIndex)

      paintDataSeries(
        paintingContext = paintingContext,
        dataSeriesIndex = dataSeriesIndex,
        buckets = buckets,
        timeRangeToPaint = timeRangeToPaint,
        minGapDistance = minGapDistance,
        valueRange = valueRange,
        tileCalculator = tileCalculator,
        contentAreaTimeRange = contentAreaTimeRange
      )
    }

    if (DebugFeature.TilesDebug.enabled(paintingContext)) {
      gc.saved {
        gc.font(FontDescriptorFragment.XS)

        gc.translateToCenter()
        gc.paintTextBox(
          listOf(
            "${identifier.tileIndex}",
            identifier.zoom.format(),
            renderedSamplingPeriod.label
          ),
          Direction.Center
        )
      }
    }

    if (DebugFeature.HistoryGaps.enabled(paintingContext)) {
      gc.font(FontDescriptorFragment.XS)

      @ms val start = buckets.first().chunk.firstTimeStamp()
      @ms val end = buckets.last().chunk.lastTimeStamp()

      if (start > visibleTimeRange.start) {
        val startX = tileCalculator.time2tileX(start, contentAreaTimeRange)
        gc.fill(Color.red)
        gc.fillRectCoordinates(0.0, 0.0, startX, gc.height)

        gc.fill(Color.black)
        gc.fillText("Delta Start: ${decimalFormat.format(visibleTimeRange.start - start)} ms", 0.0, gc.centerY, Direction.CenterLeft)
      }

      if (end < visibleTimeRange.end) {
        val endX = tileCalculator.time2tileX(end, contentAreaTimeRange)
        gc.fill(Color.red)
        gc.fillRectCoordinates(endX, 0.0, gc.width, gc.height)

        gc.fill(Color.black)
        gc.fillText("Delta End: ${decimalFormat.format(visibleTimeRange.end - end)} ms", gc.width, gc.centerY, Direction.CenterRight)
      }
    }

    return TileCreationInfo(
      currentFrameTimestamp,
      values = mapOf(
        visibleTimeRangeKey to visibleTimeRange,
        samplingPeriodKey to renderedSamplingPeriod,
        timeRangeToPaintKey to timeRangeToPaint,
        queryResultTimeRangeKey to TimeRange(buckets.first().start, buckets.last().end),
      )
    )
  }

  /**
   * Paints a single data series
   */
  protected abstract fun paintDataSeries(
    paintingContext: LayerPaintingContext,
    dataSeriesIndex: DecimalDataSeriesIndex,
    buckets: List<HistoryBucket>,
    timeRangeToPaint: TimeRange,
    minGapDistance: @ms Double,
    valueRange: @ContentArea ValueRange,
    tileCalculator: TileChartCalculator,
    contentAreaTimeRange: TimeRange,
  )

  open class Configuration(
    /**
     * Where the history is stored
     */
    val historyStorage: HistoryStorage,

    /**
     * Provides the time range of the content area
     */
    val contentAreaTimeRange: @ContentArea TimeRangeProvider,

    /**
     * Provides the value ranges of the content area for each line
     */
    val valueRanges: MultiProvider<DecimalDataSeriesIndex, @ContentArea ValueRange>,

    /**
     * Provides the indices of all visible data series.
     *
     * ATTENTION: This might contain indices that do *not* exist.
     * Therefore, it is necessary to check whether the data series for the given index does exist
     */
    val visibleDecimalSeriesIndices: () -> DecimalDataSeriesIndexProvider, //TODO replace with provider directly? Why provider of provider!
  )

  companion object {
    /**
     * Used to store the visible time range
     */
    val visibleTimeRangeKey: TileCreationInfoKey<TimeRange> = TileCreationInfoKey("visibleTimeRange")
    val samplingPeriodKey: TileCreationInfoKey<SamplingPeriod> = TileCreationInfoKey("samplingPeriod")
    val timeRangeToPaintKey: TileCreationInfoKey<TimeRange> = TileCreationInfoKey("timeRangeToPaint")
    val queryResultTimeRangeKey: TileCreationInfoKey<TimeRange> = TileCreationInfoKey("queryResultTimeRange")

    /**
     * The reason why a tile is empty
     */
    val emptyReason: TileCreationInfoKey<String> = TileCreationInfoKey("emptyReason")
  }

}

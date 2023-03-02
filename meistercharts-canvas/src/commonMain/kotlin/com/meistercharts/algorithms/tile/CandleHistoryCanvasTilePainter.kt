package com.meistercharts.algorithms.tile

import com.meistercharts.algorithms.TileChartCalculator
import com.meistercharts.algorithms.TimeRange
import com.meistercharts.algorithms.ValueRange
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.linechart.LineStyle
import com.meistercharts.annotations.ContentArea
import com.meistercharts.annotations.Domain
import com.meistercharts.annotations.DomainRelative
import com.meistercharts.annotations.Tile
import com.meistercharts.annotations.TimeRelative
import com.meistercharts.annotations.Zoomed
import com.meistercharts.history.DecimalDataSeriesIndex
import com.meistercharts.history.DecimalDataSeriesIndexProvider
import com.meistercharts.history.HistoryBucket
import com.meistercharts.history.HistoryStorage
import com.meistercharts.history.TimestampIndex
import com.meistercharts.history.impl.RecordingType
import com.meistercharts.painter.LinePainter
import com.meistercharts.provider.TimeRangeProvider
import it.neckar.open.collections.fastForEach
import it.neckar.open.provider.MultiProvider
import it.neckar.open.unit.si.ms

/**
 * Paints history related data on a tile.
 * Paints the average, min, max values as "candle"
 */
class CandleHistoryCanvasTilePainter(val configuration: Configuration) : HistoryCanvasTilePainter(configuration), CanvasTilePainter {
  override fun paintDataSeries(
    paintingContext: LayerPaintingContext,
    dataSeriesIndex: DecimalDataSeriesIndex,
    buckets: List<HistoryBucket>,
    timeRangeToPaint: TimeRange,
    minGapDistance: @ms Double,
    valueRange: @ContentArea ValueRange,
    tileCalculator: TileChartCalculator,
    contentAreaTimeRange: TimeRange,
  ) {
    val gc = paintingContext.gc
    val chartCalculator = paintingContext.chartCalculator

    //TODO???
    configuration.lineStyles.valueAt(dataSeriesIndex.value).apply(gc)

    //Paint all points for all buckets for one data series
    buckets.fastForEach { bucket ->
      val chunk = bucket.chunk

      val samplingPeriod = bucket.samplingPeriod

      @TimeRelative val distance = contentAreaTimeRange.time2relativeDelta(samplingPeriod.distance)
      @Zoomed val candleWidth = chartCalculator.domainRelativeDelta2ZoomedX(distance) - 1.0

      for (timestampIndexAsInt in 0 until chunk.timeStampsCount) {
        val timestampIndex = TimestampIndex(timestampIndexAsInt)
        @ms val time = chunk.timestampCenter(timestampIndex)

        if (time < timeRangeToPaint.start) {
          //Skip all data points that are not visible on this tile yet
          continue
        }
        if (time > timeRangeToPaint.end) {
          //Skip all data points that are no longer visible on this tile
          break
        }

        @Domain val value = chunk.getDecimalValue(dataSeriesIndex, timestampIndex)
        if (!value.isFinite()) {
          //NaN --> explicit gap!
          continue
        }

        @Tile val x = tileCalculator.time2tileX(time, contentAreaTimeRange)

        //Paint the candle rect first (if there are min/max values)
        if (chunk.recordingType == RecordingType.Calculated) {
          @DomainRelative val maxDomainRelative = valueRange.toDomainRelative(chunk.getMax(dataSeriesIndex, timestampIndex))
          val maxY = tileCalculator.domainRelative2tileY(maxDomainRelative)

          @DomainRelative val minDomainRelative = valueRange.toDomainRelative(chunk.getMin(dataSeriesIndex, timestampIndex))
          val minY = tileCalculator.domainRelative2tileY(minDomainRelative)

          //Vertical rect (candle)
          configuration.lineStyles.valueAt(dataSeriesIndex.value).apply(gc)
          gc.strokeLine(x + candleWidth / 2.0, maxY, x + candleWidth / 2.0, minY)

          //stroke max
          gc.strokeLine(x, maxY, x + candleWidth, maxY)

          //stroke min
          gc.strokeLine(x, minY, x + candleWidth, minY)
        }

        //Paint the value itself
        @DomainRelative val valueDomainRelative = valueRange.toDomainRelative(value)
        @Tile val valueY = tileCalculator.domainRelative2tileY(valueDomainRelative)

        configuration.lineStyles.valueAt(dataSeriesIndex.value).apply(gc)
        gc.strokeLine(x, valueY, x + candleWidth, valueY)
      }
    }
  }

  class Configuration(
    historyStorage: HistoryStorage,
    contentAreaTimeRange: @ContentArea TimeRangeProvider,
    valueRanges: MultiProvider<DecimalDataSeriesIndex, @ContentArea ValueRange>,
    visibleDecimalSeriesIndices: () -> DecimalDataSeriesIndexProvider,

    /**
     * Provides a width for each line
     */
    val lineStyles: MultiProvider<DecimalDataSeriesIndex, LineStyle>,

    /**
     * Provides a painter for each line
     */
    val linePainters: MultiProvider<DecimalDataSeriesIndex, LinePainter>,

    ) : HistoryCanvasTilePainter.Configuration(historyStorage, contentAreaTimeRange, valueRanges, visibleDecimalSeriesIndices)
}

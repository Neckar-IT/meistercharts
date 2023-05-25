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

import com.meistercharts.algorithms.TileChartCalculator
import com.meistercharts.algorithms.TimeRange
import com.meistercharts.algorithms.ValueRange
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.linechart.LineStyle
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.annotations.ContentArea
import com.meistercharts.annotations.Domain
import com.meistercharts.annotations.DomainRelative
import com.meistercharts.annotations.Tile
import com.meistercharts.canvas.DebugFeature
import com.meistercharts.canvas.layout.cache.CoordinatesCache
import com.meistercharts.history.DecimalDataSeriesIndex
import com.meistercharts.history.DecimalDataSeriesIndexProvider
import com.meistercharts.history.HistoryBucket
import com.meistercharts.history.HistoryStorage
import com.meistercharts.history.TimestampIndex
import com.meistercharts.history.valueAt
import com.meistercharts.painter.AreaBetweenLinesPainter
import com.meistercharts.painter.LinePainter
import com.meistercharts.painter.PointPainter
import com.meistercharts.provider.TimeRangeProvider
import it.neckar.open.collections.fastForEach
import it.neckar.open.provider.MultiProvider
import it.neckar.open.unit.si.ms

/**
 * Paints history related data on a tile.
 * Paints the average(s) as line
 */
class AverageMinMaxHistoryCanvasTilePainter(val configuration: Configuration) : HistoryCanvasTilePainter(configuration), CanvasTilePainter {
  /**
   * Used to store the locations of the points to paint them later
   */
  private val pointsCache = CoordinatesCache()

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
    if (valueRange.isEmpty()) {
      //Skip painting with empty value range
      return
    }

    val gc = paintingContext.gc

    //The point painter - that *might* be used later
    val pointPainter = configuration.pointPainters.valueAt(dataSeriesIndex.value)
    pointsCache.prepare(0) //prepare with empty values

    val averageLineStyle = configuration.averageLineStyles.valueAt(dataSeriesIndex)
    averageLineStyle.apply(gc)

    val averageLinePainter = configuration.averageLinePainters.valueAt(dataSeriesIndex.value)

    val minMaxAreaPainter: AreaBetweenLinesPainter? = configuration.minMaxAreaPainters.valueAt(dataSeriesIndex)
    val minMaxAreaColor = configuration.minMaxAreaColors.valueAt(dataSeriesIndex)

    minMaxAreaPainter?.begin(gc)
    averageLinePainter.begin(gc)

    //The time of the last data point. Is used to identify gaps
    @ms var lastTime = Double.NaN //initialize with NaN to ensure first one is no gap

    //Paint all points for all buckets for one data series
    buckets.fastForEach { bucket ->
      val chunk = bucket.chunk
      val paintMinMaxArea = minMaxAreaPainter != null && chunk.hasDecimalMinMaxValues()

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

        //Check if there is a gap because the distance between data points is larger than the gap size
        @ms val distanceToLastDataPoint = time - lastTime
        if (distanceToLastDataPoint > minGapDistance) {
          //We have a gap -> finish the current line and begin a new one
          if (paintMinMaxArea) {
            gc.fill(minMaxAreaColor)
            minMaxAreaPainter?.paint(gc, false)
          }

          //We have a gap -> finish the current line and begin a new one
          averageLineStyle.apply(gc)
          averageLinePainter.paint(gc)
          averageLinePainter.begin(gc)

          minMaxAreaPainter?.begin(gc)
        }

        //update the last time stuff
        lastTime = time


        @Domain val value = chunk.getDecimalValue(dataSeriesIndex, timestampIndex)
        @DomainRelative val domainRelative = valueRange.toDomainRelative(value)

        @Domain val maxValue = chunk.getMax(dataSeriesIndex, timestampIndex)
        @DomainRelative val maxDomainRelative = valueRange.toDomainRelative(maxValue)

        @Domain val minValue = chunk.getMin(dataSeriesIndex, timestampIndex)
        @DomainRelative val minDomainRelative = valueRange.toDomainRelative(minValue)

        //The current value is NaN - this is an *explicit* gap
        if (value.isFinite().not() || domainRelative.isFinite().not()) {
          if (paintMinMaxArea) {
            gc.fill(minMaxAreaColor)
            minMaxAreaPainter?.paint(gc, false)
          }

          averageLineStyle.apply(gc)
          averageLinePainter.paint(gc)

          minMaxAreaPainter?.begin(gc)
          averageLinePainter.begin(gc)
          continue
        }

        @Tile val x = tileCalculator.time2tileX(time, contentAreaTimeRange)
        @Tile val y = tileCalculator.domainRelative2tileY(domainRelative)
        @Tile val minValueTile = tileCalculator.domainRelative2tileY(minDomainRelative)
        @Tile val maxValueTile = tileCalculator.domainRelative2tileY(maxDomainRelative)

        averageLinePainter.addCoordinates(gc, x, y)
        pointsCache.add(x, y)
        if (paintMinMaxArea) {
          minMaxAreaPainter?.addCoordinates(gc, x, minValueTile, maxValueTile)
        }

        //min / max if available
        if (DebugFeature.ShowMinMax.enabled(paintingContext)) {
          if (chunk.hasDecimalMinMaxValues()) {
            @Domain val maxValue = chunk.getMax(dataSeriesIndex, timestampIndex)
            @DomainRelative val domainRelativeMax = valueRange.toDomainRelative(maxValue)
            val maxY = tileCalculator.domainRelative2tileY(domainRelativeMax)
            gc.stroke(Color.blue)
            gc.strokeLine(x - 3, maxY, x + 3, maxY)

            @Domain val minValue = chunk.getMin(dataSeriesIndex, timestampIndex)
            @DomainRelative val domainRelativeMin = valueRange.toDomainRelative(minValue)
            val minY = tileCalculator.domainRelative2tileY(domainRelativeMin)
            gc.stroke(Color.red)
            gc.strokeLine(x - 3, minY, x + 3, minY)

            gc.stroke(Color.gray)
            gc.strokeLine(x, minY, x, maxY)
          }
        }
      }
    }

    gc.fill(minMaxAreaColor)
    minMaxAreaPainter?.paint(gc, false)

    averageLineStyle.apply(gc)
    averageLinePainter.paint(gc)

    //Paint the points
    if (pointPainter != null) {
      pointsCache.fastForEachIndexed { _, x, y ->
        pointPainter.paintPoint(gc, x, y)
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
    val averageLineStyles: MultiProvider<DecimalDataSeriesIndex, LineStyle>,

    /**
     * Provides a painter for each line
     */
    val averageLinePainters: MultiProvider<DecimalDataSeriesIndex, LinePainter>,

    /**
     * The (optional) point painters
     */
    var pointPainters: MultiProvider<DecimalDataSeriesIndex, PointPainter?> = MultiProvider.alwaysNull(),

    /**
     * Provides the area painters for the min/max area
     */
    val minMaxAreaPainters: MultiProvider<DecimalDataSeriesIndex, AreaBetweenLinesPainter?> = MultiProvider.alwaysNull(),

    /**
     * Returns the area color for min/max areas
     */
    val minMaxAreaColors: MultiProvider<DecimalDataSeriesIndex, Color> = MultiProvider.always(Color.lightgray),

    ) : HistoryCanvasTilePainter.Configuration(historyStorage, contentAreaTimeRange, valueRanges, visibleDecimalSeriesIndices)
}

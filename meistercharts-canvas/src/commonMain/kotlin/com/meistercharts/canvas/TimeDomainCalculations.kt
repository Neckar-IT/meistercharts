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
package com.meistercharts.canvas

import com.meistercharts.algorithms.ChartCalculator
import com.meistercharts.algorithms.TileChartCalculator
import com.meistercharts.algorithms.TimeRange
import com.meistercharts.algorithms.layers.tileCalculator
import com.meistercharts.algorithms.tile.TileIdentifier
import com.meistercharts.annotations.ContentArea
import com.meistercharts.annotations.ContentAreaRelative
import com.meistercharts.annotations.Domain
import com.meistercharts.annotations.DomainRelative
import com.meistercharts.annotations.TileRelative
import com.meistercharts.annotations.WindowRelative
import com.meistercharts.annotations.Zoomed
import com.meistercharts.model.Size
import it.neckar.open.unit.other.pct
import it.neckar.open.unit.other.px
import it.neckar.open.unit.si.ms

/**
 * Contains methods related to time and domain calculations (especially related to tiles)
 */


/**
 * Computes the [TimeRange] displayed by the tile with the given [identifier] regarding the state hold by [layerSupport]
 */
@Deprecated("untested!")
@Domain
fun TileIdentifier.visibleTimeRange(
  chartSupport: ChartSupport,
  /**
   * The size of a tile
   */
  tileSize: @px Size,
  timeRange: TimeRange
): TimeRange {
  val calculator = chartSupport.tileCalculator(tileIndex, tileSize)
  return calculator.visibleTimeRangeXinTile(timeRange)
}

/**
 * Returns the time range for a domain relative range (on the x axis)
 */
@Deprecated("untested!")
fun ChartCalculator.domainRelative2TimeRangeX(
  /**
   * The start of the range (lower value)
   */
  start: @DomainRelative Double,
  /**
   * The end of the range (higher value)
   */
  end: @DomainRelative Double,
  timeRange: TimeRange
): TimeRange {
  return contentAreaRelative2TimeRangeX(
    domainRelative2contentAreaRelativeX(start),
    domainRelative2contentAreaRelativeX(end),
    timeRange
  )
}

/**
 * Returns the time range for a content are position (on the x axis)
 */
@Deprecated("untested!")
fun ChartCalculator.contentArea2TimeRangeX(start: @ContentArea Double, end: @ContentArea Double, contentAreaTimeRange: TimeRange): TimeRange {
  return contentAreaRelative2TimeRangeX(
    contentArea2contentAreaRelativeX(start),
    contentArea2contentAreaRelativeX(end),
    contentAreaTimeRange
  )
}

/**
 * Returns the time range for the start/end in content area relative
 */
@Deprecated("untested!")
fun ChartCalculator.contentAreaRelative2TimeRangeX(start: @ContentAreaRelative Double, end: @ContentAreaRelative Double, timeRange: TimeRange): TimeRange {
  @DomainRelative val startDomainRelative = contentAreaRelative2domainRelativeX(start)
  @DomainRelative val toDomainRelative = contentAreaRelative2domainRelativeX(end)

  return TimeRange(timeRange.relative2time(startDomainRelative), timeRange.relative2time(toDomainRelative))
}

/**
 * Returns the time value for a tile relative value
 */
fun TileChartCalculator.tileRelative2TimeX(tileRelative: @TileRelative Double, contentAreaTimeRange: TimeRange): @ms Double {
  //Calculate the tile width relative to the content area
  @ContentAreaRelative val contentAreaRelativeTileWidth = contentArea2contentAreaRelativeX(tileWidth * tileRelative)
  return tileOrigin2timeX(contentAreaTimeRange) + contentAreaTimeRange.relative2timeDelta(contentAreaRelativeTileWidth)
}


/**
 * sets the canvas window translation so that the provided [timeStamp] lies at the provided [positionInWindow]
 * @param timeStamp : The timeStamp to scroll to
 * @param positionInWindow : The position in the window (0.0 - 1.0) where the timeStamp is being plotted
 * @param contentAreaTimeRange The time range that is visualized in the content area (0.0..1.0)
 */
@Deprecated("untested!")
fun ChartSupport.scrollToTimestamp(@ms @Domain timeStamp: Double, @pct @WindowRelative positionInWindow: Double, contentAreaTimeRange: TimeRange) {
  val chartCalculator = zoomAndTranslationSupport.chartCalculator
  @Zoomed @px val dataPointX = chartCalculator.domainRelative2zoomedX(contentAreaTimeRange.time2relative(timeStamp))
  zoomAndTranslationSupport.setWindowTranslationX(-dataPointX + chartCalculator.windowRelative2WindowX(positionInWindow))
}

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
package com.meistercharts.charts.timeline

import com.meistercharts.calc.ChartCalculator
import com.meistercharts.time.TimeRange
import com.meistercharts.zoom.MoveDomainValueToLocation
import com.meistercharts.zoom.ZoomAndTranslationDefaults
import com.meistercharts.annotations.WindowRelative
import com.meistercharts.model.Zoom
import it.neckar.open.provider.DoubleProvider
import it.neckar.open.unit.si.ms

/**
 * Moves a time under the cross wire
 */
class MoveTimeUnderCrossWire(
  /**
   * Provides the youngest time
   */
  youngestTimeProvider: YoungestTimeProvider,
  /**
   * Provides the content area time range
   */
  timeRange: () -> TimeRange,

  crossWirePositionX: @WindowRelative DoubleProvider,
) :
  MoveDomainValueToLocation(
    defaultZoomProvider = {
      Zoom.default
    },
    domainRelativeValueProvider = { _: ChartCalculator ->
      @ms val relevantTimestamp = youngestTimeProvider()
      timeRange().time2relative(relevantTimestamp)
    },

    targetLocationProvider = { chartCalculator ->
      chartCalculator.windowRelative2WindowX(crossWirePositionX())
    },
  ),
  ZoomAndTranslationDefaults {
  companion object {
    /**
     * Creates a new [MoveTimeUnderCrossWire] instance for the [TimeLineChartGestalt]
     */
    fun create(timeLineChartGestalt: TimeLineChartGestalt): MoveTimeUnderCrossWire {
      val youngestTimeProvider = YoungestTimeProvider(timeLineChartGestalt.configuration.historyStorage)
      return MoveTimeUnderCrossWire(youngestTimeProvider, timeLineChartGestalt.configuration::contentAreaTimeRange, timeLineChartGestalt.configuration::crossWirePositionX)
    }
  }
}


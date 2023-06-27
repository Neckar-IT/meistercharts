package com.meistercharts.charts.timeline

import com.meistercharts.algorithms.ChartCalculator
import com.meistercharts.algorithms.TimeRange
import com.meistercharts.algorithms.impl.MoveDomainValueToLocation
import com.meistercharts.algorithms.impl.ZoomAndTranslationDefaults
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
      val youngestTimeProvider = YoungestTimeProvider(timeLineChartGestalt.data.historyStorage)
      return MoveTimeUnderCrossWire(youngestTimeProvider, timeLineChartGestalt.style::contentAreaTimeRange, timeLineChartGestalt.style::crossWirePositionX)
    }
  }
}


package com.meistercharts.algorithms

import com.meistercharts.annotations.Window
import com.meistercharts.annotations.Zoomed
import it.neckar.open.unit.other.px
import it.neckar.open.unit.quantity.Time
import it.neckar.open.unit.si.ms

/**
 * Offers calculations related to times and time ranges
 */
class TimeChartCalculator(
  /**
   * The chart state
   */
  chartState: ChartState,
  /**
   * The content area time range
   */
  val contentAreaTimeRangeX: TimeRange,
) : ChartCalculator(chartState) {

  fun window2timeX(@px @Window value: Double): @Time @ms Double {
    return window2timeX(value, contentAreaTimeRange = contentAreaTimeRangeX)
  }

  fun zoomed2timeDeltaX(@Zoomed @px x: Double): @Time Double {
    return zoomed2timeDeltaX(x, contentAreaTimeRangeX)
  }

  fun time2windowX(@Time @ms time: Double): @px @Window Double {
    return time2windowX(time, contentAreaTimeRangeX)
  }

  fun visibleTimeRangeXinWindow(): TimeRange {
    return visibleTimeRangeXinWindow(contentAreaTimeRangeX)
  }
}


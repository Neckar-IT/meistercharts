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


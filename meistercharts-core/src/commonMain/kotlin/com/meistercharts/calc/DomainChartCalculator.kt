/*
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
package com.meistercharts.calc

import com.meistercharts.annotations.Domain
import com.meistercharts.annotations.Window
import com.meistercharts.annotations.Zoomed
import com.meistercharts.range.LinearValueRange
import com.meistercharts.state.ChartState
import it.neckar.geometry.Coordinates
import it.neckar.geometry.Distance
import it.neckar.open.unit.other.px
import it.neckar.open.unit.si.ms

/**
 * Offers calculations related to domain values.
 *
 * Currently, this calculator requires a [LinearValueRange].
 */
class DomainChartCalculator(
  /**
   * The chart state
   */
  chartState: ChartState,

  /**
   * The range of horizontal domain values
   */
  val domainRangeX: LinearValueRange,
  /**
   * The vertical range of domain values
   */
  val domainRangeY: LinearValueRange,

  ) : ChartCalculator(chartState) {

  fun window2domainX(@px @Window value: Double): @Domain Double {
    return window2domainX(value, domainRangeX)
  }

  fun zoomed2domainDeltaX(@Zoomed @px x: Double): @Domain Double {
    return zoomed2domainDeltaX(x, domainRangeX)
  }

  fun domain2windowX(@Domain @ms domain: Double): @px @Window Double {
    return domain2windowX(domain, domainRangeX)
  }

  fun domain2window(@Domain @ms domain: Coordinates): @px @Window Coordinates {
    return domain2window(domain, domainRangeX, domainRangeY)
  }

  fun domain2contentAreaX(@Domain @ms domain: Double): @px @Window Double {
    return domain2contentAreaX(domain, domainRangeX)
  }

  fun visibleDomainRangeXinWindow(): LinearValueRange {
    return visibleDomainRangeXinWindow(domainRangeX)
  }

  fun window2domainY(@px @Window value: Double): @Domain Double {
    return window2domainY(value, domainRangeY)
  }

  fun window2domain(@px @Window value: Coordinates): @Domain Coordinates {
    return window2domain(value, domainRangeX, domainRangeY)
  }

  fun zoomed2domainDeltaY(@Zoomed @px x: Double): @Domain Double {
    return zoomed2domainDeltaY(x, domainRangeY)
  }

  fun domain2windowY(@Domain @ms domain: Double): @px @Window Double {
    return domain2windowY(domain, domainRangeY)
  }

  fun zoomed2domainDelta(@Zoomed @px coordinates: Coordinates): @Domain Coordinates {
    return zoomed2domainDelta(coordinates, domainRangeX, domainRangeY)
  }

  fun zoomed2domainDelta(@Zoomed @px distance: Distance): @Domain Coordinates {
    return zoomed2domainDelta(distance, domainRangeX, domainRangeY)
  }

  fun visibleDomainRangeYinWindow(): LinearValueRange {
    return visibleDomainRangeYinWindow(domainRangeY)
  }
}

/**
 * Creates a new instance of the domain chart calculator
 */
fun ChartState.domainChartCalculator(
  domainRangeX: LinearValueRange,
  domainRangeY: LinearValueRange,
): DomainChartCalculator {
  return DomainChartCalculator(chartState = this, domainRangeX = domainRangeX, domainRangeY = domainRangeY)
}

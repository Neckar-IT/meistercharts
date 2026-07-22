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
package com.meistercharts.algorithms.impl

import assertk.*
import assertk.assertions.*
import assertk.assertions.support.*
import com.meistercharts.calc.ChartCalculator
import com.meistercharts.zoom.ZoomAndTranslationModifier
import com.meistercharts.zoom.ZoomAndTranslationSupport
import it.neckar.geometry.Distance
import it.neckar.geometry.Size
import com.meistercharts.model.Zoom
import com.meistercharts.model.Insets
import com.meistercharts.state.DefaultChartState
import com.meistercharts.zoom.FittingWithMargin
import com.meistercharts.zoom.FittingWithMarginPercentage
import com.meistercharts.zoom.MoveDomainValueToLocation
import com.meistercharts.zoom.ZoomAndTranslationDefaults
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 *
 */
class ZoomAndTranslationDefaultsTest {
  lateinit var chartState: DefaultChartState
  lateinit var zoomAndTranslationSupport: ZoomAndTranslationSupport
  lateinit var chartCalculator: ChartCalculator

  @BeforeEach
  fun setUp() {
    chartState = DefaultChartState()
    zoomAndTranslationSupport = ZoomAndTranslationSupport(chartState, ZoomAndTranslationModifier.none, ZoomAndTranslationDefaults.noTranslation)
    chartCalculator = zoomAndTranslationSupport.chartCalculator

    assertThat(chartState.windowSize).isZero()
  }

  @Test
  fun testFittingWithMarginPercentage() {
    assertThat(chartState.windowSize).isZero()

    FittingWithMarginPercentage(0.0, 0.0).let {
      assertThat(it.defaultZoom(chartCalculator)).isEqualTo(Zoom.default)
      assertThat(it.defaultTranslation(chartCalculator)).isEqualTo(Distance.none)
    }

    FittingWithMarginPercentage(0.1, 0.1).let {
      assertThat(it.defaultZoom(chartCalculator)).isEqualTo(Zoom.of(0.9, 0.9))
      assertThat(it.defaultTranslation(chartCalculator)).isEqualTo(Distance.none)
    }
  }

  @Test
  fun testMoveDomainValueToLocation() {
    assertThat(chartState.windowSize).isZero()

    MoveDomainValueToLocation(
      domainRelativeValueProvider = { 0.8 },
      targetLocationProvider = { _ -> Double.NaN }
    ).let {
      assertThat(it.defaultZoom(chartCalculator)).isEqualTo(Zoom.default)
      assertThat(it.defaultTranslation(chartCalculator)).isEqualTo(Distance.zero)
    }
  }

  @Test
  fun `FittingWithMargin returns default zoom when the window is narrower than the horizontal margin`() {
    //Window is wide 100 but the horizontal margins (left+right) eat 120 -> net width is negative,
    //while the net height stays positive. Must fall back to Zoom.default, not a negative zoom.
    chartState.contentAreaSize = Size(800.0, 600.0)
    chartState.windowSize = Size(100.0, 600.0)

    //Insets(top, right, bottom, left) -> left 60 + right 60 = 120 horizontal margin
    FittingWithMargin(Insets(0.0, 60.0, 0.0, 60.0)).let {
      assertThat(it.defaultZoom(chartCalculator)).isEqualTo(Zoom.default)
    }
  }
}

private fun Assert<Size>.isZero() = given { actual ->
  if (actual.isZero()) return@given
  expected("to be zero but was <$actual>")
}

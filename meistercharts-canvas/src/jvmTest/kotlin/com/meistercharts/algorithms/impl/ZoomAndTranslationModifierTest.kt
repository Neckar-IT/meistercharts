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
package com.meistercharts.algorithms.impl

import assertk.*
import assertk.assertions.*
import com.meistercharts.calc.ChartCalculator
import com.meistercharts.zoom.ZoomAndTranslationModifier
import it.neckar.geometry.Distance
import it.neckar.geometry.Size
import com.meistercharts.model.Zoom
import com.meistercharts.state.DefaultChartState
import com.meistercharts.zoom.ContentAreaAlwaysCompletelyVisibleTranslationModifier
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 *
 */
class ZoomAndTranslationModifierTest {
  lateinit var chartState: DefaultChartState
  lateinit var chartCalculator: ChartCalculator

  @BeforeEach
  fun setUp() {
    chartState = DefaultChartState()
    chartCalculator = ChartCalculator(chartState)
  }

  @Test
  fun testZoom() {
    val modifier = ContentAreaAlwaysCompletelyVisibleTranslationModifier(delegate = ZoomAndTranslationModifier.none)

    assertThat(chartState.windowSize).isEqualTo(Size.zero)
    assertThat(chartState.contentAreaSize).isEqualTo(Size.zero)

    assertThat(modifier.modifyZoom(Zoom.default, chartCalculator)).isEqualTo(Zoom.default)

    chartState.windowSize = Size(800.0, 600.0)
    assertThat(modifier.modifyZoom(Zoom.default, chartCalculator)).isEqualTo(Zoom.default)

    chartState.contentAreaSize = Size(800.0, 600.0)
    assertThat(modifier.modifyZoom(Zoom.default, chartCalculator)).isEqualTo(Zoom.default)
  }

  @Test
  fun testTranslationZoom() {
    val modifier = ContentAreaAlwaysCompletelyVisibleTranslationModifier(delegate = ZoomAndTranslationModifier.none)

    chartState.windowSize = Size(800.0, 600.0)
    chartState.contentAreaSize = Size(800.0, 600.0)
    assertThat(chartState.zoom).isEqualTo(Zoom.default)
    assertThat(chartState.windowTranslationX).isEqualTo(0.0)

    assertThat(modifier.modifyTranslation(Distance.zero, chartCalculator)).isEqualTo(Distance.zero)
    assertThat(modifier.modifyTranslation(Distance.of(10.0, 10.0), chartCalculator)).isEqualTo(Distance.zero)

    chartState.zoom = Zoom.of(0.5, 0.5)
    assertThat(modifier.modifyTranslation(Distance.of(10.0, 10.0), chartCalculator)).isEqualTo(Distance.of(10.0, 10.0))
    assertThat(modifier.modifyTranslation(Distance.of(200.0, 200.0), chartCalculator)).isEqualTo(Distance.of(200.0, 200.0))
    assertThat(modifier.modifyTranslation(Distance.of(400.0, 300.0), chartCalculator)).isEqualTo(Distance.of(400.0, 300.0))

    //That is too much
    assertThat(modifier.modifyTranslation(Distance.of(401.0, 301.0), chartCalculator)).isEqualTo(Distance.of(400.0, 300.0))

    //negative
    assertThat(modifier.modifyTranslation(Distance.of(-0.1, -0.1), chartCalculator)).isEqualTo(Distance.of(0.0, 0.0))
  }

  @Test
  fun testTranslation() {
    val modifier = ContentAreaAlwaysCompletelyVisibleTranslationModifier(delegate = ZoomAndTranslationModifier.none)

    chartState.windowSize = Size(800.0, 600.0)
    chartState.contentAreaSize = Size(800.0, 600.0)
    assertThat(chartState.zoom).isEqualTo(Zoom.default)
    assertThat(chartState.windowTranslationX).isEqualTo(0.0)

    assertThat(modifier.modifyTranslation(Distance.zero, chartCalculator)).isEqualTo(Distance.zero)
    assertThat(modifier.modifyTranslation(Distance.of(10.0, 10.0), chartCalculator)).isEqualTo(Distance.zero)

    chartState.contentAreaSize = Size(750.0, 600.0)
    assertThat(modifier.modifyTranslation(Distance.of(10.0, 10.0), chartCalculator)).isEqualTo(Distance.of(10.0, 0.0))
  }
}

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
package com.meistercharts.zoom

import assertk.*
import assertk.assertions.*
import com.meistercharts.calc.ChartCalculator
import com.meistercharts.state.DefaultChartState
import it.neckar.geometry.AxisOrientationY
import it.neckar.geometry.Distance
import it.neckar.geometry.Size
import org.junit.jupiter.api.Test

/**
 *
 */
class ContentAreaAlwaysBarelyVisibleTranslationModifierTest {
  @Test
  fun testCalculations() {
    val chartState = DefaultChartState()
    val calculator = ChartCalculator(chartState)

    assertThat(chartState.zoomX).isEqualTo(1.0)
    assertThat(chartState.zoomY).isEqualTo(1.0)
    assertThat(chartState.windowTranslation).isEqualTo(Distance.zero)
    assertThat(chartState.contentAreaSize).isEqualTo(Size.zero)
    chartState.axisOrientationY = AxisOrientationY.OriginAtTop

    chartState.contentAreaSize = Size(800.0, 600.0)
    chartState.windowSize = Size(480.0, 320.0)

    val modifier = ContentAreaAlwaysBarelyVisibleTranslationModifier(ZoomAndTranslationModifier.none)
    assertThat(modifier.calculateMaxX(calculator)).isEqualTo(480.0)
    assertThat(modifier.calculateMaxY(calculator)).isEqualTo(320.0)

    chartState.zoomX = 3.0
    chartState.zoomY = 3.0

    assertThat(modifier.calculateMaxX(calculator)).isEqualTo(480.0)
    assertThat(modifier.calculateMaxY(calculator)).isEqualTo(320.0)

    chartState.zoomX = 0.2
    chartState.zoomY = 0.2

    assertThat(modifier.calculateMaxX(calculator)).isEqualTo(480.0)
    assertThat(modifier.calculateMaxY(calculator)).isEqualTo(320.0)
  }
}

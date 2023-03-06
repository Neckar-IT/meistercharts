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
import com.meistercharts.algorithms.ChartCalculator
import com.meistercharts.algorithms.ZoomAndTranslationModifier
import com.meistercharts.algorithms.axis.AxisOrientationY
import com.meistercharts.model.Distance
import com.meistercharts.model.Size
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 */
internal class OnlyPositiveTranslationModifierTest {
  lateinit var chartState: DefaultChartState
  lateinit var calculator: ChartCalculator

  @BeforeEach
  internal fun setUp() {
    chartState = DefaultChartState()
    chartState.contentAreaSize = Size(800.0, 600.0)

    calculator = ChartCalculator(chartState)
  }

  @Test
  internal fun testSimple() {
    val modifier = OnlyPositiveTranslationModifier(ZoomAndTranslationModifier.none)

    modifier.modifyTranslation(Distance.none, calculator)
      .also {
        assertThat(it.x).isEqualTo(0.0)
        assertThat(it.y).isEqualTo(0.0)
      }
  }

  @Test
  internal fun testZoomFactor1OriginTop() {
    chartState.axisOrientationY = AxisOrientationY.OriginAtTop

    val modifier = OnlyPositiveTranslationModifier(ZoomAndTranslationModifier.none)

    //No limit in positive direction allowed
    modifier.modifyTranslation(Distance.of(5000.0, 5000.0), calculator)
      .also {
        assertThat(it.x).isEqualTo(0.0)
        assertThat(it.y).isEqualTo(0.0)
      }

    //in negative direction it is possible - but limited
    modifier.modifyTranslation(Distance.of(-5000.0, -5000.0), calculator)
      .also {
        assertThat(it.x).isEqualTo(-800.0)
        assertThat(it.y).isEqualTo(-600.0)
      }
  }

  @Test
  internal fun testZoomFactor1OriginBottom() {
    chartState.axisOrientationY = AxisOrientationY.OriginAtBottom

    val modifier = OnlyPositiveTranslationModifier(ZoomAndTranslationModifier.none)

    modifier.modifyTranslation(Distance.of(5000.0, 5000.0), calculator)
      .also {
        assertThat(it.x).isEqualTo(0.0)
        assertThat(it.y).isEqualTo(600.0)
      }

    //in negative direction it is possible - but limited
    modifier.modifyTranslation(Distance.of(-5000.0, -5000.0), calculator)
      .also {
        assertThat(it.x).isEqualTo(-800.0)
        assertThat(it.y).isEqualTo(0.0)
      }
  }
}

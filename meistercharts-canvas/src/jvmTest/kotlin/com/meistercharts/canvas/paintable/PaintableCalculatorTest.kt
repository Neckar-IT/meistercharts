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
package com.meistercharts.canvas.paintable

import assertk.*
import assertk.assertions.*
import com.meistercharts.algorithms.ChartCalculator
import com.meistercharts.algorithms.axis.AxisOrientationX
import com.meistercharts.algorithms.axis.AxisOrientationY
import com.meistercharts.algorithms.impl.DefaultChartState
import com.meistercharts.model.Rectangle
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 *
 */
class PaintableCalculatorTest {
  lateinit var chartState: DefaultChartState
  lateinit var paintableCalculator: PaintableCalculator
  lateinit var chartCalculator: ChartCalculator

  @BeforeEach
  fun setUp() {
    chartState = DefaultChartState()
    chartCalculator = ChartCalculator(chartState)
    paintableCalculator = PaintableCalculator(chartCalculator, Rectangle(-20.0, -30.0, 100.0, 75.0))
  }

  @Test
  fun testNegativeValueRange() {
    paintableCalculator = PaintableCalculator(chartCalculator, Rectangle(-7.5, 0.0, 100.0, 200.0))

    chartState.axisOrientationY = AxisOrientationY.OriginAtTop

    assertThat(paintableCalculator.domainRelative2heightDelta(0.0)).isEqualTo(0.0)
    assertThat(paintableCalculator.domainRelative2heightDelta(1.0)).isEqualTo(200.0)

    assertThat(paintableCalculator.domainRelative2y(0.0)).isEqualTo(0.0)
    assertThat(paintableCalculator.domainRelative2y(0.5)).isEqualTo(100.0)
    assertThat(paintableCalculator.domainRelative2y(1.0)).isEqualTo(200.0)
    assertThat(paintableCalculator.domainRelative2y(-1.0)).isEqualTo(-200.0)


    chartState.axisOrientationY = AxisOrientationY.OriginAtBottom

    assertThat(paintableCalculator.domainRelative2heightDelta(0.0)).isEqualTo(0.0)
    assertThat(paintableCalculator.domainRelative2heightDelta(1.0)).isEqualTo(-200.0)

    assertThat(paintableCalculator.domainRelative2y(0.0)).isEqualTo(0.0)
    assertThat(paintableCalculator.domainRelative2y(0.5)).isEqualTo(-100.0)
    assertThat(paintableCalculator.domainRelative2y(1.0)).isEqualTo(-200.0)
    assertThat(paintableCalculator.domainRelative2y(-1.0)).isEqualTo(200.0)
  }

  @Test
  fun testRelativeY() {
    assertThat(chartState.axisOrientationY).isEqualTo(AxisOrientationY.OriginAtBottom)

    assertThat(paintableCalculator.domainRelative2heightDelta(0.0)).isEqualTo(0.0)
    assertThat(paintableCalculator.domainRelative2heightDelta(1.0)).isEqualTo(-75.0)

    chartState.axisOrientationY = AxisOrientationY.OriginAtTop
    assertThat(paintableCalculator.domainRelative2heightDelta(0.0)).isEqualTo(0.0)
    assertThat(paintableCalculator.domainRelative2heightDelta(1.0)).isEqualTo(75.0)
  }

  @Test
  fun testRelativeX() {
    assertThat(chartState.axisOrientationX).isEqualTo(AxisOrientationX.OriginAtLeft)

    assertThat(paintableCalculator.domainRelative2widthDelta(0.0)).isEqualTo(0.0)
    assertThat(paintableCalculator.domainRelative2widthDelta(1.0)).isEqualTo(100.0)

    chartState.axisOrientationX = AxisOrientationX.OriginAtRight
    assertThat(paintableCalculator.domainRelative2widthDelta(0.0)).isEqualTo(0.0)
    assertThat(paintableCalculator.domainRelative2widthDelta(1.0)).isEqualTo(-100.0)
  }

  @Test
  fun testY() {
    assertThat(chartState.axisOrientationY).isEqualTo(AxisOrientationY.OriginAtBottom)

    assertThat(paintableCalculator.domainRelative2y(0.0)).isEqualTo(-30.0)
    assertThat(paintableCalculator.domainRelative2y(1.0)).isEqualTo(-30.0 - 75.0)

    chartState.axisOrientationY = AxisOrientationY.OriginAtTop

    assertThat(paintableCalculator.domainRelative2y(0.0)).isEqualTo(-30.0)
    assertThat(paintableCalculator.domainRelative2y(1.0)).isEqualTo(-30.0 + 75.0)
  }

  @Test
  fun testX() {
    assertThat(chartState.axisOrientationX).isEqualTo(AxisOrientationX.OriginAtLeft)

    assertThat(paintableCalculator.domainRelative2x(0.0)).isEqualTo(-20.0)
    assertThat(paintableCalculator.domainRelative2x(1.0)).isEqualTo(80.0)

    chartState.axisOrientationX = AxisOrientationX.OriginAtRight

    assertThat(paintableCalculator.domainRelative2x(0.0)).isEqualTo(-20.0)
    assertThat(paintableCalculator.domainRelative2x(1.0)).isEqualTo(-120.0)
  }
}

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
package com.meistercharts.canvas

import assertk.*
import assertk.assertions.*
import com.meistercharts.algorithms.axis.AxisOrientationX
import com.meistercharts.algorithms.axis.AxisOrientationY
import com.meistercharts.algorithms.withAxisOrientation
import org.junit.jupiter.api.Test

class ChartSupportTest {
  @Test
  fun testRefreshListenerRemove() {
    val chartSupport = ChartSupport(MockCanvas())

    var callCount = 0


    val refreshListener = object : RefreshListener {
      override fun refresh(chartSupport: ChartSupport, frameTimestamp: Double, refreshDelta: Double) {
        callCount++

        if (callCount == 3) {
          chartSupport.removeOnRefresh(this)
        }
      }
    }
    chartSupport.onRefresh(refreshListener)

    assertThat(callCount).isEqualTo(0)
    chartSupport.refresh(600.0)
    assertThat(callCount).isEqualTo(1)
    chartSupport.refresh(700.0)
    assertThat(callCount).isEqualTo(2)

    assertThat(chartSupport.refreshListeners).contains(refreshListener)
    chartSupport.refresh(800.0)
    assertThat(callCount).isEqualTo(3)
    //Scheduled for removal, but not yet removed
    assertThat(chartSupport.refreshListenersToRemove).contains(refreshListener)
    assertThat(chartSupport.refreshListeners).contains(refreshListener)

    //Removed on next call to refresh
    chartSupport.refresh(900.0)
    assertThat(callCount).isEqualTo(3)
    assertThat(chartSupport.refreshListenersToRemove).containsNone(refreshListener)
    assertThat(chartSupport.refreshListeners).containsNone(refreshListener)
  }

  @Test
  fun testWithStack() {
    val chartSupport = ChartSupport(MockCanvas())
    assertThat(chartSupport.rootChartState).isSameAs(chartSupport.currentChartState)
    assertThat(chartSupport.currentChartState).isSameAs(chartSupport.rootChartState)
    assertThat(chartSupport.chartCalculator.chartState).isSameAs(chartSupport.currentChartState)


    var called = false

    chartSupport.withCurrentChartState({ withAxisOrientation(AxisOrientationX.OriginAtRight, AxisOrientationY.OriginAtTop) }) {
      assertThat(called).isFalse()
      assertThat(chartSupport.currentChartState).isNotEqualTo(chartSupport.rootChartState)
      assertThat(chartSupport.chartCalculator.chartState).isSameAs(chartSupport.currentChartState)

      assertThat(chartSupport.currentChartState.axisOrientationX).isEqualTo(AxisOrientationX.OriginAtRight)
      assertThat(chartSupport.currentChartState.axisOrientationY).isEqualTo(AxisOrientationY.OriginAtTop)
      called = true
    }

    assertThat(chartSupport.currentChartState).isSameAs(chartSupport.rootChartState)
    assertThat(chartSupport.chartCalculator.chartState).isSameAs(chartSupport.currentChartState)
    assertThat(called).isTrue()
  }
}

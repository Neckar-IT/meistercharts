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

import assertk.*
import assertk.assertions.*
import com.meistercharts.state.DefaultChartState
import com.meistercharts.zoom.ZoomAndTranslationDefaults
import com.meistercharts.geometry.Coordinates
import com.meistercharts.model.Insets
import com.meistercharts.model.Size
import com.meistercharts.resize.KeepCenterOnWindowResize
import com.meistercharts.resize.WindowResizeEvent
import com.meistercharts.zoom.UpdateReason
import com.meistercharts.zoom.ZoomAndTranslationModifier
import com.meistercharts.zoom.ZoomAndTranslationSupport
import org.junit.jupiter.api.Test

/**
 */
class KeepCenterOnResizeTest {
  @Test
  internal fun testHandleResizeCenter() {
    val chartState = DefaultChartState()
    val zoomAndPanSupport = ZoomAndTranslationSupport(chartState, ZoomAndTranslationModifier.none, ZoomAndTranslationDefaults.noTranslation)

    val smallSize = Size.of(800.0, 600.0)
    chartState.contentAreaSize = smallSize
    chartState.windowSize = smallSize
    zoomAndPanSupport.setZoom(4.0, 7.0, Coordinates(400.0, 300.0), reason = UpdateReason.Initial)

    assertThat(zoomAndPanSupport.chartCalculator.contentAreaRelative2domainRelativeX(0.5)).isEqualTo(0.5)
    assertThat(zoomAndPanSupport.chartCalculator.contentAreaRelative2domainRelativeY(0.5)).isEqualTo(0.5)
    assertThat(zoomAndPanSupport.chartCalculator.window2domainRelativeX(smallSize.width / 2.0)).isEqualTo(0.5)
    assertThat(zoomAndPanSupport.chartCalculator.window2domainRelativeY(smallSize.height / 2.0)).isEqualTo(0.5)

    val largeSize = Size.of(1200.0, 768.0)
    chartState.contentAreaSize = largeSize
    chartState.windowSize = largeSize

    KeepCenterOnWindowResize.handleResize(zoomAndPanSupport, WindowResizeEvent(smallSize, largeSize, smallSize, largeSize, Insets.empty))

    //Center should be the same
    assertThat(zoomAndPanSupport.chartCalculator.contentAreaRelative2domainRelativeX(0.5)).isEqualTo(0.5)
    assertThat(zoomAndPanSupport.chartCalculator.contentAreaRelative2domainRelativeY(0.5)).isEqualTo(0.5)

    assertThat(zoomAndPanSupport.chartCalculator.window2domainRelativeX(largeSize.width / 2.0)).isEqualTo(0.5)
    assertThat(zoomAndPanSupport.chartCalculator.window2domainRelativeY(largeSize.height / 2.0)).isEqualTo(0.5)
  }

  @Test
  fun testHandleResizeCenterZeroContentArea() {
    val chartState = DefaultChartState()
    val zoomAndPanSupport = ZoomAndTranslationSupport(chartState, ZoomAndTranslationModifier.none, ZoomAndTranslationDefaults.noTranslation)

    val largeSize = Size.of(1200.0, 800.0)
    chartState.contentAreaSize = largeSize
    chartState.windowSize = largeSize

    KeepCenterOnWindowResize.handleResize(zoomAndPanSupport, WindowResizeEvent(Size.of(0.0, 0.0), largeSize, Size.of(0.0, 0.0), largeSize, Insets.empty))

    assertThat(zoomAndPanSupport.chartState.windowTranslationX).isEqualTo(0.0)
    assertThat(zoomAndPanSupport.chartState.windowTranslationY).isEqualTo(0.0)

    //Center should be the same
    assertThat(zoomAndPanSupport.chartCalculator.contentAreaRelative2domainRelativeX(0.5)).isEqualTo(0.5)
    assertThat(zoomAndPanSupport.chartCalculator.contentAreaRelative2domainRelativeY(0.5)).isEqualTo(0.5)

    assertThat(zoomAndPanSupport.chartCalculator.window2domainRelativeX(largeSize.width / 2.0)).isEqualTo(0.5)
    assertThat(zoomAndPanSupport.chartCalculator.window2domainRelativeY(largeSize.height / 2.0)).isEqualTo(0.5)
  }
}

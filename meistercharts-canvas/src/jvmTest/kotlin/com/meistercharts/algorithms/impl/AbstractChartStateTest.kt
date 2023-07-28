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
import com.meistercharts.zoom.UpdateReason
import com.meistercharts.zoom.ZoomAndTranslationModifier
import com.meistercharts.zoom.ZoomAndTranslationSupport
import it.neckar.geometry.AxisOrientationY
import com.meistercharts.state.withContentAreaSize
import com.meistercharts.state.withTranslation
import it.neckar.geometry.Coordinates
import it.neckar.geometry.Distance
import it.neckar.geometry.Size
import com.meistercharts.state.DefaultChartState
import com.meistercharts.zoom.ZoomAndTranslationDefaults
import org.junit.jupiter.api.Test

/**
 */
internal class AbstractChartStateTest {
  @Test
  internal fun testContentAreaSizeOverride() {
    val chartState = DefaultChartState()
    chartState.contentAreaSize = Size(800.0, 600.0)
    assertThat(chartState.contentAreaSize.width).isEqualTo(800.0)
    assertThat(chartState.contentAreaSize.height).isEqualTo(600.0)

    assertThat(chartState.withContentAreaSize(Size(500.0, 400.0)).contentAreaSize.width).isEqualTo(500.0)
    assertThat(chartState.withContentAreaSize(Size(500.0, 400.0)).contentAreaSize.height).isEqualTo(400.0)
  }

  @Test
  fun testTranslationOverride() {
    val chartState = DefaultChartState()
    chartState.windowTranslation = Distance(30.0, 40.0)

    assertThat(chartState.windowTranslationX).isEqualTo(30.0)
    assertThat(chartState.windowTranslationY).isEqualTo(40.0)

    assertThat(chartState.withTranslation(Distance(500.0, 400.0)).windowTranslationX).isEqualTo(500.0)
    assertThat(chartState.withTranslation(Distance(500.0, 400.0)).windowTranslationY).isEqualTo(400.0)
  }

  @Test
  fun testOverrideContentAreaSizeAndTranslation() {
    val chartState = DefaultChartState()
    chartState.contentAreaSize = Size(800.0, 600.0)
    chartState.windowTranslation = Distance(30.0, 40.0)

    assertThat(chartState.contentAreaSize.width).isEqualTo(800.0)
    assertThat(chartState.contentAreaSize.height).isEqualTo(600.0)

    assertThat(chartState.windowTranslationX).isEqualTo(30.0)
    assertThat(chartState.windowTranslationY).isEqualTo(40.0)

    val overridden0 = chartState.withContentAreaSize(Size(500.0, 400.0))
    val overridden1 = overridden0.withTranslation(Distance(99.0, 98.0))

    //Overridden values
    assertThat(overridden0.contentAreaSize.width).isEqualTo(500.0)
    assertThat(overridden0.contentAreaSize.height).isEqualTo(400.0)

    //Old values
    assertThat(overridden0.windowTranslationX).isEqualTo(30.0)
    assertThat(overridden0.windowTranslationY).isEqualTo(40.0)


    //Overridden values
    assertThat(overridden1.contentAreaSize.width).isEqualTo(500.0)
    assertThat(overridden1.contentAreaSize.height).isEqualTo(400.0)

    //old values
    assertThat(overridden1.windowTranslationX).isEqualTo(99.0)
    assertThat(overridden1.windowTranslationY).isEqualTo(98.0)
  }

  @Test
  fun testZoom() {
    val chartState = DefaultChartState()
    chartState.axisOrientationY = AxisOrientationY.OriginAtTop

    val zoomAndPanSupport = ZoomAndTranslationSupport(chartState, ZoomAndTranslationModifier.none, ZoomAndTranslationDefaults.noTranslation)
    val chartCalculator = zoomAndPanSupport.chartCalculator

    chartState.contentAreaSize = Size(1000.0, 500.0)

    assertThat(chartState.windowTranslationX).isEqualTo(0.0)
    assertThat(chartState.windowTranslationY).isEqualTo(0.0)

    assertThat(chartState.zoomX).isEqualTo(1.0)
    assertThat(chartState.zoomY).isEqualTo(1.0)

    //now zoom on the origin
    //Center should be held at center

    zoomAndPanSupport.setZoom(4.0, 4.0, Coordinates.origin, reason = UpdateReason.UserInteraction)

    assertThat(chartState.zoomX).isEqualTo(4.0)
    assertThat(chartState.zoomY).isEqualTo(4.0)

    assertThat(chartState.windowTranslationX).isEqualTo(0.0)
    assertThat(chartState.windowTranslationY).isEqualTo(0.0)


    assertThat(chartCalculator.window2domainRelativeX(0.0)).isEqualTo(0.0)


    //Reset zoom
    zoomAndPanSupport.setZoom(1.0, 1.0, Coordinates.origin, reason = UpdateReason.UserInteraction)

    //Translate by 100 pixels
    zoomAndPanSupport.moveWindow(100.0, 100.0, reason = UpdateReason.UserInteraction)
    assertThat(chartState.windowTranslationX).isEqualTo(100.0)
    assertThat(chartState.windowTranslationY).isEqualTo(100.0)

    assertThat(chartCalculator.window2domainRelativeX(0.0)).isEqualTo(-0.1)
    assertThat(chartCalculator.window2domainRelativeY(0.0)).isEqualTo(-0.2)


    //Zoom again - base should be held
    zoomAndPanSupport.setZoom(10.0, 10.0, Coordinates.origin, reason = UpdateReason.UserInteraction)

    assertThat(chartCalculator.window2domainRelativeX(0.0)).isCloseTo(-0.1, 0.000001)
    assertThat(chartCalculator.window2domainRelativeY(0.0)).isCloseTo(-0.2, 0.000001)

    assertThat(chartState.windowTranslationX).isCloseTo(1_000.0, 0.000001)
    assertThat(chartState.windowTranslationY).isCloseTo(1_000.0, 0.000001)
  }
}

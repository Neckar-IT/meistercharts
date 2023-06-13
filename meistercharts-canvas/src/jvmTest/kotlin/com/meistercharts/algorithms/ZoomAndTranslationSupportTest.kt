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
import com.meistercharts.algorithms.axis.AxisOrientationX
import com.meistercharts.algorithms.axis.AxisOrientationY
import com.meistercharts.algorithms.axis.AxisSelection
import com.meistercharts.algorithms.impl.DefaultChartState
import com.meistercharts.algorithms.impl.FittingWithMarginPercentage
import com.meistercharts.algorithms.impl.ZoomAndTranslationDefaults
import com.meistercharts.annotations.ContentArea
import com.meistercharts.model.Coordinates
import com.meistercharts.model.Size
import com.meistercharts.model.Zoom
import it.neckar.open.unit.other.px
import org.junit.jupiter.api.Test

/**
 */
class ZoomAndTranslationSupportTest {
  @Test
  fun testChartStateEmpty() {
    val chartState = DefaultChartState()

    val zoomAndPanSupport = ZoomAndTranslationSupport(chartState, ZoomAndTranslationModifier.none, ZoomAndTranslationDefaults.noTranslation)
    assertThat(chartState.zoom).isEqualTo(Zoom.default)

    zoomAndPanSupport.resetToDefaults(reason = UpdateReason.UserInteraction)
    zoomAndPanSupport.setWindowTranslationX(17.0, reason = UpdateReason.UserInteraction)
    zoomAndPanSupport.setWindowTranslationY(99.0, reason = UpdateReason.UserInteraction)
    zoomAndPanSupport.fitX(5.0, 17.0, reason = UpdateReason.UserInteraction)
    zoomAndPanSupport.fitY(5.0, 17.0, reason = UpdateReason.UserInteraction)
    assertThat(chartState.zoom).isEqualTo(Zoom.default)

    zoomAndPanSupport.modifyZoom(true, AxisSelection.Both, zoomChangeFactor = 2.0, reason = UpdateReason.UserInteraction)
    assertThat(chartState.zoom).isEqualTo(Zoom.of(2.0, 2.0))

    zoomAndPanSupport.modifyZoom(true, AxisSelection.Both, zoomCenterX = 334.4, zoomCenterY = 234234.0, zoomChangeFactor = 2.0, reason = UpdateReason.UserInteraction)
    assertThat(chartState.zoom).isEqualTo(Zoom.of(4.0, 4.0))

    zoomAndPanSupport.setZoom(Zoom.default, Coordinates.of(123.09, 3123.0), reason = UpdateReason.UserInteraction)
    assertThat(chartState.zoom).isEqualTo(Zoom.default)
  }

  @Test
  fun testBasics() {
    val chartState = DefaultChartState()
    chartState.contentAreaSize = Size.of(800.0, 600.0)

    val zoomAndPanSupport = ZoomAndTranslationSupport(chartState, ZoomAndTranslationModifier.none, ZoomAndTranslationDefaults.noTranslation)

    zoomAndPanSupport.moveWindow(10.0, 20.0, reason = UpdateReason.UserInteraction)

    assertThat(chartState.windowTranslationX).isEqualTo(10.0)
    assertThat(chartState.windowTranslationY).isEqualTo(20.0)

    zoomAndPanSupport.setZoom(2.0, 3.0, null, reason = UpdateReason.UserInteraction)

    assertThat(chartState.zoomX).isEqualTo(2.0)
    assertThat(chartState.zoomY).isEqualTo(3.0)
    assertThat(chartState.windowTranslationY).isEqualTo(20.0)
    assertThat(chartState.windowTranslationX).isEqualTo(10.0)
    assertThat(chartState.windowTranslationY).isEqualTo(20.0)
  }

  @Test
  fun testFitX() {
    val chartState = DefaultChartState()
    chartState.windowSize = Size.of(800.0, 600.0)
    chartState.contentAreaSize = Size.of(800.0, 600.0)


    val zoomAndPanSupport = ZoomAndTranslationSupport(chartState, ZoomAndTranslationModifier.none, ZoomAndTranslationDefaults.noTranslation)

    zoomAndPanSupport.setZoom(4.0, 5.0, reason = UpdateReason.UserInteraction)
    assertThat {
      assertThat(chartState.zoomX).isEqualTo(4.0)
      assertThat(chartState.zoomY).isEqualTo(5.0)
    }

    zoomAndPanSupport.fitX(0.0, 1.0, reason = UpdateReason.UserInteraction)
    assertThat(chartState.zoomX).isEqualTo(1.0)
    assertThat(chartState.windowTranslationX).isCloseTo(0.0, 0.0)

    zoomAndPanSupport.fitX(1.0, 2.0, reason = UpdateReason.UserInteraction)
    assertThat(chartState.zoomX).isEqualTo(1.0)
    assertThat(chartState.windowTranslationX).isCloseTo(-800.0, 0.0)

    zoomAndPanSupport.fitX(1.1, 2.1, reason = UpdateReason.UserInteraction)
    assertThat(chartState.zoomX).isCloseTo(1.0, 0.00000001)
    assertThat(chartState.windowTranslationX).isCloseTo(-880.0, 0.00001)

    zoomAndPanSupport.fitX(-1.0, 2.0, reason = UpdateReason.UserInteraction)
    assertThat(chartState.zoomX).isCloseTo(1 / 3.0, 0.00000001)
    assertThat(chartState.windowTranslationX).isCloseTo(800.0 / 3, 0.00001)
  }

  @Test
  fun testFitXInvertedAxis() {
    val chartState = DefaultChartState()
    chartState.windowSize = Size.of(800.0, 600.0)
    chartState.contentAreaSize = Size.of(800.0, 600.0)
    chartState.axisOrientationX = AxisOrientationX.OriginAtRight

    val zoomAndPanSupport = ZoomAndTranslationSupport(chartState, ZoomAndTranslationModifier.none, ZoomAndTranslationDefaults.noTranslation)
    val chartCalculator = zoomAndPanSupport.chartCalculator

    zoomAndPanSupport.setZoom(4.0, 5.0, reason = UpdateReason.UserInteraction)
    assertThat {
      assertThat(chartState.zoomX).isEqualTo(4.0)
      assertThat(chartState.zoomY).isEqualTo(5.0)
    }


    zoomAndPanSupport.fitX(1.0, 0.0, reason = UpdateReason.UserInteraction)
    assertThat(chartState.zoomX).isEqualTo(1.0)
    assertThat(chartState.windowTranslationX).isCloseTo(0.0, 0.0)

    zoomAndPanSupport.fitX(2.0, 1.0, reason = UpdateReason.UserInteraction)
    assertThat(chartState.zoomX).isEqualTo(1.0)
    assertThat(chartState.windowTranslationX).isCloseTo(800.0, 0.0)

    zoomAndPanSupport.fitX(2.1, 1.1, reason = UpdateReason.UserInteraction)
    assertThat(chartState.zoomX).isEqualTo(1.0)
    assertThat(chartState.windowTranslationX).isCloseTo(880.0, 0.00001)

    zoomAndPanSupport.fitX(2.0, -1.0, reason = UpdateReason.UserInteraction)
    assertThat(chartState.zoomX).isEqualTo(1 / 3.0)
    assertThat(chartState.windowTranslationX).isCloseTo(800.0 / 3, 0.00001)
  }

  @Test
  fun testFitYOriginAtTop() {
    val chartState = DefaultChartState()
    chartState.windowSize = Size.of(800.0, 600.0)
    chartState.contentAreaSize = Size.of(800.0, 600.0)
    chartState.axisOrientationY = AxisOrientationY.OriginAtTop

    val zoomAndPanSupport = ZoomAndTranslationSupport(chartState, ZoomAndTranslationModifier.none, ZoomAndTranslationDefaults.noTranslation)

    assertThat(zoomAndPanSupport.chartCalculator.domainRelative2contentAreaRelativeY(0.0)).isEqualTo(0.0)
    assertThat(zoomAndPanSupport.chartCalculator.domainRelative2contentAreaRelativeY(1.0)).isEqualTo(1.0)

    assertThat(zoomAndPanSupport.chartCalculator.domainRelative2contentAreaY(1.0)).isEqualTo(600.0)
    assertThat(zoomAndPanSupport.chartCalculator.domainRelative2contentAreaY(0.0)).isEqualTo(0.0)
    assertThat(zoomAndPanSupport.chartCalculator.domainRelative2contentAreaY(-1.0)).isEqualTo(-600.0)


    zoomAndPanSupport.setZoom(4.0, 5.0, reason = UpdateReason.UserInteraction)
    assertThat(chartState.zoomX).isEqualTo(4.0)
    assertThat(chartState.zoomY).isEqualTo(5.0)

    zoomAndPanSupport.fitY(0.0, 1.0, reason = UpdateReason.UserInteraction)
    assertThat(chartState.zoomY).isEqualTo(1.0)
    assertThat(chartState.windowTranslationY).isCloseTo(0.0, 0.00001)

    zoomAndPanSupport.fitY(1.0, 2.0, reason = UpdateReason.UserInteraction)
    assertThat(chartState.zoomY).isEqualTo(1.0)
    assertThat(chartState.windowTranslationY).isCloseTo(-600.0, 0.00001)

    zoomAndPanSupport.fitY(1.1, 2.1, reason = UpdateReason.UserInteraction)
    assertThat(chartState.zoomY).isEqualTo(1.0)
    assertThat(chartState.windowTranslationY).isCloseTo(-660.0, 0.00001)

    zoomAndPanSupport.fitY(-1.0, 2.0, reason = UpdateReason.UserInteraction)
    assertThat(chartState.zoomY).isEqualTo(1 / 3.0)
    assertThat(chartState.windowTranslationY).isCloseTo(600.0 / 3.0, 0.00001)
  }

  @Test
  fun testFitYOriginAtBottom() {
    val chartState = DefaultChartState()
    chartState.windowSize = Size.of(800.0, 600.0)
    chartState.contentAreaSize = Size.of(800.0, 600.0)
    chartState.axisOrientationY = AxisOrientationY.OriginAtBottom

    val zoomAndPanSupport = ZoomAndTranslationSupport(chartState, ZoomAndTranslationModifier.none, ZoomAndTranslationDefaults.noTranslation)

    assertThat(zoomAndPanSupport.chartCalculator.domainRelative2contentAreaRelativeY(0.0)).isEqualTo(1.0)
    assertThat(zoomAndPanSupport.chartCalculator.domainRelative2contentAreaRelativeY(1.0)).isEqualTo(0.0)

    assertThat(zoomAndPanSupport.chartCalculator.domainRelative2contentAreaY(0.0)).isEqualTo(600.0)
    assertThat(zoomAndPanSupport.chartCalculator.domainRelative2contentAreaY(1.0)).isEqualTo(0.0)
    assertThat(zoomAndPanSupport.chartCalculator.domainRelative2contentAreaY(-1.0)).isEqualTo(1200.0)


    zoomAndPanSupport.setZoom(4.0, 5.0, reason = UpdateReason.UserInteraction)
    assertThat(chartState.zoomX).isEqualTo(4.0)
    assertThat(chartState.zoomY).isEqualTo(5.0)

    zoomAndPanSupport.fitY(1.0, 0.0, reason = UpdateReason.UserInteraction)
    assertThat(chartState.zoomY).isEqualTo(1.0)
    assertThat(chartState.windowTranslationY).isCloseTo(0.0, 0.00001)

    zoomAndPanSupport.fitY(2.0, 1.0, reason = UpdateReason.UserInteraction)
    assertThat(chartState.zoomY).isEqualTo(1.0)
    assertThat(chartState.windowTranslationY).isCloseTo(600.0, 0.00001)

    zoomAndPanSupport.fitY(2.1, 1.1, reason = UpdateReason.UserInteraction)
    assertThat(chartState.zoomY).isEqualTo(1.0)
    assertThat(chartState.windowTranslationY).isCloseTo(660.0, 0.00001)

    zoomAndPanSupport.fitY(2.0, -1.0, reason = UpdateReason.UserInteraction)
    assertThat(chartState.zoomY).isEqualTo(1 / 3.0)
    assertThat(chartState.windowTranslationY).isCloseTo(600.0 / 3.0, 0.00001)
  }

  @Test
  fun testFitXInvisibleWindow() {
    val chartState = DefaultChartState()
    chartState.windowSize = Size.zero
    chartState.contentAreaSize = Size(200.0, 200.0)

    val zoomAndPanSupport = ZoomAndTranslationSupport(chartState, ZoomAndTranslationModifier.none, ZoomAndTranslationDefaults.noTranslation)

    zoomAndPanSupport.fitX(0.5, 0.6, reason = UpdateReason.UserInteraction) // must not raise an exception
  }

  @Test
  fun testFitYInvisibleWindow() {
    val chartState = DefaultChartState()
    chartState.windowSize = Size.zero
    chartState.contentAreaSize = Size(200.0, 200.0)

    val zoomAndPanSupport = ZoomAndTranslationSupport(chartState, ZoomAndTranslationModifier.none, ZoomAndTranslationDefaults.noTranslation)

    zoomAndPanSupport.fitY(0.5, 0.6, reason = UpdateReason.UserInteraction) // must not raise an exception
  }

  @Test
  fun testFitXInvisibleContentArea() {
    val chartState = DefaultChartState()
    chartState.windowSize = Size(200.0, 200.0)
    chartState.contentAreaSize = Size.zero

    val zoomAndPanSupport = ZoomAndTranslationSupport(chartState, ZoomAndTranslationModifier.none, ZoomAndTranslationDefaults.noTranslation)

    zoomAndPanSupport.fitX(0.5, 0.6, reason = UpdateReason.UserInteraction) // must not raise an exception
  }

  @Test
  fun testFitYInvisibleContentArea() {
    val chartState = DefaultChartState()
    chartState.windowSize = Size(200.0, 200.0)
    chartState.contentAreaSize = Size.zero

    val zoomAndPanSupport = ZoomAndTranslationSupport(chartState, ZoomAndTranslationModifier.none, ZoomAndTranslationDefaults.noTranslation)

    zoomAndPanSupport.fitY(0.5, 0.6, reason = UpdateReason.UserInteraction) // must not raise an exception
  }

  @Test
  fun testZoomWithCenter() {
    val chartState = DefaultChartState()
    chartState.axisOrientationY = AxisOrientationY.OriginAtTop

    val zoomAndPanSupport = ZoomAndTranslationSupport(chartState, ZoomAndTranslationModifier.none, ZoomAndTranslationDefaults.noTranslation)

    assertThat(zoomAndPanSupport.chartCalculator.domainRelative2windowX(0.0)).isEqualTo(0.0)
    assertThat(zoomAndPanSupport.chartCalculator.domainRelative2windowX(1.0)).isEqualTo(0.0)

    chartState.contentAreaSize = Size.of(800.0, 600.0)

    assertThat(zoomAndPanSupport.chartCalculator.domainRelative2windowX(0.0)).isEqualTo(0.0)
    assertThat(zoomAndPanSupport.chartCalculator.domainRelative2windowX(1.0)).isEqualTo(800.0)


    assertThat(zoomAndPanSupport.chartCalculator.window2domainRelative(0.0, 0.0)).isEqualTo(Coordinates.of(0.0, 0.0))
    assertThat(zoomAndPanSupport.chartCalculator.window2domainRelative(10.0, 20.0)).isEqualTo(Coordinates.of(1.0 / 800.0 * 10.0, 1.0 / 600.0 * 20.0))


    zoomAndPanSupport.moveWindow(-10.0, -20.0, reason = UpdateReason.UserInteraction)
    assertThat(chartState.windowTranslationX).isEqualTo(-10.0)
    assertThat(chartState.windowTranslationY).isEqualTo(-20.0)

    assertThat(zoomAndPanSupport.chartCalculator.window2domainRelative(0.0, 0.0)).isEqualTo(Coordinates.of(1.0 / 800.0 * 10.0, 1.0 / 600.0 * 20.0))


    val zoomCenter = Coordinates.of(15.0, 25.0)

    //*BEFORE* zoom
    assertThat(zoomAndPanSupport.chartCalculator.window2domainRelativeX(zoomCenter.x)).isCloseTo(1.0 / 800.0 * (10 + 15), 0.0001)
    assertThat(zoomAndPanSupport.chartCalculator.window2domainRelativeY(zoomCenter.y)).isCloseTo(1.0 / 600.0 * (20 + 25), 0.0001)

    zoomAndPanSupport.setZoom(20.0, 30.0, zoomCenter, reason = UpdateReason.UserInteraction)

    //AFTER zoom - must be the same domain relative values!
    assertThat(zoomAndPanSupport.chartCalculator.window2domainRelativeX(zoomCenter.x)).isCloseTo(1.0 / 800.0 * (10 + 15), 0.0001)
    assertThat(zoomAndPanSupport.chartCalculator.window2domainRelativeY(zoomCenter.y)).isCloseTo(1.0 / 600.0 * (20 + 25), 0.0001)

    assertThat(chartState.zoomX).isEqualTo(20.0)
    assertThat(chartState.zoomY).isEqualTo(30.0)
  }

  @Test
  fun testResetWindowTranslation() {
    val chartState = DefaultChartState()
    val fittingWithMarginPercentage = FittingWithMarginPercentage(0.1, 0.2)
    val zoomAndPanSupport = ZoomAndTranslationSupport(chartState, ZoomAndTranslationModifier.none, fittingWithMarginPercentage)

    chartState.contentAreaSize = Size.of(800.0, 600.0)
    @ContentArea @px val overScanX = fittingWithMarginPercentage.marginPercentageX * chartState.contentAreaWidth
    @ContentArea @px val overScanY = fittingWithMarginPercentage.marginPercentageY * chartState.contentAreaHeight

    assertThat(overScanX).isEqualTo(80.0)
    assertThat(overScanY).isEqualTo(120.0)

    @ContentArea @px val offsetOverScanX = overScanX * 0.5
    @ContentArea @px val offsetOverScanY = overScanY * 0.5

    assertThat(offsetOverScanX).isEqualTo(40.0)
    assertThat(offsetOverScanY).isEqualTo(60.0)


    assertThat(chartState.windowTranslationX).isEqualTo(0.0)
    assertThat(chartState.windowTranslationY).isEqualTo(0.0)

    val zoomList = listOf(
      Zoom(1.0, 1.0),
      Zoom(0.5, 0.25),
      Zoom(2.0, 4.0),
      Zoom(2.0, 0.5),
      Zoom(0.5, 2.0)
    )

    zoomList.forEach {
      zoomAndPanSupport.setZoom(it, null, reason = UpdateReason.UserInteraction)
      zoomAndPanSupport.setWindowTranslationX(100.0, reason = UpdateReason.UserInteraction)
      zoomAndPanSupport.setWindowTranslationY(200.0, reason = UpdateReason.UserInteraction)
      assertThat(chartState.windowTranslationX).isEqualTo(100.0)
      assertThat(chartState.windowTranslationY).isEqualTo(200.0)

      zoomAndPanSupport.resetWindowTranslation( reason = UpdateReason.UserInteraction)
      assertThat(chartState.windowTranslationX, "Zoom: $it").isEqualTo(40.0 * it.scaleX)
      assertThat(chartState.windowTranslationY).isEqualTo(60.0 * it.scaleY)
    }
  }
}

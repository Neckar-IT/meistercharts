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
import com.meistercharts.algorithms.impl.DefaultChartState
import com.meistercharts.algorithms.tile.TileIndex
import com.meistercharts.annotations.ContentArea
import com.meistercharts.annotations.ContentAreaRelative
import com.meistercharts.annotations.Domain
import com.meistercharts.annotations.DomainRelative
import com.meistercharts.annotations.Window
import com.meistercharts.annotations.Zoomed
import com.meistercharts.model.Coordinates
import com.meistercharts.model.Distance
import com.meistercharts.model.Size
import it.neckar.open.unit.other.pct
import it.neckar.open.unit.si.ms
import org.assertj.core.data.Offset
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 */
internal class ChartCalculatorTest {

  private lateinit var chartState: MutableChartState
  private lateinit var calculator: ChartCalculator

  @BeforeEach
  fun setUp() {
    chartState = DefaultChartState()
    chartState.contentAreaSize = Size(800.0, 600.0)
    calculator = ChartCalculator(chartState)
  }

  @Test
  internal fun testWindowConversions() {
    chartState.windowTranslationX = 0.0
    chartState.zoomX = 1.0

    assertThat(calculator.window2contentAreaX(0.0)).isEqualTo(0.0)

    chartState.windowTranslationX = 100.0
    chartState.zoomX = 1.0

    assertThat(calculator.window2contentAreaX(0.0)).isEqualTo(-100.0)
  }

  @Test
  internal fun testOverride() {
    assertThat(calculator.domainRelative2windowX(1.0)).isEqualTo(800.0)
    val calc2 = calculator.withContentAreaSize(Size(500.0, 500.0))

    assertThat(calc2.chartState.contentAreaSize.width).isEqualTo(500.0)

    //the old one
    assertThat(calculator.domainRelative2windowX(1.0)).isEqualTo(800.0)
    //Overridden
    assertThat(calc2.domainRelative2windowX(1.0)).isEqualTo(500.0)
  }

  @Test
  internal fun testConversionWithAxisOrientation() {
    //Domain 0 --> 0
    assertRoundTrip(-0.1, -0.1, AxisOrientationY.OriginAtTop)
    assertRoundTrip(0.0, 0.0, AxisOrientationY.OriginAtTop)
    assertRoundTrip(0.5, 0.5, AxisOrientationY.OriginAtTop)
    assertRoundTrip(1.0, 1.0, AxisOrientationY.OriginAtTop)
    assertRoundTrip(1.1, 1.1, AxisOrientationY.OriginAtTop)

    //Inverted 0 --> -1.0
    assertRoundTrip(-0.1, 1.1, AxisOrientationY.OriginAtBottom)
    assertRoundTrip(0.0, 1.0, AxisOrientationY.OriginAtBottom)
    assertRoundTrip(0.5, 0.5, AxisOrientationY.OriginAtBottom)
    assertRoundTrip(1.0, 0.0, AxisOrientationY.OriginAtBottom)
    assertRoundTrip(1.1, -0.1, AxisOrientationY.OriginAtBottom)
  }

  @Test
  fun testConversionWithAxisOrientation2() {
    assertRoundTrip(-0.1, -0.1, AxisOrientationY.OriginAtTop)
    assertRoundTrip(-3.0, -3.0, AxisOrientationY.OriginAtTop)
    assertRoundTrip(-7.0, -7.0, AxisOrientationY.OriginAtTop)

    assertRoundTrip(1.0, 1.0, AxisOrientationY.OriginAtTop)
    assertRoundTrip(1.1, 1.1, AxisOrientationY.OriginAtTop)
    assertRoundTrip(3.0, 3.0, AxisOrientationY.OriginAtTop)
    assertRoundTrip(7.0, 7.0, AxisOrientationY.OriginAtTop)

    assertRoundTrip(-0.1, 1.1, AxisOrientationY.OriginAtBottom)
    assertRoundTrip(-3.0, 4.0, AxisOrientationY.OriginAtBottom)
    assertRoundTrip(-7.0, 8.0, AxisOrientationY.OriginAtBottom)

    assertRoundTrip(1.0, 0.0, AxisOrientationY.OriginAtBottom)
    assertRoundTrip(1.1, -0.1, AxisOrientationY.OriginAtBottom)
    assertRoundTrip(3.0, -2.0, AxisOrientationY.OriginAtBottom)
    assertRoundTrip(7.0, -6.0, AxisOrientationY.OriginAtBottom)
  }


  fun assertRoundTrip(@Domain domainRelativeValue: Double, @DomainRelative expectedContentAreaRelative: Double, axisOrientation: AxisOrientationY) {
    val contentAreaRelative = InternalCalculations.domainRelative2contentAreaRelative(domainRelativeValue, axisOrientation)
    assertThat(contentAreaRelative).isCloseTo(expectedContentAreaRelative, 0.00001)

    val domainRelativeConvertedBack = InternalCalculations.contentAreaRelative2domainRelative(contentAreaRelative, axisOrientation)
    assertThat(domainRelativeConvertedBack).isCloseTo(domainRelativeValue, 0.00001)
  }

  @Test
  internal fun testOrientation() {
    //Top down
    assertThat(calculator.contentAreaRelative2zoomedY(1.0)).isEqualTo(600.0)
  }

  @Test
  fun testConversionUnzoomed2Visible() {
    assertUnzoomed2VisibleRoundTrip(0.0)
    assertUnzoomed2VisibleRoundTrip(10.0)
    assertUnzoomed2VisibleRoundTrip(100.0)

    chartState.windowTranslation = Distance(0.0, 10.0)

    assertUnzoomed2VisibleRoundTrip(0.0)
    assertUnzoomed2VisibleRoundTrip(10.0)
    assertUnzoomed2VisibleRoundTrip(100.0)

    chartState.zoomY = 2.4

    assertUnzoomed2VisibleRoundTrip(0.0)
    assertUnzoomed2VisibleRoundTrip(10.0)
    assertUnzoomed2VisibleRoundTrip(100.0)
  }

  private fun assertUnzoomed2VisibleRoundTrip(unzoomed: Double) {
    val visibleAreay = calculator.contentAreaRelative2windowY(unzoomed)
    val back = calculator.window2contentAreaRelativeY(visibleAreay)
    assertThat(back).isEqualTo(unzoomed)
  }

  @Test
  fun testBasicToContentAreaConversion() {
    chartState.contentAreaSize = Size(800.0, 600.0)

    assertThat(calculator.contentAreaRelative2contentAreaY(0.0)).isEqualTo(0.0)
    assertThat(calculator.contentAreaRelative2contentAreaY(1.0)).isEqualTo(600.0)
    assertThat(calculator.contentAreaRelative2contentAreaY(1.5)).isEqualTo(900.0)

    assertThat(calculator.contentArea2contentAreaRelativeY(0.0)).isEqualTo(0.0)
    assertThat(calculator.contentArea2contentAreaRelativeY(600.0)).isEqualTo(1.0)
    assertThat(calculator.contentArea2contentAreaRelativeY(900.0)).isEqualTo(1.5)
  }

  @Test
  fun testZoomAndTranslate() {
    assertThat(calculator.contentArea2windowY(0.0)).isEqualTo(0.0)
    assertThat(calculator.contentArea2windowY(10.0)).isEqualTo(10.0)
    assertThat(calculator.window2contentAreaY(0.0)).isEqualTo(0.0)
    assertThat(calculator.window2contentAreaY(10.0)).isEqualTo(10.0)

    chartState.zoomY = 2.0
    assertThat(chartState.zoomY).isEqualTo(2.0)

    assertThat(calculator.contentArea2windowY(0.0)).isEqualTo(0.0)
    assertThat(calculator.contentArea2windowY(10.0)).isEqualTo(20.0)
    assertThat(calculator.window2contentAreaY(0.0)).isEqualTo(0.0)
    assertThat(calculator.window2contentAreaY(20.0)).isEqualTo(10.0)

    chartState.windowTranslation = Distance(12.0, 14.0)

    assertThat(calculator.contentArea2windowY(0.0)).isEqualTo(14.0)
    assertThat(calculator.contentArea2windowY(10.0)).isEqualTo((20 + 14).toDouble())

    assertThat(calculator.window2contentAreaY(0.0)).isEqualTo(-7.0)
    assertThat(calculator.window2contentAreaY(20.0)).isEqualTo(3.0)
  }

  @Test
  internal fun testRoundTrip() {
    for (i in 1..100) {
      chartState.zoomX = i * 0.5

      for (j in -10..10) {
        chartState.windowTranslation = Distance.of(j.toDouble(), j * 2.0)

        runRoundTripX(0.0)
        runRoundTripX(100.0)
        runRoundTripX(200.0)
        runRoundTripX(500.0)
        runRoundTripX(99.999)

        runRoundTripX(-0.0)
        runRoundTripX(-100.0)
        runRoundTripX(-200.0)
        runRoundTripX(-500.0)
        runRoundTripX(-99.999)

        runRoundTripY(0.0)
        runRoundTripY(100.0)
        runRoundTripY(200.0)
        runRoundTripY(500.0)
        runRoundTripY(99.999)

        runRoundTripY(-0.0)
        runRoundTripY(-100.0)
        runRoundTripY(-200.0)
        runRoundTripY(-500.0)
        runRoundTripY(-99.999)
      }
    }
  }


  private fun runRoundTripX(@DomainRelative domainRelative: Double) {
    assertThat(
      calculator.contentAreaRelative2domainRelativeX(
        calculator.domainRelative2contentAreaRelativeX(domainRelative)
      )
    ).isCloseTo(domainRelative, 0.0000000001)

    assertThat(
      calculator.contentArea2domainRelativeX(
        calculator.domainRelative2contentAreaX(domainRelative)
      )
    ).isCloseTo(domainRelative, 0.0000000001)

    assertThat(
      calculator.zoomed2domainRelativeX(
        calculator.domainRelative2zoomedX(domainRelative)
      )
    ).isCloseTo(domainRelative, 0.0000000001)

    assertThat(
      calculator.window2domainRelativeX(
        calculator.domainRelative2windowX(domainRelative)
      )
    ).isCloseTo(domainRelative, 0.0000000001)


    //Now compare the results of the largest step with the intermediate steps

    @ContentAreaRelative val contentAreaRelative = calculator.domainRelative2contentAreaRelativeX(domainRelative)
    @ContentArea val contentArea = calculator.contentAreaRelative2contentAreaX(contentAreaRelative)
    @Zoomed val zoomed = calculator.contentArea2zoomedX(contentArea)
    @Window val window = calculator.zoomed2windowX(zoomed)

    Offset.offset(0.0000000001)
    assertThat(
      calculator.domainRelative2contentAreaRelativeX(domainRelative)
    ).isEqualTo(contentAreaRelative)

    Offset.offset(0.0000000001)
    assertThat(
      calculator.domainRelative2contentAreaX(domainRelative)
    ).isEqualTo(contentArea)

    Offset.offset(0.0000000001)
    assertThat(
      calculator.domainRelative2zoomedX(domainRelative)
    ).isEqualTo(zoomed)

    Offset.offset(0.0000000001)
    assertThat(
      calculator.domainRelative2windowX(domainRelative)
    ).isEqualTo(window)
  }

  private fun runRoundTripY(@DomainRelative domainRelative: Double) {
    assertThat(
      calculator.contentAreaRelative2domainRelativeY(
        calculator.domainRelative2contentAreaRelativeY(domainRelative)
      )
    ).isCloseTo(domainRelative, 0.0000000001)

    assertThat(
      calculator.contentArea2domainRelativeY(
        calculator.domainRelative2contentAreaY(domainRelative)
      )
    ).isCloseTo(domainRelative, 0.0000000001)

    Offset.offset(0.0000000001)
    assertThat(
      calculator.zoomed2domainRelativeY(
        calculator.domainRelative2zoomedY(domainRelative)
      )
    ).isCloseTo(domainRelative, 0.0000000001)

    Offset.offset(0.0000000001)
    assertThat(
      calculator.window2domainRelativeY(
        calculator.domainRelative2windowY(domainRelative)
      )
    ).isCloseTo(domainRelative, 0.0000000001)


    //Now compare the results of the largest step with the intermediate steps

    @ContentAreaRelative val contentAreaRelative = calculator.domainRelative2contentAreaRelativeY(domainRelative)
    @ContentArea val contentArea = calculator.contentAreaRelative2contentAreaY(contentAreaRelative)
    @Zoomed val zoomed = calculator.contentArea2zoomedY(contentArea)
    @Window val window = calculator.zoomed2windowY(zoomed)

    Offset.offset(0.0000000001)
    assertThat(
      calculator.domainRelative2contentAreaRelativeY(domainRelative)
    ).isEqualTo(contentAreaRelative)

    Offset.offset(0.0000000001)
    assertThat(
      calculator.domainRelative2contentAreaY(domainRelative)
    ).isEqualTo(contentArea)

    Offset.offset(0.0000000001)
    assertThat(
      calculator.domainRelative2zoomedY(domainRelative)
    ).isEqualTo(zoomed)

    Offset.offset(0.0000000001)
    assertThat(
      calculator.domainRelative2windowY(domainRelative)
    ).isEqualTo(window)
  }

  @Test
  fun testTime2Window() {
    val timeRange = TimeRange(10_000.0, 15_000.0)
    chartState.contentAreaWidth = 800.0
    chartState.contentAreaHeight = 600.0
    chartState.axisOrientationY = AxisOrientationY.OriginAtTop

    testTime2WindowX(10_000.0, timeRange, 0.0)
    testTime2WindowX(11_000.0, timeRange, 160.0)
    testTime2WindowX(15_000.0, timeRange, 800.0)

    testTime2WindowY(10_000.0, timeRange, 0.0)
    testTime2WindowY(11_000.0, timeRange, 120.0)
    testTime2WindowY(15_000.0, timeRange, 600.0)

    chartState.zoomX = 2.0
    chartState.zoomY = 2.0
    testTime2WindowX(10_000.0, timeRange, 0.0)
    testTime2WindowX(11_000.0, timeRange, 320.0)
    testTime2WindowX(15_000.0, timeRange, 1600.0)

    testTime2WindowY(10_000.0, timeRange, 0.0)
    testTime2WindowY(11_000.0, timeRange, 240.0)
    testTime2WindowY(15_000.0, timeRange, 1200.0)

    chartState.windowTranslationX = 7.0
    chartState.windowTranslationY = 9.0

    testTime2WindowX(10_000.0, timeRange, 0.0 + 7)
    testTime2WindowX(11_000.0, timeRange, 320.0 + 7)
    testTime2WindowX(15_000.0, timeRange, 1600.0 + 7)

    testTime2WindowY(10_000.0, timeRange, 0.0 + 9)
    testTime2WindowY(11_000.0, timeRange, 240.0 + 9)
    testTime2WindowY(15_000.0, timeRange, 1200.0 + 9)
  }

  private fun testTime2WindowX(time: @ms Double, timeRange: TimeRange, expectedWindow: @Window Double) {
    assertThat(calculator.time2windowX(time, timeRange)).isEqualTo(expectedWindow)
    assertThat(calculator.window2timeX(expectedWindow, timeRange)).isEqualTo(time)
  }

  private fun testTime2WindowY(time: @ms Double, timeRange: TimeRange, expectedWindow: @Window Double) {
    assertThat(calculator.time2windowY(time, timeRange)).isEqualTo(expectedWindow)
    assertThat(calculator.window2timeY(expectedWindow, timeRange)).isEqualTo(time)
  }

  @Test
  fun testVisibleTimeRange() {
    val timeRange = TimeRange(10_000.0, 15_000.0)
    chartState.contentAreaWidth = 800.0
    chartState.contentAreaHeight = 600.0

    chartState.windowWidth = 250.0
    chartState.windowHeight = 150.0

    chartState.axisOrientationX = AxisOrientationX.OriginAtLeft
    chartState.axisOrientationY = AxisOrientationY.OriginAtTop

    assertThat(calculator.visibleTimeRangeXinWindow(timeRange)).isEqualTo(TimeRange.fromStartAndDuration(10_000.0, 5_000.0 / 800 * 250))
    assertThat(calculator.visibleTimeRangeYinWindow(timeRange)).isEqualTo(TimeRange.fromStartAndDuration(10_000.0, 5_000.0 / 600 * 150))

    chartState.axisOrientationY = AxisOrientationY.OriginAtBottom
    chartState.axisOrientationX = AxisOrientationX.OriginAtRight

    assertThat(calculator.visibleTimeRangeXinWindow(timeRange)).isEqualTo(TimeRange.fromEndAndDuration(15_000.0, 5_000.0 / 800 * 250))
    assertThat(calculator.visibleTimeRangeYinWindow(timeRange)).isEqualTo(TimeRange.fromEndAndDuration(15_000.0, 5_000.0 / 600 * 150))

    chartState.windowTranslationX = 100.0
    chartState.windowTranslationY = 30.0

    chartState.axisOrientationY = AxisOrientationY.OriginAtTop
    chartState.axisOrientationX = AxisOrientationX.OriginAtLeft


    assertThat(calculator.visibleTimeRangeXinWindow(timeRange)).isEqualTo(TimeRange.fromStartAndDuration(9375.0, 5_000.0 / 800 * 250))
    assertThat(calculator.visibleTimeRangeYinWindow(timeRange)).isEqualTo(TimeRange.fromStartAndDuration(9750.0, 5_000.0 / 600 * 150))

    chartState.axisOrientationY = AxisOrientationY.OriginAtBottom
    chartState.axisOrientationX = AxisOrientationX.OriginAtRight

    assertThat(calculator.visibleTimeRangeXinWindow(timeRange)).isEqualTo(TimeRange.fromStartAndDuration(14_062.5, 5_000.0 / 800 * 250))
    assertThat(calculator.visibleTimeRangeYinWindow(timeRange)).isEqualTo(TimeRange.fromStartAndDuration(14_000.0, 5_000.0 / 600 * 150))
  }

  @Test
  fun testRangeStuff() {
    val timeRange = TimeRange(10_000.0, 15_000.0)
    chartState.contentAreaWidth = 800.0
    chartState.contentAreaHeight = 600.0
    chartState.axisOrientationY = AxisOrientationY.OriginAtTop

    testTimeRelative2WindowX(0.0, 0.0)
    testTimeRelative2WindowX(0.2, 160.0)
    testTimeRelative2WindowX(1.0, 800.0)

    testTimeRelative2WindowY(0.0, 0.0)
    testTimeRelative2WindowY(0.2, 120.0)
    testTimeRelative2WindowY(1.0, 600.0)

    chartState.zoomX = 2.0
    chartState.zoomY = 2.0
    testTimeRelative2WindowX(0.0, 0.0)
    testTimeRelative2WindowX(0.2, 320.0)
    testTimeRelative2WindowX(1.0, 1600.0)

    testTimeRelative2WindowY(0.0, 0.0)
    testTimeRelative2WindowY(0.2, 240.0)
    testTimeRelative2WindowY(1.0, 1200.0)

    chartState.windowTranslationX = 7.0
    chartState.windowTranslationY = 9.0

    testTimeRelative2WindowX(0.0, 0.0 + 7)
    testTimeRelative2WindowX(0.2, 320.0 + 7)
    testTimeRelative2WindowX(1.0, 1600.0 + 7)

    testTimeRelative2WindowY(0.0, 0.0 + 9)
    testTimeRelative2WindowY(0.2, 240.0 + 9)
    testTimeRelative2WindowY(1.0, 1200.0 + 9)
  }

  private fun testTimeRelative2WindowX(timeRelative: @pct Double, expectedWindow: @Window Double) {
    assertThat(calculator.timeRelative2windowX(timeRelative)).isEqualTo(expectedWindow)
    assertThat(calculator.window2timeRelativeX(expectedWindow)).isEqualTo(timeRelative)
  }

  private fun testTimeRelative2WindowY(timeRelative: @pct Double, expectedWindow: @Window Double) {
    assertThat(calculator.timeRelative2windowY(timeRelative)).isEqualTo(expectedWindow)
    assertThat(calculator.window2timeRelativeY(expectedWindow)).isEqualTo(timeRelative)
  }

  @Test
  fun testTileIndex2Window() {
    val tileSize = Size(300.0, 200.0)
    assertThat(calculator.tileIndex2window(TileIndex(0, 0, 0, 0), tileSize)).isEqualTo(Coordinates.none)
    assertThat(calculator.tileIndex2window(TileIndex(0, 1, 0, 0), tileSize)).isEqualTo(Coordinates(300.0, 0.0))
    assertThat(calculator.tileIndex2window(TileIndex(0, 10, 0, 0), tileSize)).isEqualTo(Coordinates(3000.0, 0.0))
    assertThat(calculator.tileIndex2window(TileIndex(0, 10, 0, 7), tileSize)).isEqualTo(Coordinates(3000.0, 1400.0))

    chartState.windowTranslationX = 10.0
    chartState.windowTranslationY = 20.0

    assertThat(calculator.tileIndex2window(TileIndex(0, 0, 0, 0), tileSize)).isEqualTo(Coordinates(10.0, 20.0))
    assertThat(calculator.tileIndex2window(TileIndex(0, 1, 0, 0), tileSize)).isEqualTo(Coordinates(300.0 + 10, 0.0 + 20))
    assertThat(calculator.tileIndex2window(TileIndex(0, 10, 0, 0), tileSize)).isEqualTo(Coordinates(3000.0 + 10, 0.0 + 20))
    assertThat(calculator.tileIndex2window(TileIndex(0, 10, 0, 7), tileSize)).isEqualTo(Coordinates(3000.0 + 10, 1400.0 + 20))


    chartState.windowTranslationX = 0.0
    chartState.windowTranslationY = 0.0

    chartState.zoomX = 2.0
    chartState.zoomY = 3.0
    //the zoom does not change anything

    assertThat(calculator.tileIndex2window(TileIndex(0, 0, 0, 0), tileSize)).isEqualTo(Coordinates(0.0, 0.0))
    assertThat(calculator.tileIndex2window(TileIndex(0, 1, 0, 0), tileSize)).isEqualTo(Coordinates(300.0, 0.0))
    assertThat(calculator.tileIndex2window(TileIndex(0, 10, 0, 0), tileSize)).isEqualTo(Coordinates(3000.0, 0.0))
    assertThat(calculator.tileIndex2window(TileIndex(0, 10, 0, 7), tileSize)).isEqualTo(Coordinates(3000.0, 1400.0))

    chartState.windowTranslationX = 10.0
    chartState.windowTranslationY = 20.0

    assertThat(calculator.tileIndex2window(TileIndex(0, 0, 0, 0), tileSize)).isEqualTo(Coordinates(10.0, 20.0))
    assertThat(calculator.tileIndex2window(TileIndex(0, 1, 0, 0), tileSize)).isEqualTo(Coordinates(300.0 + 10, 0.0 + 20))
    assertThat(calculator.tileIndex2window(TileIndex(0, 10, 0, 0), tileSize)).isEqualTo(Coordinates(3000.0 + 10, 0.0 + 20))
    assertThat(calculator.tileIndex2window(TileIndex(0, 10, 0, 7), tileSize)).isEqualTo(Coordinates(3000.0 + 10, 1400.0 + 20))
  }

  @Test
  fun testWindow2tileIndex() {
    val tileSize = Size(300.0, 200.0)

    assertThat(calculator.window2tileIndex(Coordinates(0.0, 0.0), tileSize)).isEqualTo(TileIndex(0, 0, 0, 0))
    assertThat(calculator.window2tileIndex(Coordinates(1.0, 0.0), tileSize)).isEqualTo(TileIndex(0, 0, 0, 0))
    assertThat(calculator.window2tileIndex(Coordinates(300.0, 0.0), tileSize)).isEqualTo(TileIndex(0, 1, 0, 0))
    assertThat(calculator.window2tileIndex(Coordinates(301.0, 0.0), tileSize)).isEqualTo(TileIndex(0, 1, 0, 0))
    assertThat(calculator.window2tileIndex(Coordinates(301.0, 199.0), tileSize)).isEqualTo(TileIndex(0, 1, 0, 0))
    assertThat(calculator.window2tileIndex(Coordinates(301.0, 200.0), tileSize)).isEqualTo(TileIndex(0, 1, 0, 1))
    assertThat(calculator.window2tileIndex(Coordinates(301.0, 201.0), tileSize)).isEqualTo(TileIndex(0, 1, 0, 1))

    chartState.windowTranslationX = 100.0
    chartState.windowTranslationY = 700.0

    assertThat(calculator.window2tileIndex(Coordinates(0.0, 0.0), tileSize)).isEqualTo(TileIndex(-1, TileIndex.SubIndexFactor - 1, -1, TileIndex.SubIndexFactor - 4))

    chartState.zoomX = 10.0
    chartState.zoomY = 30.0
    //does not change anything

    assertThat(calculator.window2tileIndex(Coordinates(0.0, 0.0), tileSize)).isEqualTo(TileIndex(-1, TileIndex.SubIndexFactor - 1, -1, TileIndex.SubIndexFactor - 4))
  }

  @Test
  fun testContentArea2TileIndex() {
    val tileSize = Size(300.0, 200.0)

    assertThat(calculator.contentArea2tileIndex(Coordinates(0.0, 0.0), tileSize)).isEqualTo(TileIndex(0, 0, 0, 0))
    assertThat(calculator.contentArea2tileIndex(Coordinates(1.0, 0.0), tileSize)).isEqualTo(TileIndex(0, 0, 0, 0))
    assertThat(calculator.contentArea2tileIndex(Coordinates(300.0, 0.0), tileSize)).isEqualTo(TileIndex(0, 1, 0, 0))
    assertThat(calculator.contentArea2tileIndex(Coordinates(301.0, 0.0), tileSize)).isEqualTo(TileIndex(0, 1, 0, 0))
    assertThat(calculator.contentArea2tileIndex(Coordinates(301.0, 199.0), tileSize)).isEqualTo(TileIndex(0, 1, 0, 0))
    assertThat(calculator.contentArea2tileIndex(Coordinates(301.0, 200.0), tileSize)).isEqualTo(TileIndex(0, 1, 0, 1))
    assertThat(calculator.contentArea2tileIndex(Coordinates(301.0, 201.0), tileSize)).isEqualTo(TileIndex(0, 1, 0, 1))

    chartState.windowTranslationX = 100.0
    chartState.windowTranslationY = 700.0
    //does not have any effect

    assertThat(calculator.contentArea2tileIndex(Coordinates(301.0, 199.0), tileSize)).isEqualTo(TileIndex(0, 1, 0, 0))
    assertThat(calculator.contentArea2tileIndex(Coordinates(301.0, 200.0), tileSize)).isEqualTo(TileIndex(0, 1, 0, 1))

    chartState.zoomX = 2.0
    chartState.zoomY = 3.0

    assertThat(calculator.contentArea2tileIndex(Coordinates(0.0, 0.0), tileSize)).isEqualTo(TileIndex(0, 0, 0, 0))
    assertThat(calculator.contentArea2tileIndex(Coordinates(1.0, 0.0), tileSize)).isEqualTo(TileIndex(0, 0, 0, 0))

    assertThat(calculator.contentArea2tileIndex(Coordinates(300.0 / 2, 0.0 / 3), tileSize)).isEqualTo(TileIndex(0, 1, 0, 0))
    assertThat(calculator.contentArea2tileIndex(Coordinates(301.0 / 2, 0.0 / 3), tileSize)).isEqualTo(TileIndex(0, 1, 0, 0))
    assertThat(calculator.contentArea2tileIndex(Coordinates(301.0 / 2, 199.0 / 3), tileSize)).isEqualTo(TileIndex(0, 1, 0, 0))
    assertThat(calculator.contentArea2tileIndex(Coordinates(301.0 / 2, 200.0 / 3), tileSize)).isEqualTo(TileIndex(0, 1, 0, 1))
    assertThat(calculator.contentArea2tileIndex(Coordinates(301.0 / 2, 201.0 / 3), tileSize)).isEqualTo(TileIndex(0, 1, 0, 1))
  }

  @Test
  fun testTileIndex2ContentArea() {
    val tileSize = Size(300.0, 200.0)
    assertThat(calculator.tileIndex2contentArea(TileIndex(0, 0, 0, 0), tileSize)).isEqualTo(Coordinates.none)
    assertThat(calculator.tileIndex2contentArea(TileIndex(0, 1, 0, 0), tileSize)).isEqualTo(Coordinates(300.0, 0.0))
    assertThat(calculator.tileIndex2contentArea(TileIndex(0, 10, 0, 0), tileSize)).isEqualTo(Coordinates(3000.0, 0.0))
    assertThat(calculator.tileIndex2contentArea(TileIndex(0, 10, 0, 7), tileSize)).isEqualTo(Coordinates(3000.0, 1400.0))

    chartState.windowTranslationX = 100.0
    chartState.windowTranslationY = 700.0
    //does not have any effect

    assertThat(calculator.tileIndex2contentArea(TileIndex(0, 0, 0, 0), tileSize)).isEqualTo(Coordinates.none)
    assertThat(calculator.tileIndex2contentArea(TileIndex(0, 1, 0, 0), tileSize)).isEqualTo(Coordinates(300.0, 0.0))
    assertThat(calculator.tileIndex2contentArea(TileIndex(0, 10, 0, 0), tileSize)).isEqualTo(Coordinates(3000.0, 0.0))
    assertThat(calculator.tileIndex2contentArea(TileIndex(0, 10, 0, 7), tileSize)).isEqualTo(Coordinates(3000.0, 1400.0))

    chartState.zoomX = 2.0
    chartState.zoomY = 3.0

    assertThat(calculator.tileIndex2contentArea(TileIndex(0, 0, 0, 0), tileSize)).isEqualTo(Coordinates.none)
    assertThat(calculator.tileIndex2contentArea(TileIndex(0, 1, 0, 0), tileSize)).isEqualTo(Coordinates(300.0 / 2, 0.0))
    assertThat(calculator.tileIndex2contentArea(TileIndex(0, 10, 0, 0), tileSize)).isEqualTo(Coordinates(3000.0 / 2, 0.0))
    assertThat(calculator.tileIndex2contentArea(TileIndex(0, 10, 0, 7), tileSize)).isEqualTo(Coordinates(3000.0 / 2, 1400.0 / 3))
  }
}

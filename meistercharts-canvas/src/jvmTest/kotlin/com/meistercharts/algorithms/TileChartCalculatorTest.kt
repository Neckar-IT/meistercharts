package com.meistercharts.algorithms

import assertk.*
import assertk.assertions.*
import com.meistercharts.algorithms.axis.AxisOrientationX
import com.meistercharts.algorithms.axis.AxisOrientationY
import com.meistercharts.algorithms.impl.DefaultChartState
import com.meistercharts.algorithms.layers.tileCalculator
import com.meistercharts.algorithms.tile.TileIndex
import com.meistercharts.annotations.ContentArea
import com.meistercharts.annotations.ContentAreaRelative
import com.meistercharts.annotations.Tile
import com.meistercharts.annotations.TimeRelative
import com.meistercharts.canvas.tileRelative2TimeX
import com.meistercharts.model.Distance
import com.meistercharts.model.Size
import com.meistercharts.model.Zoom
import it.neckar.open.unit.quantity.Time
import it.neckar.open.unit.si.ms
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class TileChartCalculatorTest {
  lateinit var chartState: DefaultChartState
  lateinit var calculator: TileChartCalculator

  @BeforeEach
  fun setUp() {
    chartState = DefaultChartState()
    calculator = TileChartCalculator(chartState, TileIndex(4, 7), Size(200.0, 300.0))
    chartState.contentAreaWidth = 1000.0
    chartState.contentAreaHeight = 600.0

    chartState.axisOrientationX = AxisOrientationX.OriginAtLeft
    chartState.axisOrientationY = AxisOrientationY.OriginAtTop

    assertThat(chartState.contentAreaWidth).isEqualTo(1000.0)
    assertThat(chartState.contentAreaHeight).isEqualTo(600.0)
  }

  @Test
  fun testBugRealWorld() {
    val contentAreaTimeRange = TimeRange(1.58981589118E12, 1.58981595118E12).also {
      assertThat(it.start).isEqualTo(1.58981589118E12)
      assertThat(it.end).isEqualTo(1.58981595118E12)
      assertThat(it.span).isEqualTo(60_000.0)
    }
    assertThat(chartState.contentAreaWidth).isEqualTo(1000.0)

    calculator = TileChartCalculator(chartState, TileIndex(4, 0), Size(400.0, 300.0))

    //the content area is 2.5 tiles wide
    @ms val expectedTileWidthInMillis = contentAreaTimeRange.span / 2.5

    // tile index 3
    TileChartCalculator(chartState, TileIndex(3, 0), Size(400.0, 300.0)).also { tileChartCalculator ->
      @ms val startTime = tileChartCalculator.tileRelative2TimeX(0.0, contentAreaTimeRange)
      assertThat(startTime).isEqualTo(1.58981596318E12)
      @ms val endTime = tileChartCalculator.tileRelative2TimeX(1.0, contentAreaTimeRange)
      assertThat(endTime).isEqualTo(1.58981598718E12)

      assertThat(endTime - startTime).isEqualTo(expectedTileWidthInMillis)
    }

    // tile index 4
    TileChartCalculator(chartState, TileIndex(4, 0), Size(400.0, 300.0)).also { tileChartCalculator ->
      @ms val startTime = tileChartCalculator.tileRelative2TimeX(0.0, contentAreaTimeRange)
      assertThat(startTime).isEqualTo(1.58981598718E12) //end time from tile index 3
      @ms val endTime = tileChartCalculator.tileRelative2TimeX(1.0, contentAreaTimeRange)
      assertThat(endTime).isEqualTo(1.58981598718E12 + expectedTileWidthInMillis)

      assertThat(endTime - startTime).isEqualTo(expectedTileWidthInMillis)
    }
  }

  @Test
  fun testTimeBugReproduce() {
    val contentAreaTimeRange = TimeRange.fromStartAndDuration(100_000.0, 1_000.0).also {
      assertThat(it.start).isEqualTo(100_000.0)
      assertThat(it.end).isEqualTo(100_000.0 + 1_000.0)
      assertThat(it.span).isEqualTo(1_000.0)
    }

    assertThat(calculator.tileIndex).isEqualTo(TileIndex(4, 7))

    val visibleTimeRange = calculator.visibleTimeRangeXinTile(contentAreaTimeRange)

    //content are has a width of 5 tiles!
    val timeSpanPerTile = contentAreaTimeRange.span / 5.0
    assertThat(timeSpanPerTile).isEqualTo(200.0)

    assertThat(visibleTimeRange.start).isEqualTo(100_000.0 + 4 * timeSpanPerTile)
    assertThat(visibleTimeRange.end).isEqualTo(100_000.0 + 5 * timeSpanPerTile)

    assertThat(calculator.tile2timeX(0.0, contentAreaTimeRange)).isEqualTo(100_000.0 + 4 * timeSpanPerTile)
    assertThat(calculator.tile2timeX(200.0, contentAreaTimeRange)).isEqualTo(100_000.0 + 5 * timeSpanPerTile)


    assertThat(calculator.time2tileX(100_000.0 + 4 * timeSpanPerTile, contentAreaTimeRange)).isEqualTo(0.0)
    assertThat(calculator.time2tileX(100_000.0 + 5 * timeSpanPerTile, contentAreaTimeRange)).isEqualTo(200.0)


    assertThat(calculator.tileOrigin2timeX(contentAreaTimeRange)).isEqualTo(100_000.0 + 4 * timeSpanPerTile)

    assertThat(calculator.tileRelative2TimeX(0.0, contentAreaTimeRange)).isEqualTo(100_000.0 + 4 * timeSpanPerTile)
    assertThat(calculator.tileRelative2TimeX(1.0, contentAreaTimeRange)).isEqualTo(100_000.0 + 5 * timeSpanPerTile)
  }

  @Test
  fun testOrigin() {
    assertThat(calculator.tileOrigin2contentAreaX()).isEqualTo(4 * 200.0)
    assertThat(calculator.contentArea2tileX(4 * 200.0)).isEqualTo(0.0)

    //reverse

    assertThat(calculator.tileOrigin2contentAreaY()).isEqualTo(7 * 300.0)
    assertThat(calculator.contentArea2tileY(7 * 300.0)).isEqualTo(0.0)
  }

  @Test
  fun testOriginRelative() {
    assertThat(calculator.tileOrigin2contentAreaRelativeX()).isEqualTo(1.0 / 1000 * (4 * 200))
    assertThat(calculator.origin2contentAreaRelativeY()).isCloseTo(1.0 / 600.0 * (7 * 300.0), 0.0001)

    //reverse

    assertThat(calculator.contentAreaRelative2tileX(1.0 / 1000 * (4 * 200))).isCloseTo(0.0, 0.0001)
    assertThat(calculator.contentAreaRelative2tileY(1.0 / 600.0 * (7 * 300.0))).isCloseTo(0.0, 0.0001)
  }

  @Test
  fun testTile2Relative() {
    assertThat(calculator.tile2tileRelativeX(0.0)).isEqualTo(0.0)
    assertThat(calculator.tile2tileRelativeX(100.0)).isEqualTo(0.5)
    assertThat(calculator.tile2tileRelativeX(200.0)).isEqualTo(1.0)
    assertThat(calculator.tile2tileRelativeX(200.0 * 2)).isEqualTo(2.0)

    assertThat(calculator.tile2tileRelativeY(0.0)).isEqualTo(0.0)
    assertThat(calculator.tile2tileRelativeY(150.0)).isEqualTo(0.5)
    assertThat(calculator.tile2tileRelativeY(300.0)).isEqualTo(1.0)
    assertThat(calculator.tile2tileRelativeY(300.0 * 2)).isEqualTo(2.0)

    //reverse

    assertThat(calculator.tileRelative2tileX(0.0)).isEqualTo(0.0)
    assertThat(calculator.tileRelative2tileX(0.5)).isEqualTo(100.0)
    assertThat(calculator.tileRelative2tileX(1.0)).isEqualTo(200.0)
    assertThat(calculator.tileRelative2tileX(2.0)).isEqualTo(200.0 * 2)

    assertThat(calculator.tileRelative2tileY(0.0)).isEqualTo(0.0)
    assertThat(calculator.tileRelative2tileY(0.5)).isEqualTo(150.0)
    assertThat(calculator.tileRelative2tileY(1.0)).isEqualTo(300.0)
    assertThat(calculator.tileRelative2tileY(2.0)).isEqualTo(300.0 * 2)
  }

  @Test
  fun testTile2ContentAreaRelative() {
    chartState.zoom = Zoom.default

    calculator.testTile2ContentAreaRelativeX(0.0, 1.0 / 1000 * (4 * 200 + 0))
    calculator.testTile2ContentAreaRelativeX(100.0, 1.0 / 1000 * (4 * 200 + 100))
    calculator.testTile2ContentAreaRelativeX(1000.0, 1.0 / 1000 * (4 * 200 + 1000))

    calculator.testTile2ContentAreaRelativeY(0.0, 1.0 / 600 * (7 * 300 + 0))
    calculator.testTile2ContentAreaRelativeY(100.0, 1.0 / 600 * (7 * 300 + 100))
    calculator.testTile2ContentAreaRelativeY(1000.0, 1.0 / 600 * (7 * 300 + 1000))
  }

  @Test
  fun testTile2ContentArea() {
    chartState.zoom = Zoom.default

    calculator.testTile2ContentAreaX(0.0, 4 * 200 + 0.0)
    calculator.testTile2ContentAreaX(100.0, 4 * 200 + 100.0)
    calculator.testTile2ContentAreaX(1000.0, 4 * 200 + 1000.0)

    calculator.testTile2ContentAreaY(0.0, 7 * 300 + 0.0)
    calculator.testTile2ContentAreaY(100.0, 7 * 300 + 100.0)
    calculator.testTile2ContentAreaY(1000.0, 7 * 300 + 1000.0)

    //Now zoom
    chartState.zoom = Zoom(2.0, 3.0)

    calculator.testTile2ContentAreaX(0.0, (4 * 200 + 0.0) / 2)
    calculator.testTile2ContentAreaX(100.0, (4 * 200 + 100.0) / 2)
    calculator.testTile2ContentAreaX(1000.0, (4 * 200 + 1000.0) / 2)

    calculator.testTile2ContentAreaY(0.0, (7 * 300 + 0.0) / 3.0)
    calculator.testTile2ContentAreaY(100.0, (7 * 300 + 100.0) / 3.0)
    calculator.testTile2ContentAreaY(1000.0, (7 * 300 + 1000.0) / 3.0)

    //Translation does *not* have any effect!
    chartState.windowTranslation = Distance.of(234234.0, 123123213.0)

    calculator.testTile2ContentAreaX(1000.0, (4 * 200 + 1000.0) / 2)
    calculator.testTile2ContentAreaY(1000.0, (7 * 300 + 1000.0) / 3.0)
  }

  @Test
  internal fun testTime2Relative() {
    val timeRange = TimeRange(200.0, 300.0)

    assertThat(timeRange.time2relative(220.0)).isEqualTo(0.2)
    assertThat(timeRange.time2relativeDelta(20.0)).isEqualTo(0.2)
    //Reverse
    assertThat(timeRange.relative2time(0.2)).isEqualTo(220.0)
    assertThat(timeRange.relative2timeDelta(0.2)).isEqualTo(20.0)
  }

  @Test
  fun testTime2tileYOriginAtTop() {
    assertThat(calculator.tileHeight).isEqualTo(300.0)
    assertThat(calculator.tileIndex.y).isEqualTo(7)
    chartState.contentAreaHeight = 600.0
    chartState.axisOrientationY = AxisOrientationY.OriginAtTop

    //content are is 2,0 tiles high
    //1 tile has a "time height" of 150 ms
    val timeRange = TimeRange(200.0, 300.0)

    assertThat(calculator.tile2contentAreaRelativeY(0.0)).isEqualTo(3.5)
    assertThat(calculator.tile2timeY(0.0, timeRange)).isEqualTo(200 + 7 * 50.0)

    //reverse
    assertThat(calculator.contentAreaRelative2tileY(3.5)).isEqualTo(0.0)

    //Inlined time2tiley method
    kotlin.run {
      @TimeRelative val timeRelative = timeRange.time2relative(200 + 7 * 50.0)
      assertThat(timeRelative).isEqualTo(3.5)
      assertThat(3.5 * 100.0 + 200).isEqualTo(200 + 7 * 50.0)

      @ContentAreaRelative val contentAreaRelative = calculator.domainRelative2contentAreaRelativeY(timeRelative)
      assertThat(contentAreaRelative).isEqualTo(3.5)

      val tileY = calculator.contentAreaRelative2tileY(contentAreaRelative)
      assertThat(tileY).isEqualTo(0.0)
    }

    //reverse
    assertThat(calculator.time2tileY(200 + 7 * 50.0, timeRange)).isEqualTo(0.0)
  }

  @Test
  fun testTime2tileYOriginAtBottom() {
    assertThat(calculator.tileHeight).isEqualTo(300.0)
    assertThat(calculator.tileIndex.y).isEqualTo(7)
    chartState.contentAreaHeight = 600.0
    chartState.axisOrientationY = AxisOrientationY.OriginAtBottom

    //content are is 2,0 tiles high
    //1 tile has a "time height" of 50 ms

    val timeRange = TimeRange(200.0, 300.0)

    assertThat(calculator.tile2contentAreaRelativeY(0.0)).isEqualTo(3.5)
    //reverse
    assertThat(calculator.contentAreaRelative2tileY(3.5)).isEqualTo(0.0)

    assertThat(calculator.contentAreaRelative2domainRelativeY(3.5)).isEqualTo(-2.5)
    //reverse
    assertThat(calculator.domainRelative2contentAreaRelativeY(-2.5)).isEqualTo(3.5)


    assertThat(calculator.tile2timeY(0.0, timeRange)).isEqualTo(200 - 5 * 50.0)
    //reverse
    assertThat(calculator.time2tileY(200 - 5 * 50.0, timeRange)).isEqualTo(0.0)
  }

  @Test
  fun testContentAreaRelative2tileX() {
    assertThat(calculator.tile2contentAreaX(0.0)).isEqualTo(4 * 200.0)
    assertThat(calculator.contentArea2tileX(4 * 200.0)).isEqualTo(0.0) //reverse

    //manual steps
    assertThat(calculator.chartState.contentAreaWidth).isEqualTo(1000.0)
    assertThat(calculator.contentArea2contentAreaRelativeX(800.0)).isEqualTo(0.8)

    assertThat(calculator.contentAreaRelative2tileX(0.8)).isEqualTo(0.0)
    //reverse
    assertThat(calculator.tile2contentAreaRelativeX(0.0)).isEqualTo(0.8)
  }

  @Test
  fun testTimeRange() {
    assertThat(calculator.tileIndex).isEqualTo(TileIndex(4, 7))
    assertThat(calculator.tileWidth).isEqualTo(200.0)
    assertThat(calculator.tileHeight).isEqualTo(300.0)
    assertThat(calculator.chartState.contentAreaWidth).isEqualTo(1000.0)

    //content area is 5 tiles wide
    //content are is 2,0 tiles high

    val timeRange = TimeRange(10_000.0, 15_000.0)

    assertThat(calculator.tileOrigin2timeX(timeRange)).isEqualTo(14_000.0)
    assertThat(calculator.tileOrigin2timeY(timeRange)).isEqualTo(27_500.0)
    assertThat(calculator.tile2timeX(0.0, timeRange)).isEqualTo(14_000.0)
    assertThat(calculator.tile2timeY(0.0, timeRange)).isEqualTo(27_500.0)
    // Revert
    assertThat(calculator.time2tileX(14_000.0, timeRange)).isEqualTo(0.0)
    assertThat(calculator.time2tileY(27_500.0, timeRange)).isEqualTo(0.0)

    calculator.testTile2TimeX(0.0, timeRange, 14_000.0)
    calculator.testTile2TimeX(300.0, timeRange, 15_500.0)
    calculator.testTile2TimeX(400.0, timeRange, 16_000.0)

    chartState.zoomX = 2.0

    assertThat(calculator.tileOrigin2timeX(timeRange)).isEqualTo(12_000.0)

    calculator.testTile2TimeX(0.0, timeRange, 12_000.0)
    calculator.testTile2TimeX(300.0, timeRange, 12_750.0)
    calculator.testTile2TimeX(400.0, timeRange, 13_000.0)
  }

  private fun TileChartCalculator.testTile2TimeX(tile: @Tile Double, contentAreaTimeRange: TimeRange, expected: @Time Double) {
    assertThat(tile2timeX(tile, contentAreaTimeRange)).isEqualTo(expected)
    //Test reverse
    assertThat(time2tileX(expected, contentAreaTimeRange)).isEqualTo(tile)
  }

  @Test
  fun testTimeRangeSimple() {
    chartState.contentAreaWidth = 2000.0
    chartState.contentAreaHeight = 1000.0

    val tileSize = Size(500.0, 200.0) //1/4 / 1/5 of content area with zoom 1/1
    val timeRange = TimeRange(100_000.0, 110_000.0)

    TileChartCalculator(chartState, TileIndex(0, 0), tileSize).also {
      assertThat(it.tileOrigin2contentAreaRelativeX()).isEqualTo(0.0)
      assertThat(it.tileOrigin2contentAreaRelativeY()).isEqualTo(0.0)

      assertThat(it.tileOrigin2timeX(timeRange)).isEqualTo(100_000.0)
      assertThat(it.tileOrigin2timeY(timeRange)).isEqualTo(100_000.0)

      assertThat(it.tile2timeX(0.0, timeRange)).isEqualTo(100_000.0)
      assertThat(it.tile2timeX(500.0, timeRange)).isEqualTo(100_000.0 + 2500)

      assertThat(it.visibleTimeRangeXinTile(timeRange)).isEqualTo(TimeRange(100_000.0, 100_000.0 + 2500))
    }

    TileChartCalculator(chartState, TileIndex(1, 1), tileSize).also {
      assertThat(it.tileOrigin2contentAreaRelativeX()).isEqualTo(0.25)
      assertThat(it.tileOrigin2contentAreaRelativeY()).isEqualTo(0.2)

      assertThat(it.tileOrigin2timeX(timeRange)).isEqualTo(100_000.0 + 2500)
      assertThat(it.tileOrigin2timeY(timeRange)).isEqualTo(100_000.0 + 2000)

      assertThat(it.tile2timeX(0.0, timeRange)).isEqualTo(100_000.0 + 2500)
      assertThat(it.tile2timeX(500.0, timeRange)).isEqualTo(100_000.0 + 2500 + 2500)

      assertThat(it.visibleTimeRangeXinTile(timeRange)).isEqualTo(TimeRange(100_000.0 + 2500, 100_000.0 + 2500 + 2500))
    }

    TileChartCalculator(chartState, TileIndex(2, 2), tileSize).also {
      assertThat(it.tileOrigin2contentAreaRelativeX()).isEqualTo(0.5)
      assertThat(it.tileOrigin2contentAreaRelativeY()).isEqualTo(0.4)

      assertThat(it.tileOrigin2timeX(timeRange)).isEqualTo(100_000.0 + 2500 * 2)
      assertThat(it.tileOrigin2timeY(timeRange)).isEqualTo(100_000.0 + 2000 * 2)

      assertThat(it.tile2timeX(0.0, timeRange)).isEqualTo(100_000.0 + 2500 * 2)
      assertThat(it.tile2timeX(500.0, timeRange)).isEqualTo(100_000.0 + 2500 * 2 + 2500)

      assertThat(it.visibleTimeRangeXinTile(timeRange)).isEqualTo(TimeRange(100_000.0 + 2500 * 2, 100_000.0 + 2500 * 3))
    }

    //Set zoom!
    chartState.zoomX = 10.0
    chartState.zoomY = 10.0

    TileChartCalculator(chartState, TileIndex(0, 0), tileSize).also {
      assertThat(it.tileOrigin2contentAreaRelativeX()).isEqualTo(0.0)
      assertThat(it.tileOrigin2contentAreaRelativeY()).isEqualTo(0.0)

      assertThat(it.tileOrigin2timeX(timeRange)).isEqualTo(100_000.0)
      assertThat(it.tileOrigin2timeY(timeRange)).isEqualTo(100_000.0)

      assertThat(it.tile2timeX(0.0, timeRange)).isEqualTo(100_000.0)
      assertThat(it.tile2timeX(500.0, timeRange)).isEqualTo(100_000.0 + 250)

      assertThat(it.visibleTimeRangeXinTile(timeRange)).isEqualTo(TimeRange(100_000.0, 100_000.0 + 250))
    }

    TileChartCalculator(chartState, TileIndex(1, 1), tileSize).also {
      assertThat(it.tileOrigin2contentAreaRelativeX()).isEqualTo(0.025)
      assertThat(it.tileOrigin2contentAreaRelativeY()).isEqualTo(0.02)

      assertThat(it.tileOrigin2timeX(timeRange)).isEqualTo(100_000.0 + 250)
      assertThat(it.tileOrigin2timeY(timeRange)).isEqualTo(100_000.0 + 200)

      assertThat(it.tile2timeX(0.0, timeRange)).isEqualTo(100_000.0 + 250)
      assertThat(it.tile2timeX(500.0, timeRange)).isEqualTo(100_000.0 + 250 * 2)

      assertThat(it.visibleTimeRangeXinTile(timeRange)).isEqualTo(TimeRange(100_000.0 + 250, 100_000.0 + 250 * 2))
    }
  }

  private fun TileChartCalculator.testTile2ContentAreaX(tile: @Tile Double, expected: @ContentArea Double) {
    assertThat(tile2contentAreaX(tile)).isCloseTo(expected, 0.0001)
    //Test reverse
    assertThat(contentArea2tileX(expected)).isCloseTo(tile, 0.0001)
  }

  private fun TileChartCalculator.testTile2ContentAreaY(tile: @Tile Double, expected: @ContentArea Double) {
    assertThat(tile2contentAreaY(tile)).isCloseTo(expected, 0.0001)
    //Test reverse
    assertThat(contentArea2tileY(expected)).isCloseTo(tile, 0.0001)
  }

  private fun TileChartCalculator.testTile2ContentAreaRelativeX(tile: @Tile Double, expected: @ContentAreaRelative Double) {
    assertThat(tile2contentAreaRelativeX(tile)).isCloseTo(expected, 0.0001)
    //Test reverse
    assertThat(contentAreaRelative2tileX(expected)).isCloseTo(tile, 0.0001)
  }

  private fun TileChartCalculator.testTile2ContentAreaRelativeY(tile: @Tile Double, expected: @ContentAreaRelative Double) {
    assertThat(tile2contentAreaRelativeY(tile)).isCloseTo(expected, 0.0001)
    //Test reverse
    assertThat(contentAreaRelative2tileY(expected)).isCloseTo(tile, 0.0001)
  }

  @Test
  fun testZoomed2Time() {
    val timeRange = TimeRange(10_000.0, 15_000.0)

    assertThat(chartState.contentAreaWidth).isEqualTo(1000.0)

    //Relative!!!
    assertThat(calculator.zoomed2timeDeltaX(0.0, timeRange)).isEqualTo(0_000.0)
    assertThat(calculator.zoomed2timeDeltaX(1000.0, timeRange)).isEqualTo(5_000.0)

    assertThat(calculator.window2timeX(0.0, timeRange)).isEqualTo(10_000.0)
    assertThat(calculator.window2timeX(1000.0, timeRange)).isEqualTo(15_000.0)

    //Translate
    chartState.windowTranslationX = 30.0

    //Translation *not* included
    assertThat(calculator.zoomed2timeDeltaX(0.0, timeRange)).isEqualTo(0_000.0)
    assertThat(calculator.zoomed2timeDeltaX(1000.0, timeRange)).isEqualTo(5_000.0)

    //Translation is included
    assertThat(calculator.window2timeX(30.0, timeRange)).isEqualTo(10_000.0)
    assertThat(calculator.window2timeX(1030.0, timeRange)).isEqualTo(15_000.0)

    assertThat(calculator.window2timeX(0.0, timeRange)).isEqualTo(9_850.0)
    assertThat(calculator.window2timeX(1000.0, timeRange)).isEqualTo(14_850.0)
  }

  @Test
  fun testVisibleInTileX() {
    chartState.zoom = Zoom.default
    assertThat(chartState.contentAreaWidth).isEqualTo(1000.0)
    assertThat(calculator.tileWidth).isEqualTo(200.0)

    val timeRange = TimeRange(10_000.0, 15_000.0)

    assertThat(calculator.tileIndex).isEqualTo(TileIndex(4, 7))
    assertThat(calculator.tileWidth).isEqualTo(200.0)

    assertThat(calculator.tileOrigin2contentAreaRelativeX()).isEqualTo(0.8)
    assertThat(calculator.tile2contentAreaRelativeX(200.0)).isEqualTo(1.0)

    assertThat(calculator.tileOrigin2timeX(timeRange)).isEqualTo(14_000.0)

    assertThat(calculator.tile2timeX(0.0, timeRange)).isEqualTo(14_000.0)
    assertThat(calculator.tile2timeX(200.0, timeRange)).isEqualTo(15_000.0)
    assertThat(calculator.tile2timeX(1000.0, timeRange)).isEqualTo(19_000.0)
    assertThat(calculator.visibleTimeRangeXinTile(timeRange)).isEqualTo(TimeRange(14_000.0, 15_000.0))
  }

  @Test
  fun testVisibleInTileY() {
    chartState.zoom = Zoom.default
    assertThat(chartState.contentAreaHeight).isEqualTo(600.0)
    val timeRange = TimeRange(10_000.0, 15_000.0)

    assertThat(calculator.tileIndex).isEqualTo(TileIndex(4, 7))
    assertThat(calculator.tileHeight).isEqualTo(300.0)

    assertThat(calculator.tileOrigin2contentAreaRelativeY()).isEqualTo(3.5)
    assertThat(calculator.tile2contentAreaRelativeY(300.0)).isEqualTo(4.0)

    assertThat(calculator.tileOrigin2timeY(timeRange)).isEqualTo(27_500.0)

    assertThat(calculator.tile2timeY(0.0, timeRange)).isEqualTo(27_500.0)
    assertThat(calculator.tile2timeY(300.0, timeRange)).isEqualTo(30_000.0)
    assertThat(calculator.tile2timeY(1200.0, timeRange)).isEqualTo(37_500.0)
    assertThat(calculator.visibleTimeRangeYinTile(timeRange)).isEqualTo(TimeRange(27_500.0, 30_000.0))
  }

  @Test
  fun testWithZoom() {
    val zoomOverride = Zoom(2.0, 4.0)
    val changedChartState = chartState.withZoom(zoomOverride)
    assertThat(changedChartState.zoomX).isEqualTo(2.0)
    assertThat(changedChartState.zoomY).isEqualTo(4.0)
    assertThat(changedChartState.contentAreaWidth).isEqualTo(1000.0)
    assertThat(changedChartState.contentAreaHeight).isEqualTo(600.0)

    val tileIndex = TileIndex(3, 4)
    val tileSize = Size(200.0, 300.0)
    val tileCalculator = changedChartState.tileCalculator(tileIndex, tileSize)
    assertThat(tileCalculator.tileIndex).isEqualTo(tileIndex)
    assertThat(tileCalculator.tileSize).isEqualTo(tileSize)

    val timeRange = TimeRange(10_000.0, 15_000.0)
    assertThat(tileCalculator.tile2timeX(0.0, timeRange)).isEqualTo(timeRange.start + ((timeRange.span / zoomOverride.scaleX) / (changedChartState.contentAreaWidth / tileSize.width)) * tileIndex.x)
    assertThat(tileCalculator.tile2timeX(tileSize.width, timeRange)).isEqualTo(timeRange.start + ((timeRange.span / zoomOverride.scaleX) / (changedChartState.contentAreaWidth / tileSize.width)) * (tileIndex.x + 1))

    assertThat(tileCalculator.tile2timeY(0.0, timeRange)).isEqualTo(timeRange.start + ((timeRange.span / zoomOverride.scaleY) / (changedChartState.contentAreaHeight / tileSize.height)) * tileIndex.y)
    assertThat(tileCalculator.tile2timeY(tileSize.height, timeRange)).isEqualTo(timeRange.start + ((timeRange.span / zoomOverride.scaleY) / (changedChartState.contentAreaHeight / tileSize.height)) * (tileIndex.y + 1))
  }
}

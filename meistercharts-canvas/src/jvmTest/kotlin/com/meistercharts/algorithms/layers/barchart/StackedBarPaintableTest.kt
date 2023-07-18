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
package com.meistercharts.algorithms.layers.barchart

import assertk.*
import assertk.assertions.*
import assertk.assertions.support.*
import com.meistercharts.calc.ChartCalculator
import com.meistercharts.range.ValueRange
import com.meistercharts.axis.AxisOrientationX
import com.meistercharts.axis.AxisOrientationY
import com.meistercharts.state.DefaultChartState
import com.meistercharts.model.Size
import it.neckar.open.provider.DoublesProvider
import it.neckar.open.kotlin.lang.abs
import it.neckar.open.kotlin.lang.isCloseTo
import org.junit.jupiter.api.Test

class StackedBarPaintableTest {
  @Test
  fun testOneValue() {
    val data = StackedBarPaintable.Data(valuesProvider = DoublesProvider.forDoubles(5.0), ValueRange.linear(-10.0, 10.0))

    val paintable = StackedBarPaintable(data, 10.0, 500.0) {
      showRemainderAsSegment = true
      segmentsGap = 2.0
    }

    val chartState = DefaultChartState()
    chartState.contentAreaSize = Size(800.0, 1000.0)

    run {
      val layout = paintable.layout

      chartState.axisOrientationX = AxisOrientationX.OriginAtLeft
      chartState.axisOrientationY = AxisOrientationY.OriginAtBottom

      layout.calculateLayout(ChartCalculator(chartState))

      assertThat(layout.visibleSegments).isEqualTo(3)
      assertThat(layout.segmentsSumNegative).isEqualTo(0.0)
      assertThat(layout.segmentsSumPositive).isEqualTo(5.0)
      assertThat(layout.hasPositiveAndNegativeSegments).isTrue()
      assertThat(layout.hasPositiveSegments).isTrue()
      assertThat(layout.hasNegativeSegments).isFalse()

      assertThat(layout.segmentGap).isCloseTo(0.002, 0.00001) //3 gaps, 2px each

      //Remainder ends must be on 0.0/1.0
      assertThat(layout.remainderEndNegative).isCloseTo(0.0, 0.000001)
      assertThat(layout.remainderEndPositive).isCloseTo(1.0, 0.000001)

      //The segments start in the middle
      assertThat(layout.segmentStart[0]).isCloseTo(0.501, 0.000001)
      assertThat(layout.segmentEnd[0]).isCloseTo(0.7495, 0.000001)

      //Check gaps size
      assertThat(layout.segmentEnd[0]).hasDistance(layout.remainderStartPositive, 0.002, 0.0000000001)
      assertThat(layout.segmentStart[0]).hasDistance(layout.remainderStartNegative, 0.002, 0.0000000001)

      assertThat(layout.remainderStartPositive).isCloseTo(0.7515, 0.000001)
      assertThat(layout.remainderStartNegative).isCloseTo(0.499, 0.000001)

      //Check gaps size
      assertThat(layout.segmentEnd[0]).hasDistance(layout.remainderStartPositive, 0.002, 0.0000000001)
      assertThat(layout.segmentStart[0]).hasDistance(layout.remainderStartNegative, 0.002, 0.0000000001)
    }
  }

  @Test
  fun testVerySimple() {
    val data = StackedBarPaintable.Data(valuesProvider = DoublesProvider.forDoubles(4.0, -3.0), ValueRange.linear(-10.0, 10.0))

    val paintable = StackedBarPaintable(data, 10.0, 500.0) {
      showRemainderAsSegment = true
      segmentsGap = 2.0
    }

    val chartState = DefaultChartState()
    chartState.contentAreaSize = Size(800.0, 1000.0)

    run {
      val layout = paintable.layout

      chartState.axisOrientationX = AxisOrientationX.OriginAtLeft
      chartState.axisOrientationY = AxisOrientationY.OriginAtBottom

      layout.calculateLayout(ChartCalculator(chartState))

      assertThat(layout.hasPositiveAndNegativeSegments).isTrue()

      assertThat(layout.visibleSegments).isEqualTo(4)
      assertThat(layout.segmentsSumNegative).isEqualTo(-3.0)
      assertThat(layout.segmentsSumPositive).isEqualTo(4.0)

      assertThat(layout.segmentGap).isCloseTo(0.002, 0.00001) //3 gaps, 2px each

      //Remainder ends must be on 0.0/1.0
      assertThat(layout.remainderEndNegative).isCloseTo(0.0, 0.000001)
      assertThat(layout.remainderEndPositive).isCloseTo(1.0, 0.000001)

      //The segments start in the middle
      assertThat(layout.segmentStart[0]).isCloseTo(0.501, 0.000001)
      assertThat(layout.segmentStart[1]).isCloseTo(0.499, 0.000001)

      assertThat(layout.segmentEnd[0]).isCloseTo(0.6998, 0.000001)
      assertThat(layout.segmentEnd[1]).isCloseTo(0.3499, 0.000001)

      assertThat(layout.remainderStartPositive).isCloseTo(0.7018, 0.000001)
      assertThat(layout.remainderStartNegative).isCloseTo(0.3479, 0.000001)

      //Check gaps size
      assertThat(layout.segmentStart[0]).hasDistance(layout.segmentStart[1], 0.002, 0.0000000001)
      assertThat(layout.segmentEnd[0]).hasDistance(layout.remainderStartPositive, 0.002, 0.0000000001)
      assertThat(layout.segmentEnd[1]).hasDistance(layout.remainderStartNegative, 0.002, 0.0000000001)
    }
  }


  @Test
  fun testBug() {
    val data = StackedBarPaintable.Data(valuesProvider = DoublesProvider.forDoubles(10.0, -5.0, -3.0, 0.0), ValueRange.linear(-10.0, 14.0))

    val paintable = StackedBarPaintable(data, 10.0, 50.0) {
      showRemainderAsSegment = true
      segmentsGap = 2.0
    }

    val chartState = DefaultChartState()
    chartState.contentAreaSize = Size(800.0, 1000.0)


    run {
      chartState.axisOrientationX = AxisOrientationX.OriginAtLeft
      chartState.axisOrientationY = AxisOrientationY.OriginAtBottom

      val layout = paintable.layout
      layout.calculateLayout(ChartCalculator(chartState))

      assertThat(layout.hasPositiveAndNegativeSegments).isTrue()

      assertThat(layout.visibleSegments).isEqualTo(5)
      assertThat(layout.segmentsSumNegative).isEqualTo(-8.0)
      assertThat(layout.segmentsSumPositive).isEqualTo(10.0)

      assertThat(layout.segmentGap).isCloseTo(0.002, 0.00001)

      //Check gaps size
      assertThat(layout.segmentStart[0]).hasDistance(layout.segmentStart[1], 0.002, 0.0000000001)
      assertThat(layout.segmentEnd[0]).hasDistance(layout.remainderStartPositive, 0.002, 0.0000000001)

      assertThat(layout.segmentEnd[1]).hasDistance(layout.segmentStart[2], 0.002, 0.0000000001)
      assertThat(layout.segmentEnd[2]).hasDistance(layout.remainderStartNegative, 0.002, 0.0000000001)


      assertThat(layout.segmentStart[0]).isCloseTo(0.4176666666, 0.001)
      assertThat(layout.segmentEnd[0]).isCloseTo(0.83219, 0.001)

      assertThat(layout.segmentStart[1]).isCloseTo(0.4156, 0.001)
      assertThat(layout.segmentEnd[1]).isCloseTo(0.209, 0.001)


      assertThat(layout.remainderEndNegative).isCloseTo(0.0, 0.001)
      assertThat(layout.remainderEndPositive).isCloseTo(1.0, 0.001)

      assertThat(layout.remainderStartPositive).isCloseTo(0.83419, 0.001)
      assertThat(layout.remainderStartNegative).isCloseTo(0.08233, 0.001)
    }
  }

  @Test
  fun testBug2() {
    val data = StackedBarPaintable.Data(valuesProvider = DoublesProvider.forDoubles(-10.0, 7.0, 7.0, 0.0), ValueRange.linear(-10.0, 14.0))

    val paintable = StackedBarPaintable(data, 10.0, 50.0) {
      showRemainderAsSegment = true
      segmentsGap = 2.0
    }

    val chartState = DefaultChartState()
    chartState.contentAreaSize = Size(800.0, 1000.0)


    run {
      chartState.axisOrientationX = AxisOrientationX.OriginAtLeft
      chartState.axisOrientationY = AxisOrientationY.OriginAtBottom

      val layout = paintable.layout
      layout.calculateLayout(ChartCalculator(chartState))

      assertThat(layout.hasPositiveAndNegativeSegments).isTrue()

      assertThat(layout.visibleSegments).isEqualTo(3)
      assertThat(layout.segmentsSumNegative).isEqualTo(-10.0)
      assertThat(layout.segmentsSumPositive).isEqualTo(14.0)

      assertThat(layout.segmentGap).isCloseTo(0.002, 0.00001)

      //Check gaps size
      assertThat(layout.segmentStart[0]).hasDistance(layout.segmentStart[1], 0.002, 0.0000000001)
      assertThat(layout.segmentEnd[1]).hasDistance(layout.segmentStart[2], 0.002, 0.0000000001)


      //Remainer is not visible
      assertThat(layout.remainderEndNegative).isEqualTo(Double.NaN)
      assertThat(layout.remainderEndPositive).isEqualTo(Double.NaN)
      assertThat(layout.remainderStartPositive).isEqualTo(Double.NaN)
      assertThat(layout.remainderStartNegative).isEqualTo(Double.NaN)


      assertThat(layout.segmentStart[0]).isCloseTo(0.4156666, 0.00001)
      assertThat(layout.segmentEnd[0]).isCloseTo(0.0, 0.00001)

      assertThat(layout.segmentStart[1]).isCloseTo(0.4176666, 0.00001)
      assertThat(layout.segmentEnd[1]).isCloseTo(0.70783, 0.00001)
    }
  }

  @Test
  fun test1Neg1Pos() {
    val data = StackedBarPaintable.Data(valuesProvider = DoublesProvider.forDoubles(-2.0, 2.0), ValueRange.linear(-5.0, 5.0))
    val paintable = StackedBarPaintable(data, 10.0, 50.0) {
      segmentsGap = 2.0
      showRemainderAsSegment = false
    }

    val chartState = DefaultChartState()
    chartState.contentAreaSize = Size(800.0, 600.0)

    run {
      chartState.axisOrientationX = AxisOrientationX.OriginAtLeft
      chartState.axisOrientationY = AxisOrientationY.OriginAtTop

      paintable.layout.calculateLayout(ChartCalculator(chartState))

      assertThat(paintable.layout.hasPositiveAndNegativeSegments).isTrue()

      assertThat(paintable.layout.visibleSegments).isEqualTo(2)
      assertThat(paintable.layout.segmentsSumNegative).isEqualTo(-2.0)
      assertThat(paintable.layout.segmentsSumPositive).isEqualTo(2.0)

      assertThat(paintable.layout.segmentGap).isCloseTo(0.003333, 0.00001)

      assertThat(paintable.layout.segmentStart[0]).isCloseTo(0.49833, 0.001)
      assertThat(paintable.layout.segmentEnd[0]).isCloseTo(0.30000, 0.001)

      assertThat(paintable.layout.segmentStart[1]).isCloseTo(0.5016, 0.001)
      assertThat(paintable.layout.segmentEnd[1]).isCloseTo(0.701, 0.001)
    }

    run {
      chartState.axisOrientationX = AxisOrientationX.OriginAtLeft
      chartState.axisOrientationY = AxisOrientationY.OriginAtBottom

      paintable.layout.calculateLayout(ChartCalculator(chartState))

      assertThat(paintable.layout.visibleSegments).isEqualTo(2)
      assertThat(paintable.layout.segmentsSumNegative).isEqualTo(-2.0)
      assertThat(paintable.layout.segmentsSumPositive).isEqualTo(2.0)

      assertThat(paintable.layout.segmentGap).isCloseTo(0.003333, 0.00001)

      assertThat(paintable.layout.segmentStart[0]).isCloseTo(0.49833, 0.001)
      assertThat(paintable.layout.segmentEnd[0]).isCloseTo(0.30000, 0.001)

      assertThat(paintable.layout.segmentStart[1]).isCloseTo(0.5016, 0.001)
      assertThat(paintable.layout.segmentEnd[1]).isCloseTo(0.701, 0.001)
    }
  }

  @Test
  fun testSimpleRemainder() {
    val data = StackedBarPaintable.Data(valuesProvider = DoublesProvider.forDoubles(5.0), ValueRange.linear(0.0, 10.0))
    val paintable = StackedBarPaintable(data, 10.0, 50.0) {
      segmentsGap = 2.0
      showRemainderAsSegment = false
    }

    val chartState = DefaultChartState()

    chartState.axisOrientationX = AxisOrientationX.OriginAtLeft
    chartState.axisOrientationY = AxisOrientationY.OriginAtTop
    chartState.contentAreaSize = Size(800.0, 600.0)

    run {
      paintable.style.showRemainderAsSegment = false

      paintable.layout.calculateLayout(ChartCalculator(chartState))

      assertThat(paintable.layout.hasPositiveAndNegativeSegments).isFalse()

      assertThat(paintable.layout.visibleSegments).isEqualTo(1)
      assertThat(paintable.layout.segmentsSumPositive).isEqualTo(5.0)
      assertThat(paintable.layout.segmentsSumNegative).isEqualTo(0.0)

      assertThat(paintable.layout.segmentGap).isCloseTo(0.0033333, 0.0001)
      assertThat(paintable.layout.segmentStart[0]).isEqualTo(0.0)
      assertThat(paintable.layout.segmentEnd[0]).isEqualTo(0.5)

      assertThat(paintable.layout.remainderStartPositive).isEqualTo(Double.NaN)
      assertThat(paintable.layout.remainderStartNegative).isEqualTo(Double.NaN)

      assertThat(paintable.layout.remainderNetSizePositive).isEqualTo(Double.NaN)
    }

    run {
      paintable.style.showRemainderAsSegment = true

      paintable.layout.calculateLayout(ChartCalculator(chartState))

      assertThat(paintable.layout.visibleSegments).isEqualTo(2)
      assertThat(paintable.layout.segmentsSumPositive).isEqualTo(5.0)
      assertThat(paintable.layout.segmentsSumNegative).isEqualTo(0.0)

      assertThat(paintable.layout.segmentGap).isCloseTo(0.0033, 0.0001)
      assertThat(paintable.layout.segmentStart[0]).isEqualTo(0.0)
      //The segment is smaller to make space for the gap
      assertThat(paintable.layout.segmentEnd[0]).isCloseTo(0.498333, 0.0001)

      //The remainder
      assertThat(paintable.layout.remainderStartPositive).isCloseTo(0.5016666, 0.0001)
      assertThat(paintable.layout.remainderNetSizePositive).isCloseTo(0.498333, 0.0001)
      assertThat(paintable.layout.remainderEndPositive).isEqualTo(1.0)

      assertThat(paintable.layout.remainderStartNegative).isEqualTo(Double.NaN)
      assertThat(paintable.layout.remainderNetSizeNegative).isEqualTo(Double.NaN)
    }
  }

  @Test
  fun test2Pos() {
    val data = StackedBarPaintable.Data(valuesProvider = DoublesProvider.forDoubles(2.0, 2.0), ValueRange.linear(0.0, 10.0))
    val paintable = StackedBarPaintable(data, 10.0, 50.0) {
      segmentsGap = 2.0
      showRemainderAsSegment = false
    }

    val chartState = DefaultChartState()

    chartState.axisOrientationX = AxisOrientationX.OriginAtLeft
    chartState.axisOrientationY = AxisOrientationY.OriginAtTop
    chartState.contentAreaSize = Size(800.0, 600.0)

    run {
      paintable.style.showRemainderAsSegment = false

      paintable.layout.calculateLayout(ChartCalculator(chartState))

      assertThat(paintable.layout.hasPositiveAndNegativeSegments).isFalse()

      assertThat(paintable.layout.visibleSegments).isEqualTo(2)
      assertThat(paintable.layout.segmentsSumNegative).isEqualTo(0.0)
      assertThat(paintable.layout.segmentsSumPositive).isEqualTo(4.0)

      val gap = 0.003333
      assertThat(paintable.layout.segmentGap).isCloseTo(gap, 0.00001)

      assertThat(paintable.layout.segmentStart[0]).isCloseTo(0.0, 0.001)
      assertThat(paintable.layout.segmentEnd[0]).all {
        isLessThan(0.2) //the segment size has been reduced
        isCloseTo(0.199333, 0.001)
      }

      assertThat(paintable.layout.segmentStart[1]).isCloseTo(paintable.layout.segmentEnd[0] + gap, 0.001)
      assertThat(paintable.layout.segmentEnd[1]).isCloseTo(0.402, 0.001)

      assertThat(paintable.layout.remainderStartPositive).isEqualTo(Double.NaN)
      assertThat(paintable.layout.remainderNetSizePositive).isEqualTo(Double.NaN)
    }

    run {
      //With remainder
      paintable.style.showRemainderAsSegment = true

      paintable.layout.calculateLayout(ChartCalculator(chartState))

      assertThat(paintable.layout.hasPositiveAndNegativeSegments).isFalse()

      assertThat(paintable.layout.visibleSegments).isEqualTo(3)
      assertThat(paintable.layout.segmentsSumNegative).isEqualTo(0.0)
      assertThat(paintable.layout.segmentsSumPositive).isEqualTo(4.0)

      val gap = 0.003333
      assertThat(paintable.layout.segmentGap).isCloseTo(gap, 0.00001)

      assertThat(paintable.layout.segmentStart[0]).isCloseTo(0.0, 0.001)
      assertThat(paintable.layout.segmentEnd[0]).all {
        isLessThan(0.2) //the segment size has been reduced
        isCloseTo(0.1983, 0.001)
      }

      assertThat(paintable.layout.segmentStart[1]).isCloseTo(paintable.layout.segmentEnd[0] + gap, 0.001)
      assertThat(paintable.layout.segmentEnd[1]).isCloseTo(0.4, 0.001)

      assertThat(paintable.layout.remainderStartPositive).isCloseTo(0.404, 0.0001)
      assertThat(paintable.layout.remainderEndPositive).isEqualTo(1.0)
    }
  }

  @Test
  fun test2Neg() {
    val data = StackedBarPaintable.Data(valuesProvider = DoublesProvider.forDoubles(-2.0, -2.0), ValueRange.linear(-10.0, 0.0))
    val paintable = StackedBarPaintable(data, 10.0, 50.0) {
      segmentsGap = 2.0
      showRemainderAsSegment = false
    }

    val chartState = DefaultChartState()
    chartState.axisOrientationX = AxisOrientationX.OriginAtLeft
    chartState.axisOrientationY = AxisOrientationY.OriginAtTop
    chartState.contentAreaSize = Size(800.0, 600.0)

    paintable.layout.calculateLayout(ChartCalculator(chartState))

    assertThat(paintable.layout.hasPositiveAndNegativeSegments).isFalse()

    assertThat(paintable.layout.visibleSegments).isEqualTo(2)
    assertThat(paintable.layout.segmentsSumNegative).isEqualTo(-4.0)
    assertThat(paintable.layout.segmentsSumPositive).isEqualTo(0.0)

    val gap = 0.003333
    assertThat(paintable.layout.segmentGap).isCloseTo(gap, 0.00001)

    assertThat(paintable.layout.segmentStart[0]).isCloseTo(1.0, 0.001)
    assertThat(paintable.layout.segmentEnd[0]).isCloseTo(0.80166, 0.001)

    assertThat(paintable.layout.segmentStart[1]).isCloseTo(paintable.layout.segmentEnd[0] - gap, 0.001)
    assertThat(paintable.layout.segmentEnd[1]).isCloseTo(0.598, 0.001)
  }
}


fun Assert<Double>.hasDistance(otherValue: Double, expectedDistance: Double, delta: Double) {
  given { actual ->
    val distance = (actual - otherValue).abs()

    if (distance.isCloseTo(expectedDistance, delta)) {
      return
    }

    expected("Distance to be $expectedDistance was: ${show(distance)} for ${show(actual)} - ${show(otherValue)}")
  }
}

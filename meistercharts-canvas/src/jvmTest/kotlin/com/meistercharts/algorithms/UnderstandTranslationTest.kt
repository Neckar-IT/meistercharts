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
import it.neckar.geometry.AxisOrientationY
import com.meistercharts.state.DefaultChartState
import com.meistercharts.calc.ChartCalculator
import it.neckar.geometry.Size
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 * A unit test that shows how the translation works together with zooming
 *
 */
class UnderstandTranslationTest {
  private lateinit var chartState: DefaultChartState
  private lateinit var calculator: ChartCalculator

  @BeforeEach
  fun setUp() {
    chartState = DefaultChartState()
    chartState.contentAreaSize = Size(800.0, 600.0)
    calculator = ChartCalculator(chartState)
  }

  @Test
  internal fun testBasics() {
    assertThat(chartState.contentAreaWidth).isEqualTo(800.0)
    assertThat(chartState.contentAreaHeight).isEqualTo(600.0)
  }

  @Test
  internal fun testAxisOrientation() {
    //domain 0 is at the bottom of the content area
    chartState.axisOrientationY = AxisOrientationY.OriginAtBottom
    assertThat(calculator.domainRelative2windowY(0.0)).all {
      isEqualTo(chartState.contentAreaHeight)
      isEqualTo(600.0)
    }

    //domain 0 is now at the top of the content area
    chartState.axisOrientationY = AxisOrientationY.OriginAtTop
    assertThat(calculator.domainRelative2windowY(0.0)).isEqualTo(0.0)
  }

  /**
   * Shows the behavior translation with a zoom of 1
   */
  @Test
  internal fun testTranslationZoom1() {
    //Calculate everything on the x axis to avoid confusion with top-->down / bottom-->up

    //--> zoom1_translate0.png

    assertThat(calculator.domainRelative2windowX(0.0)).isEqualTo(0.0)
    assertThat(calculator.domainRelative2windowX(1.0)).all {
      isEqualTo(chartState.contentAreaWidth)
      isEqualTo(800.0)
    }


    assertThat(calculator.chartState.axisOrientationY).isEqualTo(AxisOrientationY.OriginAtBottom)
    assertThat(calculator.domainRelative2windowY(0.0)).all {
      isEqualTo(chartState.contentAreaHeight)
      isEqualTo(600.0)
    }
    assertThat(calculator.domainRelative2windowY(1.0)).isEqualTo(0.0)

    //move to the left. The left side of the diagram is no longer visible
    chartState.windowTranslationX = -100.0
    //--> zoom1_translate-100.png

    //Domain value 0.0 is no longer visible but on the left side of the canvas
    assertThat(calculator.domainRelative2windowX(0.0)).isEqualTo(-100.0)
    //the right side is 100 pixels away from the right side of the canvas
    assertThat(calculator.domainRelative2windowX(1.0)).isEqualTo(800.0 - 100.0)
    //Since domain value 0 is moved out of the canvas (to the left)
    //we see a "small" domain value at the left border
    assertThat(calculator.window2domainRelativeX(0.0)).all {
      isEqualTo(0.125)
      isEqualTo(1.0 / 800.0 * 100.0)
    }
    //on the right side of the canvas we see a value a little bit larger than 1.0
    assertThat(calculator.window2domainRelativeX(800.0)).isEqualTo(1.125)


    //Move to the right
    chartState.windowTranslationX = 200.0
    //--> zoom1_translate200.png

    //Domain value 0.0 is 100 pixels away from the left side2
    assertThat(calculator.domainRelative2windowX(0.0)).isEqualTo(200.0)
    //the right side is no longer visible in the canvas but "hidden" on the right
    assertThat(calculator.domainRelative2windowX(1.0)).isEqualTo(800.0 + 200.0)

    //The domain value 0 is moved to the right
    //we see a smaller than 0 domain value at the left border
    assertThat(calculator.window2domainRelativeX(0.0)).isEqualTo(-0.25)
    //on the right side of the canvas we see a value a little bit smaller than 1.0
    assertThat(calculator.window2domainRelativeX(800.0)).isEqualTo(0.75)
  }

  /**
   * Shows how the behavior of the translation with a zoom of 2
   */
  @Test
  internal fun testTranslationZoom2() {
    //Calculate everything on the x axis to avoid confusion with top-->down / bottom-->up
    chartState.zoomX = 2.0

    //--> zoom2_translate0.png


    assertThat(calculator.domainRelative2windowX(0.0)).isEqualTo(0.0)
    //The center of x is now painted on the right side of the canvas
    assertThat(calculator.domainRelative2windowX(0.5)).isEqualTo(800.0)
    //Domain value 1.0 is no longer visible because the zoom moves the value out of the canvas
    assertThat(calculator.domainRelative2windowX(1.0)).isEqualTo(800.0 * 2)


    //move to the left. The left side of the diagram is no longer visible
    chartState.windowTranslationX = -100.0
    //--> zoom2_translate-100.png

    //Domain value 0.0 is no longer visible but on the left side of the canvas
    assertThat(calculator.domainRelative2windowX(0.0)).isEqualTo(-100.0)
    //The domain value 0.5 is moved slightly to the left of the right side by the translation
    assertThat(calculator.domainRelative2windowX(0.5)).isEqualTo(800.0 - 100.0)
    //Domain value 1.0 is not visible because the translation is not large enough. But it is also moved by the translated value
    assertThat(calculator.domainRelative2windowX(1.0)).isEqualTo(800.0 * 2 - 100.0)

    //Since domain value 0 is moved out of the canvas (to the left)
    //we see a "small" domain value at the left border
    assertThat(calculator.window2domainRelativeX(0.0)).isEqualTo(0.0625)
    //on the right side of the canvas we see a value a little bit larger than 0.5
    assertThat(calculator.window2domainRelativeX(800.0)).isEqualTo(0.5625)


    //Move to the right
    chartState.windowTranslationX = 200.0
    //--> zoom2_translate200.png

    //Domain value 0.0 is 100 pixels away from the left side2
    assertThat(calculator.domainRelative2windowX(0.0)).isEqualTo(200.0)
    //The domain value 0.5 is moved to the right and no longer visible
    assertThat(calculator.domainRelative2windowX(0.5)).isEqualTo(800.0 + 200.0)
    //Domain value 1.0 is moved even further to the right
    assertThat(calculator.domainRelative2windowX(1.0)).isEqualTo(800.0 * 2 + 200.0)


    //The domain value 0 is moved to the right
    //we see a smaller than 0 domain value at the left border
    assertThat(calculator.window2domainRelativeX(0.0)).isEqualTo(-0.125)
    //on the right side of the canvas we see a value a little bit smaller than 0.5
    assertThat(calculator.window2domainRelativeX(800.0)).isEqualTo(0.375)
  }
}


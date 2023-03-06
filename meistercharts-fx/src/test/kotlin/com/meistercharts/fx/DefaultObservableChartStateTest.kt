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
package com.meistercharts.fx

import assertk.*
import assertk.assertions.*
import com.meistercharts.algorithms.axis.AxisOrientationX
import com.meistercharts.algorithms.axis.AxisOrientationY
import com.meistercharts.algorithms.axis.AxisSelection
import com.meistercharts.algorithms.impl.DefaultChartState
import com.meistercharts.model.Size
import org.junit.jupiter.api.Test

/**
 */
class DefaultObservableChartStateTest {
  @Test
  fun testProperties() {
    val viewState = DefaultChartState()
    assertThat(viewState.axisOrientationX).isSameAs(viewState.axisOrientationXProperty.value)
    assertThat(viewState.axisOrientationY).isSameAs(viewState.axisOrientationYProperty.value)
  }

  @Test
  fun testBinding() {
    val viewState0 = DefaultChartState()
      .apply {
        contentAreaSize = Size(10.0, 20.0)
      }

    val viewState1 = DefaultChartState()
      .apply {
        contentAreaSize = Size(90.0, 80.0)

        zoomX = 12.0
        zoomY = 11.0
        windowTranslationX = 13.0
        windowTranslationY = 17.0
      }

    viewState0.bindBidirectional(viewState1, AxisSelection.Both)

    //The view size is *not* connected
    assertThat(viewState0.contentAreaSize).isNotEqualTo(viewState1.contentAreaSize)
    assertThat(viewState0.contentAreaSize.width).isEqualTo(10.0)

    //The other properties are connected
    assertThat(viewState0.axisOrientationX).all {
      isSameAs(viewState1.axisOrientationX)
      isSameAs(AxisOrientationX.OriginAtLeft)
    }
    assertThat(viewState0.axisOrientationY).all {
      isSameAs(viewState1.axisOrientationY)
      isSameAs(AxisOrientationY.OriginAtBottom)
    }
    assertThat(viewState0.windowTranslationX).all {
      isEqualTo(viewState1.windowTranslationX)
      isEqualTo(13.0)
    }
    assertThat(viewState0.windowTranslationY).all {
      isEqualTo(viewState1.windowTranslationY)
      isEqualTo(17.0)
    }

    assertThat(viewState0.zoomX).all {
      isEqualTo(viewState1.zoomX)
      isEqualTo(12.0)
    }
    assertThat(viewState0.zoomY).all {
      isEqualTo(viewState1.zoomY)
      isEqualTo(11.0)
    }


    //Modify the values - the other state should reflect these changes

    viewState0.apply {
      axisOrientationX = AxisOrientationX.OriginAtRight
      axisOrientationY = AxisOrientationY.OriginAtTop

      zoomX = 22.0
      zoomY = 21.0
      windowTranslationX = 23.0
      windowTranslationY = 27.0
    }

    //Verify the values have been updated in both view states
    assertThat(viewState0.axisOrientationX).all {
      isSameAs(viewState1.axisOrientationX)
      isSameAs(AxisOrientationX.OriginAtRight)
    }
    assertThat(viewState0.axisOrientationY).all {
      isSameAs(viewState1.axisOrientationY)
      isSameAs(AxisOrientationY.OriginAtTop)
    }
    assertThat(viewState0.windowTranslationX).all {
      isEqualTo(viewState1.windowTranslationX)
      isEqualTo(23.0)
    }
    assertThat(viewState0.windowTranslationY).all {
      isEqualTo(viewState1.windowTranslationY)
      isEqualTo(27.0)
    }
    assertThat(viewState0.zoomX).all {
      isEqualTo(viewState1.zoomX)
      isEqualTo(22.0)
    }
    assertThat(viewState0.zoomY).all {
      isEqualTo(viewState1.zoomY)
      isEqualTo(21.0)
    }
  }

  @Test
  fun testBindingX() {
    val viewState0 = DefaultChartState()
      .apply {
        contentAreaSize = Size(10.0, 20.0)
        axisOrientationX = AxisOrientationX.OriginAtLeft
        axisOrientationY = AxisOrientationY.OriginAtBottom

        zoomX = 1.0
        zoomY = 1.0
        windowTranslationX = 0.0
        windowTranslationY = 0.0
      }

    val viewState1 = DefaultChartState()
      .apply {
        contentAreaSize = Size(90.0, 80.0)
        axisOrientationX = AxisOrientationX.OriginAtRight
        axisOrientationY = AxisOrientationY.OriginAtTop

        zoomX = 12.0
        zoomY = 11.0
        windowTranslationX = 13.0
        windowTranslationY = 17.0
      }

    viewState0.bindBidirectional(viewState1, AxisSelection.X)

    //The view size is *not* connected
    assertThat(viewState0.contentAreaSize).isNotEqualTo(viewState1.contentAreaSize)
    assertThat(viewState0.contentAreaSize.width).isEqualTo(10.0)
    assertThat(viewState0.contentAreaSize.height).isEqualTo(20.0)

    //X-values should be equal
    assertThat(viewState0.axisOrientationX).isEqualTo(viewState1.axisOrientationX)
    assertThat(viewState0.zoomX).isEqualTo(viewState1.zoomX)
    assertThat(viewState0.windowTranslationX).isEqualTo(viewState1.windowTranslationX)

    //Y-values should not be equal
    assertThat(viewState0.axisOrientationY).isNotEqualTo(viewState1.axisOrientationX)
    assertThat(viewState0.zoomY).isNotEqualTo(viewState1.zoomX)
    assertThat(viewState0.windowTranslationY).isNotEqualTo(viewState1.windowTranslationX)


    //Modify the values - the other state should reflect these changes
    viewState0.apply {
      axisOrientationX = AxisOrientationX.OriginAtLeft
      axisOrientationY = AxisOrientationY.OriginAtBottom

      zoomX = 22.0
      zoomY = 21.0
      windowTranslationX = 123.0
      windowTranslationY = 321.0
    }

    viewState1.apply {
      axisOrientationY = AxisOrientationY.OriginAtTop
      zoomY = 1.0
      windowTranslationY = 0.0
    }

    //X-values should be equal
    assertThat(viewState0.axisOrientationX).isEqualTo(viewState1.axisOrientationX)
    assertThat(viewState0.zoomX).isEqualTo(viewState1.zoomX)
    assertThat(viewState0.windowTranslationX).isEqualTo(viewState1.windowTranslationX)

    //Y-values should not be equal
    assertThat(viewState0.axisOrientationY).isNotEqualTo(viewState1.axisOrientationX)
    assertThat(viewState0.zoomY).isNotEqualTo(viewState1.zoomX)
    assertThat(viewState0.windowTranslationY).isNotEqualTo(viewState1.windowTranslationX)
  }

  @Test
  fun testBindingY() {
    val viewState0 = DefaultChartState()
      .apply {
        contentAreaSize = Size(10.0, 20.0)
        axisOrientationX = AxisOrientationX.OriginAtLeft
        axisOrientationY = AxisOrientationY.OriginAtBottom

        zoomX = 1.0
        zoomY = 1.0
        windowTranslationX = 0.0
        windowTranslationY = 0.0
      }

    val viewState1 = DefaultChartState()
      .apply {
        contentAreaSize = Size(90.0, 80.0)
        axisOrientationX = AxisOrientationX.OriginAtRight
        axisOrientationY = AxisOrientationY.OriginAtTop

        zoomX = 12.0
        zoomY = 11.0
        windowTranslationX = 13.0
        windowTranslationY = 17.0
      }

    viewState0.bindBidirectional(viewState1, AxisSelection.Y)

    //The view size is *not* connected
    assertThat(viewState0.contentAreaSize).isNotEqualTo(viewState1.contentAreaSize)
    assertThat(viewState0.contentAreaSize.width).isEqualTo(10.0)
    assertThat(viewState0.contentAreaSize.height).isEqualTo(20.0)

    //Y-values should be equal
    assertThat(viewState0.axisOrientationY).isEqualTo(viewState1.axisOrientationY)
    assertThat(viewState0.zoomY).isEqualTo(viewState1.zoomY)
    assertThat(viewState0.windowTranslationY).isEqualTo(viewState1.windowTranslationY)

    //X-values should not be equal
    assertThat(viewState0.axisOrientationX).isNotEqualTo(viewState1.axisOrientationX)
    assertThat(viewState0.zoomX).isNotEqualTo(viewState1.zoomX)
    assertThat(viewState0.windowTranslationX).isNotEqualTo(viewState1.windowTranslationX)


    //Modify the values - the other state should reflect these changes
    viewState0.apply {
      axisOrientationX = AxisOrientationX.OriginAtLeft
      axisOrientationY = AxisOrientationY.OriginAtBottom

      zoomX = 22.0
      zoomY = 21.0
      windowTranslationX = 123.0
      windowTranslationY = 321.0
    }

    viewState1.apply {
      axisOrientationX = AxisOrientationX.OriginAtRight
      zoomX = 1.0
      windowTranslationX = 0.0
    }

    //Y-values should be equal
    assertThat(viewState0.axisOrientationY).isEqualTo(viewState1.axisOrientationY)
    assertThat(viewState0.zoomY).isEqualTo(viewState1.zoomY)
    assertThat(viewState0.windowTranslationY).isEqualTo(viewState1.windowTranslationY)

    //X-values should not be equal
    assertThat(viewState0.axisOrientationX).isNotEqualTo(viewState1.axisOrientationX)
    assertThat(viewState0.zoomX).isNotEqualTo(viewState1.zoomX)
    assertThat(viewState0.windowTranslationX).isNotEqualTo(viewState1.windowTranslationX)
  }
}

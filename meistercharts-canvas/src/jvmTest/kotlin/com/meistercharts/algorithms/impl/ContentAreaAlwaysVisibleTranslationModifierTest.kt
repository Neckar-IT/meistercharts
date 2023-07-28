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
import com.meistercharts.calc.ChartCalculator
import com.meistercharts.zoom.ZoomAndTranslationModifier
import it.neckar.geometry.Distance
import it.neckar.geometry.Size
import com.meistercharts.model.Zoom
import com.meistercharts.state.DefaultChartState
import com.meistercharts.zoom.ContentAreaAlwaysBarelyVisibleTranslationModifier
import com.meistercharts.zoom.ContentAreaAlwaysCompletelyVisibleTranslationModifier
import org.junit.jupiter.api.Test

/**
 */
class ContentAreaAlwaysVisibleTranslationModifierTest {
  @Test
  fun testCompletelyVisible() {
    val chartState = DefaultChartState()
    val calculator = ChartCalculator(chartState)
    chartState.contentAreaSize = Size(800.0, 600.0)
    chartState.windowSize = Size(800.0, 600.0)

    val modifier = ContentAreaAlwaysCompletelyVisibleTranslationModifier(delegate = ZoomAndTranslationModifier.none)

    modifier.modifyTranslation(Distance.none, calculator).let {
      assertThat(it.x).isEqualTo(0.0)
      assertThat(it.y).isEqualTo(0.0)
    }

    modifier.modifyTranslation(Distance.of(17.0, 34234.0), calculator).let {
      assertThat(it.x).isEqualTo(0.0)
      assertThat(it.y).isEqualTo(0.0)
    }

    modifier.modifyTranslation(Distance.of(-17.0, -34234.0), calculator).let {
      assertThat(it.x).isEqualTo(0.0)
      assertThat(it.y).isEqualTo(0.0)
    }
  }

  @Test
  fun testBasics() {
    val chartState = DefaultChartState()
    val calculator = ChartCalculator(chartState)
    chartState.contentAreaSize = Size(800.0, 600.0)

    val modifier = ContentAreaAlwaysBarelyVisibleTranslationModifier(ZoomAndTranslationModifier.none)

    val modifiedZoomFactors = modifier.modifyZoom(Zoom(100.0, 100.0), calculator)
    assertThat(modifiedZoomFactors.scaleX).isEqualTo(100.0)
    assertThat(modifiedZoomFactors.scaleY).isEqualTo(100.0)

    val modifiedPanning = modifier.modifyTranslation(Distance.none, calculator)
    assertThat(modifiedPanning.x).isEqualTo(0.0)
    assertThat(modifiedPanning.y).isEqualTo(0.0)
  }

  @Test
  fun testLimitWithZoomFactor1() {
    val chartState = DefaultChartState()
    chartState.contentAreaSize = Size(800.0, 600.0)
    val calculator = ChartCalculator(chartState)

    val modifier = ContentAreaAlwaysBarelyVisibleTranslationModifier(ZoomAndTranslationModifier.none)


    modifier.modifyTranslation(Distance.of(5000.0, 5000.0), calculator)
      .also {
        assertThat(it.x).isEqualTo(800.0)
        assertThat(it.y).isEqualTo(600.0)
      }

    modifier.modifyTranslation(Distance.of(-5000.0, -5000.0), calculator)
      .also {
        assertThat(it.x).isEqualTo(-800.0)
        assertThat(it.y).isEqualTo(-600.0)
      }
  }

  @Test
  fun testLimitWithZoomFactor2() {
    val chartState = DefaultChartState()
    val calculator = ChartCalculator(chartState)

    chartState.contentAreaSize = Size(800.0, 600.0)
    chartState.zoom = Zoom.of(2.0, 2.0)

    val modifier = ContentAreaAlwaysBarelyVisibleTranslationModifier(ZoomAndTranslationModifier.none)

    //Max panning to the bottom right
    modifier.modifyTranslation(Distance.of(5000.0, 5000.0), calculator)
      .also {
        assertThat(it.x).isEqualTo(800.0)
        assertThat(it.y).isEqualTo(600.0)
      }

    modifier.modifyTranslation(Distance.of(-5000.0, -5000.0), calculator)
      .also {
        assertThat(it.x).isEqualTo(-800.0 * 2)
        assertThat(it.y).isEqualTo(-600.0 * 2)
      }
  }
}

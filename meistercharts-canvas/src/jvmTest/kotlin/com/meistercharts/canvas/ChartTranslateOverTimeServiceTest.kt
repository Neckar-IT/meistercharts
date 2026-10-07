/*
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
import com.meistercharts.loop.RenderLoopSupport
import com.meistercharts.time.TimeRange
import it.neckar.geometry.Size
import it.neckar.open.time.monotonicTimeSource
import it.neckar.open.time.nowProvider
import it.neckar.open.time.resetMonotonicTimeSource
import it.neckar.open.time.resetNowProvider
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TestTimeSource
import it.neckar.open.dispose.Disposable

class ChartTranslateOverTimeServiceTest {
  @AfterEach
  fun tearDown() {
    resetNowProvider()
    resetMonotonicTimeSource()
  }

  @Test
  fun testFollowsWallClockJump() {
    val wallClock = MutableNowProvider(1_700_000_000_000.0)
    val timeSource = TestTimeSource()
    nowProvider = wallClock
    monotonicTimeSource = timeSource

    val canvas = MockCanvas()
    val chartSupport = ChartSupport(canvas)
    BindContentAreaSize2ContentViewport().bindResize(chartSupport)
    canvas.size = Size.of(800.0, 600.0)

    chartSupport.translateOverTime.also {
      it.contentAreaTimeRangeX = TimeRange.fromStartAndDuration(1_700_000_000_000.0, 60_000.0)
      it.roundingStrategy = RoundingStrategy.exact
      it.animated = true
    }

    val renderLoop = RenderLoopSupport()
    val registration: Disposable = renderLoop.onRender(chartSupport)

    renderLoop.nextLoop(0.0)
    val translationBefore: Double = chartSupport.currentChartState.windowTranslationX

    //The wall clock is set back by one hour, the monotonic time source moves on by one second
    wallClock.now -= 60 * 60_000.0
    timeSource += 1000.milliseconds
    renderLoop.nextLoop(1000.0)

    val contentAreaWidth: Double = chartSupport.currentChartState.contentAreaWidth
    assertThat(contentAreaWidth).isEqualTo(800.0)
    //One hour equals 60 content area widths of one minute each
    assertThat(chartSupport.currentChartState.windowTranslationX - translationBefore).isCloseTo(60 * contentAreaWidth, 0.000_001)

    registration.dispose()
  }
}

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
package com.meistercharts.canvas.animation

import com.meistercharts.canvas.MutableNowProvider
import assertk.*
import assertk.assertions.*
import com.meistercharts.animation.Easing
import com.meistercharts.loop.RenderLoopSupport
import it.neckar.open.time.monotonicMillis
import it.neckar.open.time.monotonicTimeSource
import it.neckar.open.time.nowProvider
import it.neckar.open.time.resetMonotonicTimeSource
import it.neckar.open.time.resetNowProvider
import it.neckar.open.unit.si.ms
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit
import kotlin.time.TestTimeSource
import it.neckar.open.kotlin.lang.requireNotNull
import it.neckar.open.dispose.Disposable

class TweenTest {
  private val timeSource: TestTimeSource = TestTimeSource()

  @ms
  private val start: Double = 1_000.0

  @AfterEach
  fun tearDown() {
    resetNowProvider()
    resetMonotonicTimeSource()
  }

  @Test
  fun testElapsed() {
    val tween = Tween(start, 5.seconds, Easing.linear)

    assertThat(tween.elapsedTime(start + 1.0)).isEqualTo(1.0)
    assertThat(tween.elapsedTime(start + 100.0)).isEqualTo(100.0)
    assertThat(tween.elapsedTime(start + 99999.0)).isEqualTo(99999.0)
  }

  @Test
  fun testTweening() {
    val tween = Tween(start, 5.seconds, Easing.linear).also {
      assertThat(it.duration).isEqualTo(5.seconds)
    }

    assertThat(tween.start).isEqualTo(start)
    assertThat(tween.end).isEqualTo(start + 5000.0)

    assertThat(tween.elapsedRatioForTime(start)).isEqualTo(0.0)
    assertThat(tween.elapsedRatioForTime(start + 1.0)).isEqualTo(0.0002)

    assertThat(tween.elapsedRatioForTime(start + 2.0)).isEqualTo(0.0004)

    assertThat(tween.elapsedRatioForTime(start + 20.0)).isEqualTo(0.004)
    assertThat(tween.elapsedRatioForTime(start + 500.0)).isEqualTo(0.1)
    assertThat(tween.elapsedRatioForTime(start + 1000.0)).isEqualTo(0.2)
    assertThat(tween.elapsedRatioForTime(start + 4999.0)).isEqualTo(1.0 - 0.0002)

    assertThat(tween.elapsedRatioForTime(start + 5000.0)).isEqualTo(1.0)
    assertThat(tween.elapsedRatioForTime(start + 99999.0)).isEqualTo(1.0)
    assertThat(tween.elapsedRatioForTime(start - 99999.0)).isEqualTo(0.0)
  }

  @Test
  fun testFinished() {
    val tween = Tween(start, 5.seconds, Easing.linear)

    assertThat(tween.isFinished(start + 5000.0)).isFalse()
    assertThat(tween.isFinished(start + 5001.0)).isTrue()
    assertThat(Tween(start, 5.seconds, Easing.linear, AnimationRepeatType.Repeat).isFinished(start + 3_600_000.0)).isFalse()
  }

  @Test
  fun testRepeat() {
    Tween(start, 5.seconds, Easing.linear).let { tween ->
      assertThat(tween.repeatType).isEqualTo(AnimationRepeatType.Once)

      assertThat(tween.elapsedRatioForTime(start + 5000.0)).isEqualTo(1.0)
      //Does *not* repeat
      assertThat(tween.elapsedRatioForTime(start + 1.0)).isEqualTo(0.0002)
      assertThat(tween.elapsedRatioForTime(start + 5001.0)).isEqualTo(1.0)
    }

    Tween(start, 5.seconds, Easing.linear, AnimationRepeatType.Repeat).let { tween ->
      assertThat(tween.elapsedRatioForTime(start + 5001.0)).isEqualTo(0.0002)
    }
  }

  @Test
  fun testAutoReverse() {
    val nonReversingTween = Tween(start, 5.seconds, Easing.linear, repeatType = AnimationRepeatType.Repeat)

    assertThat(nonReversingTween.elapsedRatioForTime(start + 4999.9)).isCloseTo(1.0, 0.001)
    assertThat(nonReversingTween.elapsedRatioForTime(start + 5001.0)).isEqualTo(0.0002)

    //Auto reverse
    val reversingTween = Tween(start, 5.seconds, Easing.linear, repeatType = AnimationRepeatType.RepeatAutoReverse)
    assertThat(reversingTween.elapsedRatioForTime(start + 5001.0)).isEqualTo(1 - 0.0002)
  }

  @Test
  fun testConstant() {
    val tween = Tween.constant(0.7)

    assertThat(tween.interpolate(start)).isEqualTo(0.7)
    assertThat(tween.interpolate(-start)).isEqualTo(0.7)
    assertThat(tween.isFinished(start + 3_600_000.0)).isFalse()
  }

  @Test
  fun testWallClockJumpLeavesTweenUnchanged() {
    assertTweenIgnoresWallClockJump(-1.hours.toDouble(DurationUnit.MILLISECONDS))
  }

  @Test
  fun testWallClockJumpForwardLeavesTweenUnchanged() {
    assertTweenIgnoresWallClockJump(1.hours.toDouble(DurationUnit.MILLISECONDS))
  }

  private fun assertTweenIgnoresWallClockJump(wallClockJump: @ms Double) {
    val wallClock = MutableNowProvider(1_700_000_000_000.0)
    nowProvider = wallClock
    monotonicTimeSource = timeSource

    val renderLoop = RenderLoopSupport()
    var tween: Tween? = null
    var frameTimestamp: @ms Double = Double.NaN
    var interpolated: Double = Double.NaN

    val registration: Disposable = renderLoop.onRender { timestamp, frameMonotonicMillis, _ ->
      val currentTween = tween ?: Tween(frameMonotonicMillis, 2.seconds, Easing.linear).also { tween = it }
      frameTimestamp = timestamp
      interpolated = currentTween.interpolate(frameMonotonicMillis)
    }

    renderLoop.nextLoop(0.0)
    assertThat(interpolated).isEqualTo(0.0)

    timeSource += 500.milliseconds
    wallClock.now += 500.0 + wallClockJump
    renderLoop.nextLoop(500.0)

    assertThat(frameTimestamp).isEqualTo(1_700_000_000_500.0 + wallClockJump)
    assertThat(interpolated).isEqualTo(0.25)

    timeSource += 1500.milliseconds
    wallClock.now += 1500.0
    renderLoop.nextLoop(2000.0)
    assertThat(interpolated).isEqualTo(1.0)
    val startedTween: Tween = tween.requireNotNull { "The first frame starts the tween" }
    assertThat(startedTween.isFinished(monotonicMillis() + 1.0)).isTrue()

    registration.dispose()
  }
}

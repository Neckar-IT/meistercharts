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
package com.meistercharts.canvas.animation

import assertk.*
import assertk.assertions.*
import com.meistercharts.animation.Easing
import it.neckar.open.time.nowMillis
import org.junit.jupiter.api.Test

class TweenTest {
  @Test
  fun testElapsed() {
    val now = nowMillis()

    val tween = Tween(now, 5_000.0, Easing.linear)

    assertThat(tween.elapsedTime(now + 1)).isEqualTo(1.0)
    assertThat(tween.elapsedTime(now + 100)).isEqualTo(100.0)
    assertThat(tween.elapsedTime(now + 99999)).isEqualTo(99999.0)
  }

  @Test
  fun testTweening() {
    val now = nowMillis()

    val tween = Tween(now, 5_000.0, Easing.linear).also {
      assertThat(it.duration).isEqualTo(5_000.0)
    }

    assertThat(tween.startTime).isEqualTo(now)

    assertThat(tween.elapsedRatioForTime(now)).isEqualTo(0.0)
    assertThat(tween.elapsedRatioForTime(now + 1)).isEqualTo(0.0002)

    assertThat(tween.elapsedRatioForTime(now + 2)).isEqualTo(0.0004)

    assertThat(tween.elapsedRatioForTime(now + 20)).isEqualTo(0.004)
    assertThat(tween.elapsedRatioForTime(now + 500)).isEqualTo(0.1)
    assertThat(tween.elapsedRatioForTime(now + 1000)).isEqualTo(0.2)
    assertThat(tween.elapsedRatioForTime(now + 4999)).isEqualTo(1.0 - 0.0002)

    assertThat(tween.elapsedRatioForTime(now + 5000.0)).isEqualTo(1.0)
    assertThat(tween.elapsedRatioForTime(now + 99999.0)).isEqualTo(1.0)
    assertThat(tween.elapsedRatioForTime(now - 99999.0)).isEqualTo(0.0)
  }

  @Test
  fun testRepeat() {
    val now = nowMillis()

    Tween(now, 5_000.0, Easing.linear).let { tween ->
      assertThat(tween.repeatType).isEqualTo(AnimationRepeatType.Once)

      assertThat(tween.elapsedRatioForTime(now + 5000.0)).isEqualTo(1.0)
      //Does *not* repeat
      assertThat(tween.elapsedRatioForTime(now + 1.0)).isEqualTo(0.0002)
      assertThat(tween.elapsedRatioForTime(now + 5001.0)).isEqualTo(1.0)

    }

    //enable repeating
    Tween(now, 5_000.0, Easing.linear, AnimationRepeatType.Repeat).let { tween ->
      assertThat(tween.elapsedRatioForTime(now + 5001.0)).isEqualTo(0.0002)
    }
  }

  @Test
  fun testAutoReverse() {
    val now = nowMillis()
    val nonReversingTween = Tween(now, 5_000.0, Easing.linear, repeatType = AnimationRepeatType.Repeat)

    assertThat(nonReversingTween.elapsedRatioForTime(now + 4999.9)).isCloseTo(1.0, 0.001)
    assertThat(nonReversingTween.elapsedRatioForTime(now + 5000.0 + 1)).isEqualTo(0.0002)

    //Auto reverse
    val reversingTween = Tween(now, 5_000.0, Easing.linear, repeatType = AnimationRepeatType.RepeatAutoReverse)
    assertThat(reversingTween.elapsedRatioForTime(now + 5000.0 + 1)).isEqualTo(1 - 0.0002)
  }
}


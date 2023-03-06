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
import org.junit.jupiter.api.Test

class SequentialAnimationsTest {
  @Test
  fun testIt() {
    val sequentialAnimations = SequentialAnimations(
      listOf(
        animation(10.0),
        animation(20.0),
        animation(30.0),
      )
    )

    assertThat(sequentialAnimations.animations).hasSize(3)
    assertThat(sequentialAnimations.currentAnimationIndex).isEqualTo(0)

    sequentialAnimations.animationFrame(1.0).let {
      assertThat(it).isEqualTo(AnimationState.Active)
    }
    assertThat(sequentialAnimations.currentAnimationIndex).isEqualTo(0)

    sequentialAnimations.animationFrame(10.0)
    assertThat(sequentialAnimations.currentAnimationIndex).isEqualTo(0)
    sequentialAnimations.animationFrame(10.1)
    assertThat(sequentialAnimations.currentAnimationIndex).isEqualTo(1)

    sequentialAnimations.animationFrame(20.1).let {
      assertThat(it).isEqualTo(AnimationState.Active)
    }
    assertThat(sequentialAnimations.currentAnimationIndex).isEqualTo(2)

    //Does *NOT* reset!
    sequentialAnimations.animationFrame(0.1)
    assertThat(sequentialAnimations.currentAnimationIndex).isEqualTo(2)

    //Does *NOT* reset!
    sequentialAnimations.animationFrame(30.1).let {
      assertThat(it).isEqualTo(AnimationState.Finished)
    }
    assertThat(sequentialAnimations.currentAnimationIndex).isEqualTo(3)

  }

  private fun animation(finishedAfter: Double) = Animated { frameTimestamp ->
    AnimationState.finishedIf(frameTimestamp > finishedAfter)
  }
}

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

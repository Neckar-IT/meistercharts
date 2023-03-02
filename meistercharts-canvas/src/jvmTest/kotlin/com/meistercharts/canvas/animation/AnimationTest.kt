package com.meistercharts.canvas.animation

import com.meistercharts.animation.Easing
import org.junit.jupiter.api.Test

/**
 *
 */
class AnimationTest {
  @Test
  fun testAnimation() {
    val definition = TweenDefinition(500.0, Easing.incoming)
    val tween = definition.realize(10_000.0)


  }
}

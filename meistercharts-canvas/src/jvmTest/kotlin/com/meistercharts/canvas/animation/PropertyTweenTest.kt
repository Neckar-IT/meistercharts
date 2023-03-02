package com.meistercharts.canvas.animation

import assertk.*
import assertk.assertions.*
import org.junit.jupiter.api.Test

class PropertyTweenTest {
  @Test
  fun testBasic() {
    val tweenDefinition = TweenDefinition(1000.0)
    val tween = tweenDefinition.realize(40_000.0)

    var currentValue: Double = -1.0

    val propertyTween = PropertyTween(3.0, 4.0, tween) {
      currentValue = it
    }

    assertThat(currentValue).isEqualTo(-1.0)

    propertyTween.update(40_000.0) //start of animation
    assertThat(currentValue).isEqualTo(3.0)

    propertyTween.update(41_000.0) //end of animation
    assertThat(currentValue).isEqualTo(4.0)

    propertyTween.update(40_500.0) //middle of animation
    assertThat(currentValue).isEqualTo(3.5)
  }
}

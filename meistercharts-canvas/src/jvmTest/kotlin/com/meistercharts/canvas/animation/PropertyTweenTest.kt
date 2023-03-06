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

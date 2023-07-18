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
package com.meistercharts.math

import assertk.*
import assertk.assertions.*
import com.meistercharts.animation.Easing
import org.junit.jupiter.api.Test

class EasingTest {
  @Test
  fun testLinear() {
    assertThat(Easing.linear(0.0)).isEqualTo(0.0)
    assertThat(Easing.linear(1.0)).isEqualTo(1.0)
    assertThat(Easing.linear(0.9)).isEqualTo(0.9)
  }

  @Test
  fun testSin() {
    assertThat(Easing.sin(0.0)).isEqualTo(0.0)
    assertThat(Easing.sin(0.5)).isEqualTo(0.479425538604203)
    assertThat(Easing.sin(0.9)).isEqualTo(0.7833269096274834)
    assertThat(Easing.sin(1.0)).isEqualTo(0.8414709848078965)
  }

  @Test
  fun testSmooth() {
    assertThat(Easing.smooth(0.0)).isEqualTo(0.0)
    assertThat(Easing.smooth(1.0)).isEqualTo(1.0)

    assertThat(Easing.smooth(0.1)).isCloseTo(0.028, 0.000000001)
    assertThat(Easing.smooth(0.5)).isEqualTo(0.5)
    assertThat(Easing.smooth(0.9)).isEqualTo(0.972)
  }
}

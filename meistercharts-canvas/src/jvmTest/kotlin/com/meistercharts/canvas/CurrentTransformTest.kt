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
package com.meistercharts.canvas

import assertk.*
import assertk.assertions.*
import org.junit.jupiter.api.Test

class CurrentTransformTest {
  @Test
  fun testTranslate() {
    val currentTransform = CurrentTransform()

    assertThat(currentTransform.translationX).isEqualTo(0.0)
    assertThat(currentTransform.translationY).isEqualTo(0.0)

    currentTransform.translateScaled(2.0, 3.0)
    assertThat(currentTransform.translationX).isEqualTo(2.0)
    assertThat(currentTransform.translationY).isEqualTo(3.0)

    currentTransform.translationX = 17.0
    currentTransform.translationY = 18.0
    assertThat(currentTransform.translationX).isEqualTo(17.0)
    assertThat(currentTransform.translationY).isEqualTo(18.0)

    currentTransform.translateScaled(2.0, 3.0)

    assertThat(currentTransform.translationX).isEqualTo(19.0)
    assertThat(currentTransform.translationY).isEqualTo(21.0)
  }

  @Test
  fun testWithScale() {
    val currentTransform = CurrentTransform()

    currentTransform.scaleX = 1.27
    currentTransform.scaleY = 1.5

    assertThat(currentTransform.translationX).isEqualTo(0.0)
    assertThat(currentTransform.translationY).isEqualTo(0.0)

    currentTransform.translationX = 17.0
    currentTransform.translationY = 18.0

    assertThat(currentTransform.translationX).isEqualTo(17.0)
    assertThat(currentTransform.translationY).isEqualTo(18.0)

    currentTransform.translateScaled(2.0, 3.0)

    assertThat(currentTransform.translationX).isEqualTo(19.54)
    assertThat(currentTransform.translationY).isEqualTo(22.5)
  }
}

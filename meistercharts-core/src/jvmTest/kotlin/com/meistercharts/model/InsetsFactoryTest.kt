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
package com.meistercharts.model

import assertk.assertThat
import assertk.assertions.isEqualTo
import org.junit.jupiter.api.Test

class InsetsFactoryTest {
  @Test
  fun `bottomLeft sets bottom and left`() {
    val insets = Insets.bottomLeft(bottom = 10.0, left = 5.0)
    assertThat(insets.bottom).isEqualTo(10.0)
    assertThat(insets.left).isEqualTo(5.0)
    assertThat(insets.top).isEqualTo(0.0)
    assertThat(insets.right).isEqualTo(0.0)
  }

  @Test
  fun `bottomRight sets bottom and right`() {
    val insets = Insets.bottomRight(bottom = 10.0, right = 5.0)
    assertThat(insets.bottom).isEqualTo(10.0)
    assertThat(insets.right).isEqualTo(5.0)
    assertThat(insets.top).isEqualTo(0.0)
    assertThat(insets.left).isEqualTo(0.0)
  }
}

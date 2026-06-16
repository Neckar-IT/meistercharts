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
package com.meistercharts.color

import assertk.*
import assertk.assertions.*
import org.junit.jupiter.api.Test

class ColorTest {
  /**
   * Regression: black50percent used to be defined as RgbaColor(1.0, 1.0, 1.0, 0.5),
   * which is white at 50% opacity, contradicting both the property name and KDoc.
   */
  @Test
  fun `black50percent is actually black with half opacity`() {
    val color = Color.black50percent()
    assertThat(color.red).isEqualTo(0)
    assertThat(color.green).isEqualTo(0)
    assertThat(color.blue).isEqualTo(0)
    assertThat(color.alpha).isEqualTo(0.5)
  }
}

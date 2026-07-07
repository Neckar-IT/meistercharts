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
package com.meistercharts.canvas.paintable

import assertk.*
import assertk.assertions.*
import org.junit.jupiter.api.Test

class LabelPaintableConfigurationTest {
  /**
   * Pins the maxWidth default. Previously it was Double.NaN, which propagated through
   * `coerceAtMost` and turned every textWidth into NaN, breaking the layout.
   */
  @Test
  fun `default maxWidth is positive infinity, not NaN`() {
    val style = LabelPaintable.Style { _, _ -> "label" }
    assertThat(style.maxWidth.isNaN()).isFalse()

    // Round-trip through coerceAtMost must not turn the input into NaN.
    val width = 42.0.coerceAtMost(style.maxWidth)
    assertThat(width).isEqualTo(42.0)
  }
}

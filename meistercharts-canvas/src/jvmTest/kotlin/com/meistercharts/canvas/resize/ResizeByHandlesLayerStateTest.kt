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
package com.meistercharts.canvas.resize

import assertk.*
import assertk.assertions.*
import it.neckar.geometry.Direction
import org.junit.jupiter.api.Test

class ResizeByHandlesLayerStateTest {
  /**
   * Pins that hovering moves to a different handle. Before the fix, the function
   * returned `this` and ignored the new direction — a hover from handle A to
   * handle B left the state at HoveringOverHandle(A).
   */
  @Test
  fun `hovering moves between handles`() {
    val initial = DefaultState.hoveringAboveHandle(Direction.TopLeft)
    assertThat(initial).isEqualTo(HoveringOverHandle(Direction.TopLeft))

    val moved = initial.hoveringAboveHandle(Direction.TopRight)
    assertThat(moved).isEqualTo(HoveringOverHandle(Direction.TopRight))
  }

  @Test
  fun `hovering with same handle returns the same state`() {
    val state = HoveringOverHandle(Direction.TopLeft)
    val same = state.hoveringAboveHandle(Direction.TopLeft)
    assertThat(same).isSameInstanceAs(state)
  }

  @Test
  fun `hovering with null returns to DefaultState`() {
    val state = HoveringOverHandle(Direction.TopLeft)
    assertThat(state.hoveringAboveHandle(null)).isEqualTo(DefaultState)
  }
}

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
package com.meistercharts.events

import assertk.*
import assertk.assertions.*
import it.neckar.geometry.Coordinates
import it.neckar.events.MouseClickEvent
import it.neckar.events.MouseDoubleClickEvent
import it.neckar.events.MouseDownEvent
import it.neckar.events.MouseDragEvent
import it.neckar.events.MouseEvent
import it.neckar.events.MouseMoveEvent
import it.neckar.events.MouseUpEvent
import it.neckar.events.MouseWheelEvent
import org.junit.jupiter.api.Test

/**
 */
class MouseEventTest {
  @Test
  fun testMouseEventsCompilerCheckWhen() {
    val event: MouseEvent = MouseClickEvent(1.0, Coordinates.origin)

    val result = when (event) {
      is MouseMoveEvent -> "a"
      is MouseClickEvent -> "b"
      is MouseDoubleClickEvent -> "c"
      is MouseWheelEvent -> "d"
      is MouseDragEvent -> "e"
      is MouseDownEvent -> "f"
      is MouseUpEvent -> "g"
    }

    assertThat(result).isEqualTo("b")
  }
}


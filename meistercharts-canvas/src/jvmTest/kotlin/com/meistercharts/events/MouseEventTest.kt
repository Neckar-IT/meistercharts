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
import com.meistercharts.events.MouseClickEvent
import com.meistercharts.events.MouseDoubleClickEvent
import com.meistercharts.events.MouseDownEvent
import com.meistercharts.events.MouseDragEvent
import com.meistercharts.events.MouseEvent
import com.meistercharts.events.MouseMoveEvent
import com.meistercharts.events.MouseUpEvent
import com.meistercharts.events.MouseWheelEvent
import com.meistercharts.model.Coordinates
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


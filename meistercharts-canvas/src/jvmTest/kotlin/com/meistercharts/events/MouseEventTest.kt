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


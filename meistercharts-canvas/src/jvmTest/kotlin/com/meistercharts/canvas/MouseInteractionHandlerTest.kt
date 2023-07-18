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
import com.meistercharts.geometry.Coordinates
import com.meistercharts.events.DefaultMouseEventBroker
import com.meistercharts.events.EventConsumption
import com.meistercharts.events.MouseClickEvent
import com.meistercharts.events.MouseDoubleClickEvent
import com.meistercharts.events.MouseMoveEvent
import org.junit.jupiter.api.Test

internal class MouseInteractionHandlerTest {
  @Test
  internal fun testMouseMovement() {
    val handler = DefaultMouseEventBroker()
    assertThat(handler.mousePosition).isNull()

    handler.notifyMove(MouseMoveEvent(1.0, Coordinates(7.0, 8.0)))
    assertThat(handler.mousePosition).isEqualTo(Coordinates(7.0, 8.0))

    handler.notifyMove(MouseMoveEvent(1.0, Coordinates(7.0, 11.0)))
    assertThat(handler.mousePosition).isEqualTo(Coordinates(7.0, 11.0))

    handler.notifyMove(MouseMoveEvent(1.0, null))
    assertThat(handler.mousePosition).isNull()

    handler.notifyMove(MouseMoveEvent(1.0, Coordinates(17.0, 17.0)))
    assertThat(handler.mousePosition).isEqualTo(Coordinates(17.0, 17.0))
  }

  @Test
  internal fun testMouseMovementHandler() {
    val handler = DefaultMouseEventBroker()
    assertThat(handler.mousePosition).isNull()

    var lastMoveCoordinates: Coordinates? = null
    handler.onMove {
      lastMoveCoordinates = it.coordinates
      EventConsumption.Ignored
    }

    assertThat(lastMoveCoordinates).isNull()
    handler.notifyMove(MouseMoveEvent(1.0, Coordinates(7.0, 8.0)))
    assertThat(lastMoveCoordinates).isEqualTo(Coordinates(7.0, 8.0))

    handler.notifyMove(MouseMoveEvent(1.0, Coordinates(7.0, 11.0)))
    assertThat(lastMoveCoordinates).isEqualTo(Coordinates(7.0, 11.0))

    handler.notifyMove(MouseMoveEvent(1.0, null))
    assertThat(lastMoveCoordinates).isNull()

    handler.notifyMove(MouseMoveEvent(1.0, Coordinates(17.0, 17.0)))
    assertThat(lastMoveCoordinates).isEqualTo(Coordinates(17.0, 17.0))
  }

  @Test
  internal fun testClickHandler() {
    val handler = DefaultMouseEventBroker()

    var lastClickCoordinates: Coordinates? = null
    handler.onClick {
      assertThat(lastClickCoordinates).isNull()
      lastClickCoordinates = it.coordinates
      EventConsumption.Ignored
    }

    assertThat(lastClickCoordinates).isNull()
    handler.notifyClick(MouseClickEvent(1.0, Coordinates(1.0, 2.0)))
    assertThat(lastClickCoordinates).isEqualTo(Coordinates(1.0, 2.0))
  }

  @Test
  internal fun testDoubleClickHandler() {
    val handler = DefaultMouseEventBroker()

    var lastClickCoordinates: Coordinates? = null
    handler.onDoubleClick {
      assertThat(lastClickCoordinates).isNull()
      lastClickCoordinates = it.coordinates
      EventConsumption.Ignored
    }

    assertThat(lastClickCoordinates).isNull()
    handler.notifyDoubleClick(MouseDoubleClickEvent(1.0, Coordinates(1.0, 2.0)))
    assertThat(lastClickCoordinates).isEqualTo(Coordinates(1.0, 2.0))
  }
}

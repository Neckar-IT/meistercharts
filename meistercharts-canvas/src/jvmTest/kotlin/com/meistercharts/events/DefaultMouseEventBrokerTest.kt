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
import it.neckar.events.ModifierCombination
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
class DefaultMouseEventBrokerTest {
  @Test
  internal fun name() {
    val broker = DefaultMouseEventBroker()

    var lastEvent: MouseClickEvent? = null

    broker.onClick {
      assertThat(lastEvent).isNull()
      lastEvent = it
      EventConsumption.Ignored
    }

    assertThat(lastEvent).isNull()
    MouseClickEvent(1.0, Coordinates.none).also {
      broker.notifyClick(it)
      assertThat(lastEvent).isEqualTo(it)
    }
  }

  @Test
  internal fun testRegisterHandler() {
    val broker = DefaultMouseEventBroker()

    var lastEvent: MouseEvent? = null

    val handler: MouseEventHandler = object : MouseEventHandler {
      override fun onClick(event: MouseClickEvent): EventConsumption {
        assertThat(lastEvent).isNull()
        lastEvent = event
        return EventConsumption.Ignored
      }

      override fun onDoubleClick(event: MouseDoubleClickEvent): EventConsumption {
        assertThat(lastEvent).isNull()
        lastEvent = event
        return EventConsumption.Ignored
      }

      override fun onMove(event: MouseMoveEvent): EventConsumption {
        assertThat(lastEvent).isNull()
        lastEvent = event
        return EventConsumption.Ignored
      }

      override fun onWheel(event: MouseWheelEvent): EventConsumption {
        assertThat(lastEvent).isNull()
        lastEvent = event
        return EventConsumption.Ignored
      }

      override fun onDrag(event: MouseDragEvent): EventConsumption {
        assertThat(lastEvent).isNull()
        lastEvent = event
        return EventConsumption.Ignored
      }

      override fun onDown(event: MouseDownEvent): EventConsumption {
        assertThat(lastEvent).isNull()
        lastEvent = event
        return EventConsumption.Ignored
      }

      override fun onUp(event: MouseUpEvent): EventConsumption {
        assertThat(lastEvent).isNull()
        lastEvent = event
        return EventConsumption.Ignored
      }
    }

    broker.register(handler)

    assertThat(lastEvent).isNull()

    lastEvent = null
    MouseClickEvent(1.0, Coordinates.none).also {
      broker.notifyClick(it)
      assertThat(lastEvent).isEqualTo(it)
    }

    lastEvent = null
    MouseDoubleClickEvent(1.0, Coordinates.none).also {
      broker.notifyDoubleClick(it)
      assertThat(lastEvent).isEqualTo(it)
    }

    lastEvent = null
    MouseMoveEvent(1.0, Coordinates.none).also {
      broker.notifyMove(it)
      assertThat(lastEvent).isEqualTo(it)
    }

    lastEvent = null
    MouseWheelEvent(1.0, Coordinates.none, 4.0, ModifierCombination.None).also {
      broker.notifyWheel(it)
      assertThat(lastEvent).isEqualTo(it)
    }

    lastEvent = null
    MouseDragEvent(1.0, Coordinates.none).also {
      broker.notifyDrag(it)
      assertThat(lastEvent).isEqualTo(it)
    }

    lastEvent = null
    MouseDownEvent(1.0, Coordinates.none).also {
      broker.notifyDown(it)
      assertThat(lastEvent).isEqualTo(it)
    }

    lastEvent = null
    MouseUpEvent(1.0, Coordinates.none).also {
      broker.notifyUp(it)
      assertThat(lastEvent).isEqualTo(it)
    }
  }
}

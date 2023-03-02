package com.meistercharts.events

import assertk.*
import assertk.assertions.*
import com.meistercharts.events.DefaultMouseEventBroker
import com.meistercharts.events.EventConsumption
import com.meistercharts.events.ModifierCombination
import com.meistercharts.events.MouseClickEvent
import com.meistercharts.events.MouseDoubleClickEvent
import com.meistercharts.events.MouseDownEvent
import com.meistercharts.events.MouseDragEvent
import com.meistercharts.events.MouseEvent
import com.meistercharts.events.MouseEventHandler
import com.meistercharts.events.MouseMoveEvent
import com.meistercharts.events.MouseUpEvent
import com.meistercharts.events.MouseWheelEvent
import com.meistercharts.events.register
import com.meistercharts.model.Coordinates
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

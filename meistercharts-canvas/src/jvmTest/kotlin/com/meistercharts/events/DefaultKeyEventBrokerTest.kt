package com.meistercharts.events

import assertk.*
import assertk.assertions.*
import com.meistercharts.events.DefaultKeyEventBroker
import com.meistercharts.events.EventConsumption
import com.meistercharts.events.KeyCode
import com.meistercharts.events.KeyDownEvent
import com.meistercharts.events.KeyEvent
import com.meistercharts.events.KeyEventHandler
import com.meistercharts.events.KeyStroke
import com.meistercharts.events.KeyTypeEvent
import com.meistercharts.events.KeyUpEvent
import com.meistercharts.events.register
import org.junit.jupiter.api.Test

class DefaultKeyEventBrokerTest {
  @Test
  fun testIt() {
    val broker = DefaultKeyEventBroker()

    var lastEvent: KeyDownEvent? = null

    broker.onDown {
      assertThat(lastEvent).isNull()
      lastEvent = it
      EventConsumption.Ignored
    }

    assertThat(lastEvent).isNull()
    KeyDownEvent(1.0, "asdf", KeyStroke(KeyCode('A'))).also {
      broker.notifyDown(it)
      assertThat(lastEvent).isEqualTo(it)
    }
  }

  @Test
  internal fun testRegisterHandler() {
    val broker = DefaultKeyEventBroker()

    var lastEvent: KeyEvent? = null

    val handler: KeyEventHandler = object : KeyEventHandler {
      override fun onDown(event: KeyDownEvent): EventConsumption {
        assertThat(lastEvent).isNull()
        lastEvent = event
        return EventConsumption.Ignored
      }

      override fun onUp(event: KeyUpEvent): EventConsumption {
        assertThat(lastEvent).isNull()
        lastEvent = event
        return EventConsumption.Ignored
      }

      override fun onType(event: KeyTypeEvent): EventConsumption {
        assertThat(lastEvent).isNull()
        lastEvent = event
        return EventConsumption.Ignored
      }
    }

    broker.register(handler)

    assertThat(lastEvent).isNull()
    KeyDownEvent(1.0, "asdf", KeyStroke(KeyCode('A'))).also { it: KeyDownEvent ->
      broker.notifyDown(it)
      assertThat(lastEvent).isEqualTo(it)
    }

    lastEvent = null
    KeyUpEvent(1.0, "asdf", KeyStroke(KeyCode('A'))).also { it: KeyUpEvent ->
      broker.notifyUp(it)
      assertThat(lastEvent).isEqualTo(it)
    }

    lastEvent = null
    KeyTypeEvent(1.0, "asdf", KeyStroke(KeyCode('A'))).also { it: KeyTypeEvent ->
      broker.notifyTyped(it)
      assertThat(lastEvent).isEqualTo(it)
    }
  }
}

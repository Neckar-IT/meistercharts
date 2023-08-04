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
import it.neckar.events.KeyCode
import it.neckar.events.KeyDownEvent
import it.neckar.events.KeyEvent
import it.neckar.events.KeyStroke
import it.neckar.events.KeyTypeEvent
import it.neckar.events.KeyUpEvent
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

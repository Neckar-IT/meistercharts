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

import it.neckar.events.KeyDownEvent
import it.neckar.events.KeyTypeEvent
import it.neckar.events.KeyUpEvent

/**
 * Listens for key events
 *
 * Instances *consume* events
 */
interface KeyEventHandler {
  /**
   * Is notified when a key has been pressed
   */
  fun onDown(event: KeyDownEvent): EventConsumption {
    return EventConsumption.Ignored
  }

  /**
   * Is notified when a key has been released
   */
  fun onUp(event: KeyUpEvent): EventConsumption {
    return EventConsumption.Ignored
  }

  /**
   * Is notified when a key has been typed
   */
  fun onType(event: KeyTypeEvent): EventConsumption {
    return EventConsumption.Ignored
  }
}

/**
 * Register the given event handler at the broker
 */
fun KeyEventBroker.register(eventHandler: KeyEventHandler) {
  onDown(eventHandler::onDown)
  onUp(eventHandler::onUp)
  onType(eventHandler::onType)
}

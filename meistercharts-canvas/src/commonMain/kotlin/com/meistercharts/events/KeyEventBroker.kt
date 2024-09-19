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
 * Offers a way to register listeners for key events.
 *
 * Instances *provide* events
 */
interface KeyEventBroker {
  /**
   * Registers a lambda that is notified when a key has been pressed
   */
  fun onDown(handler: KeyDownAction)

  /**
   * Registers a lambda that is notified when a key has been released
   */
  fun onUp(handler: KeyUpAction)

  /**
   * Registers a lambda that is notified when a key has been typed
   */
  fun onType(handler: KeyTypedAction)
}

typealias KeyDownAction = (KeyDownEvent) -> EventConsumption
typealias KeyUpAction = (KeyUpEvent) -> EventConsumption
typealias KeyTypedAction = (KeyTypeEvent) -> EventConsumption

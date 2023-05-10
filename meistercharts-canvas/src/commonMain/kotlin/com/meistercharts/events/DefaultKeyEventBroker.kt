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

import it.neckar.open.kotlin.lang.consumeUntil
import com.meistercharts.events.EventConsumption.Consumed
import com.meistercharts.events.EventConsumption.Ignored

/**
 * Handles key events. Offers a way to register listeners and call them on key events
 *
 */
class DefaultKeyEventBroker : KeyEventBroker {
  /**
   * Handles key pressed events
   */
  private val downCallbacks: MutableList<(KeyDownEvent) -> EventConsumption> = mutableListOf()

  /**
   * Registers a lambda that is notified when a key has been pressed
   */
  override fun onDown(handler: (KeyDownEvent) -> EventConsumption) {
    downCallbacks.add(handler)
  }

  /**
   * Notifies the callbacks that a key has been pressed
   */
  fun notifyDown(keyEvent: KeyDownEvent): EventConsumption {
    return downCallbacks.consumeUntil(Consumed) {
      it(keyEvent)
    } ?: Ignored
  }

  /**
   * Handles key released events
   */
  private val upCallbacks: MutableList<(KeyUpEvent) -> EventConsumption> = mutableListOf()

  /**
   * Registers a lambda that is notified when a key has been released
   */
  override fun onUp(handler: (KeyUpEvent) -> EventConsumption) {
    upCallbacks.add(handler)
  }

  /**
   * Notifies the callbacks that a key has been released
   */
  fun notifyUp(keyEvent: KeyUpEvent): EventConsumption {
    return upCallbacks.consumeUntil(Consumed) {
      it(keyEvent)
    } ?: Ignored
  }

  /**
   * Handles key typed events
   */
  private val typeCallbacks: MutableList<(KeyTypeEvent) -> EventConsumption> = mutableListOf()

  /**
   * Registers a lambda that is notified when a key has been typed
   */
  override fun onType(handler: (KeyTypeEvent) -> EventConsumption) {
    typeCallbacks.add(handler)
  }

  /**
   * Notifies the callbacks that a key has been typed
   */
  fun notifyTyped(keyEvent: KeyTypeEvent): EventConsumption {
    return typeCallbacks.consumeUntil(Consumed) {
      it(keyEvent)
    } ?: Ignored
  }
}

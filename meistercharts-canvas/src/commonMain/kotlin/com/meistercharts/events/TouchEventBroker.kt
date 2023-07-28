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

import it.neckar.events.TouchCancelEvent
import it.neckar.events.TouchEndEvent
import it.neckar.events.TouchMoveEvent
import it.neckar.events.TouchStartEvent

/**
 * Offers a way to register listeners for [TouchEvent]s
 *
 * Instances *provide* events
 *
 */
interface TouchEventBroker {
  /**
   * Registers a callback that is notified when a [TouchStartEvent] is fired
   */
  fun onStart(handler: (TouchStartEvent) -> EventConsumption)

  /**
   * Registers a callback that is notified when a [TouchEndEvent] is fired
   */
  fun onEnd(handler: (TouchEndEvent) -> EventConsumption)

  /**
   * Registers a callback that is notified when a [TouchMoveEvent] is fired
   */
  fun onMove(handler: (TouchMoveEvent) -> EventConsumption)

  /**
   * Registers a callback that is notified when a [TouchCancelEvent] is fired
   */
  fun onCancel(handler: (TouchCancelEvent) -> EventConsumption)
}

/**
 * Register the given event handler at the broker
 */
fun TouchEventBroker.register(eventHandler: TouchEventHandler) {
  onStart(eventHandler::onStart)
  onEnd(eventHandler::onEnd)
  onMove(eventHandler::onMove)
  onCancel(eventHandler::onCancel)
}

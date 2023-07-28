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
import it.neckar.open.kotlin.lang.consumeUntil

/**
 * Handles [TouchEvent]s
 *
 * Offers a way to register listeners and calls them on [TouchEvent]s
 *
 */
class DefaultTouchEventBroker : TouchEventBroker {

  private val touchStartCallbacks: MutableList<(TouchStartEvent) -> EventConsumption> = mutableListOf()
  private val touchEndCallbacks: MutableList<(TouchEndEvent) -> EventConsumption> = mutableListOf()
  private val touchMoveCallbacks: MutableList<(TouchMoveEvent) -> EventConsumption> = mutableListOf()
  private val touchCancelCallbacks: MutableList<(TouchCancelEvent) -> EventConsumption> = mutableListOf()

  override fun onStart(handler: (TouchStartEvent) -> EventConsumption) {
    touchStartCallbacks.add(handler)
  }

  override fun onEnd(handler: (TouchEndEvent) -> EventConsumption) {
    touchEndCallbacks.add(handler)
  }

  override fun onMove(handler: (TouchMoveEvent) -> EventConsumption) {
    touchMoveCallbacks.add(handler)
  }

  override fun onCancel(handler: (TouchCancelEvent) -> EventConsumption) {
    touchCancelCallbacks.add(handler)
  }

  /**
   * Notify this [TouchEventBroker] about [touchEvent]
   */
  fun notifyOnStart(touchEvent: TouchStartEvent): EventConsumption {
    return touchStartCallbacks.consumeUntil(EventConsumption.Consumed) {
      it(touchEvent)
    } ?: EventConsumption.Ignored
  }

  /**
   * Notify this [TouchEventBroker] about [touchEvent]
   */
  fun notifyOnEnd(touchEvent: TouchEndEvent): EventConsumption {
    return touchEndCallbacks.consumeUntil(EventConsumption.Consumed) {
      it(touchEvent)
    } ?: EventConsumption.Ignored
  }

  /**
   * Notify this [TouchEventBroker] about [touchEvent]
   */
  fun notifyOnMove(touchEvent: TouchMoveEvent): EventConsumption {
    return touchMoveCallbacks.consumeUntil(EventConsumption.Consumed) {
      it(touchEvent)
    } ?: EventConsumption.Ignored
  }

  /**
   * Notify this [TouchEventBroker] about [touchEvent]
   */
  fun notifyOnCancel(touchEvent: TouchCancelEvent): EventConsumption {
    return touchCancelCallbacks.consumeUntil(EventConsumption.Consumed) {
      it(touchEvent)
    } ?: EventConsumption.Ignored
  }
}

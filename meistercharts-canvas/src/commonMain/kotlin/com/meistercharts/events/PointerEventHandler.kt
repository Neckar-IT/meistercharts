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

/**
 * Listener for pointer events.
 *
 * Implementations *consume* events
 */
interface PointerEventHandler {
  /**
   * Is notified when a [PointerOverEvent] is fired
   */
  fun onOver(event: PointerOverEvent): EventConsumption {
    return EventConsumption.Ignored
  }

  /**
   * Is notified when a [PointerEnterEvent] is fired
   */
  fun onEnter(event: PointerEnterEvent): EventConsumption {
    return EventConsumption.Ignored
  }

  /**
   * Is notified when a [PointerDownEvent] is fired
   */
  fun onDown(event: PointerDownEvent): EventConsumption {
    return EventConsumption.Ignored
  }

  /**
   * Is notified when a [PointerMoveEvent] is fired
   */
  fun onMove(event: PointerMoveEvent): EventConsumption {
    return EventConsumption.Ignored
  }

  /**
   * Is notified when a [PointerUpEvent] is fired
   */
  fun onUp(event: PointerUpEvent): EventConsumption {
    return EventConsumption.Ignored
  }

  /**
   * Is notified when a [PointerCancelEvent] is fired
   */
  fun onCancel(event: PointerCancelEvent): EventConsumption {
    return EventConsumption.Ignored
  }

  /**
   * Is notified when a [PointerOutEvent] is fired
   */
  fun onOut(event: PointerOutEvent): EventConsumption {
    return EventConsumption.Ignored
  }

  /**
   * Is notified when a [PointerLeaveEvent] is fired
   */
  fun onLeave(event: PointerLeaveEvent): EventConsumption {
    return EventConsumption.Ignored
  }
}

/**
 * Register the given event handler at the broker
 */
fun PointerEventBroker.register(eventHandler: PointerEventHandler) {
  onOver(eventHandler::onOver)
  onEnter(eventHandler::onEnter)
  onDown(eventHandler::onDown)
  onMove(eventHandler::onMove)
  onUp(eventHandler::onUp)
  onCancel(eventHandler::onCancel)
  onOut(eventHandler::onOut)
  onLeave(eventHandler::onLeave)
}

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
 * Listens for key events
 *
 * Instances *consume* events
 */
interface MouseEventHandler {
  /**
   * Is notified when the user clicked on the node
   */
  fun onClick(event: MouseClickEvent): EventConsumption {
    return EventConsumption.Ignored
  }

  /**
   * Is notified when the user double-clicks
   */
  fun onDoubleClick(event: MouseDoubleClickEvent): EventConsumption {
    return EventConsumption.Ignored
  }

  /**
   * Is notified about mouse movements
   */
  fun onMove(event: MouseMoveEvent): EventConsumption {
    return EventConsumption.Ignored
  }

  /**
   * Is notified about dragging events
   */
  fun onDrag(event: MouseDragEvent): EventConsumption {
    return EventConsumption.Ignored
  }

  /**
   * Is notified about mouse wheel events
   */
  fun onWheel(event: MouseWheelEvent): EventConsumption {
    return EventConsumption.Ignored
  }

  /**
   * Is notified about mouse press events
   */
  fun onDown(event: MouseDownEvent): EventConsumption {
    return EventConsumption.Ignored
  }

  /**
   * Is notified about mouse release events
   */
  fun onUp(event: MouseUpEvent): EventConsumption {
    return EventConsumption.Ignored
  }
}

/**
 * Register the given event handler at the broker
 */
fun MouseEventBroker.register(eventHandler: MouseEventHandler) {
  onWheel(eventHandler::onWheel)
  onClick(eventHandler::onClick)
  onDoubleClick(eventHandler::onDoubleClick)
  onMove(eventHandler::onMove)
  onDrag(eventHandler::onDrag)
  onDown(eventHandler::onDown)
  onUp(eventHandler::onUp)
}


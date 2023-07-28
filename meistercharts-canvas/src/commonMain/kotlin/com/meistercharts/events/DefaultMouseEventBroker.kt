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

import com.meistercharts.annotations.Window
import it.neckar.geometry.Coordinates
import it.neckar.open.kotlin.lang.consumeUntil
import com.meistercharts.events.EventConsumption.Consumed
import com.meistercharts.events.EventConsumption.Ignored
import it.neckar.events.MouseClickEvent
import it.neckar.events.MouseDoubleClickEvent
import it.neckar.events.MouseDownEvent
import it.neckar.events.MouseDragEvent
import it.neckar.events.MouseMoveEvent
import it.neckar.events.MouseUpEvent
import it.neckar.events.MouseWheelEvent
import it.neckar.open.observable.ObservableObject
import it.neckar.open.observable.ReadOnlyObservableObject
import it.neckar.open.unit.other.px

/**
 * Interaction stuff that supports mouse clicks
 *
 */
class DefaultMouseEventBroker : MouseEventBroker {
  /**
   * Callbacks for mouse click events
   */
  private val clickCallbacks = mutableListOf<MouseClickAction>()

  /**
   * Callbacks for double-click events
   */
  private val doubleClickCallbacks = mutableListOf<MouseDoubleClickAction>()

  /**
   * Callbacks for mouse movements
   */
  private val moveCallbacks = mutableListOf<MouseMoveAction>()

  /**
   * Callbacks for mouse dragging
   */
  private val dragCallbacks = mutableListOf<MouseDragAction>()

  /**
   * Callbacks for mouse wheel events
   */
  private val wheelCallbacks = mutableListOf<MouseWheelAction>()

  /**
   * Callbacks for mouse wheel events
   */
  private val downCallbacks = mutableListOf<(MouseDownEvent) -> EventConsumption>()

  private val upCallbacks = mutableListOf<(MouseUpEvent) -> EventConsumption>()

  /**
   * Contains the current (latest known) position of the mouse.
   */
  @px
  @Window
  override val mousePositionProperty: ReadOnlyObservableObject<@Window Coordinates?> = ObservableObject<@Window Coordinates?>(null)
    .also { it ->
      //Update the mouse position property when the mouse is moved
      onMove { mouseMoveEvent ->
        it.value = mouseMoveEvent.coordinates
        Ignored
      }
    }

  /**
   * Returns the current mouse position or null if the mouse is not on the canvas
   */
  @px
  @Window
  override val mousePosition: Coordinates? by mousePositionProperty

  /**
   * Registers a callback that is notified about mouse movements
   */
  override fun onMove(callback: MouseMoveAction) {
    moveCallbacks.add(callback)
  }

  /**
   * Is called when the mouse is moved
   */
  @Deprecated("do not use anymore. Do something else instead")
  fun mouseMoved(isActive: (@Window Coordinates) -> Boolean, activateAction: (@Window Coordinates) -> Unit, deactivateAction: () -> Unit) {
    var lastActive = false

    onMove { event ->
      val coordinates = event.coordinates
      if (coordinates == null) {
        if (lastActive) {
          lastActive = false
          deactivateAction()
        }

        return@onMove Ignored
      }

      if (isActive(coordinates)) {
        if (!lastActive) {
          activateAction(coordinates)
        }
        lastActive = true
      } else {
        if (lastActive) {
          deactivateAction()
        }
        lastActive = false
      }

      Ignored
    }
  }

  override fun onDrag(callback: MouseDragAction) {
    dragCallbacks.add(callback)
  }

  /**
   * Register a callback that is notified when the user clicked on the node
   */
  override fun onClick(callback: MouseClickAction) {
    clickCallbacks.add(callback)
  }

  /**
   * Register a callback that is notified when the user double-clicks
   */
  override fun onDoubleClick(callback: MouseDoubleClickAction) {
    doubleClickCallbacks.add(callback)
  }

  override fun onWheel(callback: MouseWheelAction) {
    wheelCallbacks.add(callback)
  }

  override fun onDown(callback: (MouseDownEvent) -> EventConsumption) {
    downCallbacks.add(callback)
  }

  override fun onUp(callback: (MouseUpEvent) -> EventConsumption) {
    upCallbacks.add(callback)
  }

  /**
   * Notifies the mouse interaction handler that a mouse has been clicked
   */
  fun notifyClick(event: MouseClickEvent): EventConsumption {
    return clickCallbacks.consumeUntil(Consumed) {
      it(event)
    } ?: Ignored
  }

  /**
   * Notifies the double-click handlers about a double-click event
   */
  fun notifyDoubleClick(event: MouseDoubleClickEvent): EventConsumption {
    return doubleClickCallbacks.consumeUntil(Consumed) {
      it(event)
    } ?: Ignored
  }

  /**
   * Notifies the callbacks that the mouse has been moved
   */
  fun notifyMove(event: MouseMoveEvent): EventConsumption {
    return moveCallbacks.consumeUntil(Consumed) {
      it(event)
    } ?: Ignored
  }

  /**
   * Notifies the callbacks that the mouse has been dragged
   */
  fun notifyDrag(event: MouseDragEvent): EventConsumption {
    return dragCallbacks.consumeUntil(Consumed) {
      it(event)
    } ?: Ignored
  }

  /**
   * Notifies about wheel events
   */
  fun notifyWheel(event: MouseWheelEvent): EventConsumption {
    return wheelCallbacks.consumeUntil(Consumed) {
      it(event)
    } ?: Ignored
  }

  /**
   * Notifies the callbacks that a mouse button has been pressed
   */
  fun notifyDown(event: MouseDownEvent): EventConsumption {
    return downCallbacks.consumeUntil(Consumed) {
      it(event)
    } ?: Ignored
  }

  /**
   * Notifies the callbacks that a mouse button has been released
   */
  fun notifyUp(event: MouseUpEvent): EventConsumption {
    return upCallbacks.consumeUntil(Consumed) {
      it(event)
    } ?: Ignored
  }
}

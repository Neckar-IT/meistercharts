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
import com.meistercharts.annotations.Zoomed
import com.meistercharts.geometry.Coordinates
import com.meistercharts.geometry.Distance
import it.neckar.open.observable.ReadOnlyObservableObject
import it.neckar.open.unit.other.pct
import it.neckar.open.unit.other.px

/**
 * Offers a way to register mouse events
 *
 * Instances *provide* events
 *
 */
interface MouseEventBroker {
  /**
   * Contains the current/last x position of the mouse.
   *
   * Do *not* use this property to handle events!
   * Instead, use {onMove} and return the correct [EventConsumption] value.
   */
  @px
  @Window
  @Deprecated("Use the on* methods for event handling instead")
  val mousePositionProperty: ReadOnlyObservableObject<@Window Coordinates?>

  /**
   * Returns the current mouse position or null if the mouse is not on the canvas
   */
  @px
  @Window
  val mousePosition: Coordinates?

  /**
   * Register a callback that is notified when the user clicked on the node
   */
  fun onClick(callback: MouseClickAction)

  /**
   * Register a callback that is notified when the user double-clicks
   */
  fun onDoubleClick(callback: MouseDoubleClickAction)

  /**
   * Registers a callback that is notified about mouse movements
   */
  fun onMove(callback: MouseMoveAction)

  /**
   * Registers a callback that is notified about mouse movements
   */
  fun onDrag(callback: MouseDragAction)

  /**
   * Registers a callback that is notified about mouse wheel events
   */
  fun onWheel(callback: MouseWheelAction)

  fun onDown(callback: MouseDownAction)

  fun onUp(callback: MouseUpAction)
}



typealias MouseDoubleClickAction = (MouseDoubleClickEvent) -> EventConsumption
typealias MouseClickAction = (MouseClickEvent) -> EventConsumption
typealias MouseMoveAction = (MouseMoveEvent) -> EventConsumption
typealias MouseDragAction = (MouseDragEvent) -> EventConsumption
typealias MouseWheelAction = (MouseWheelEvent) -> EventConsumption
typealias MouseDownAction = (MouseDownEvent) -> EventConsumption
typealias MouseUpAction = (MouseUpEvent) -> EventConsumption
/**
 * A simple drag action that is only notified about the drags - but not the start/finish events
 * @return whether the event should be consumed
 */
typealias DragAction = (dragDistance: Distance) -> EventConsumption


/**
 * Will be called on translate using a touch screen
 */
typealias TouchPanAction = (oldCenter: @Window Coordinates, newCenter: @Window Coordinates, deltaCenter: @Zoomed Distance) -> EventConsumption

/**
 * Is called on pinch action using a touch screen
 */
typealias TouchPinchAction = (
  oldCenter: @Window Coordinates,
  newCenter: @Window Coordinates,
  oldDistanceBetweenTouches: @Zoomed Distance,
  newDistanceBetweenTouches: @Zoomed Distance,
  /**
   * The zoom factor change.
   *
   * Examples:
   * * 1.0: Nothing has changed
   * * 0.5: Zoomed out
   */
  zoomFactorChangeX: @pct Double,
  zoomFactorChangeY: @pct Double,
) -> EventConsumption

/**
 * Is called on double tap using a touch screen
 */
typealias TouchDoubleTapAction = (coordinates: @Window Coordinates) -> EventConsumption

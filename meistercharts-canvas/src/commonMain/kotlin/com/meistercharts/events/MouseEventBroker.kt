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

import com.meistercharts.algorithms.layers.gesture.MouseClickAction
import com.meistercharts.algorithms.layers.gesture.MouseDoubleClickAction
import com.meistercharts.algorithms.layers.gesture.MouseDragAction
import com.meistercharts.algorithms.layers.gesture.MouseMoveAction
import com.meistercharts.algorithms.layers.gesture.MouseWheelAction
import com.meistercharts.annotations.Window
import com.meistercharts.model.Coordinates
import it.neckar.open.observable.ReadOnlyObservableObject
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
   */
  @px
  @Window
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

  fun onDown(callback: (MouseDownEvent) -> EventConsumption)

  fun onUp(callback: (MouseUpEvent) -> EventConsumption)
}

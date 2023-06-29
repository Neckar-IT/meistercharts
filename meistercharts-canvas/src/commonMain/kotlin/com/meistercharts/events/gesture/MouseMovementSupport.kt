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
package com.meistercharts.events.gesture

import com.meistercharts.annotations.Window
import com.meistercharts.geometry.Coordinates
import com.meistercharts.geometry.Rectangle
import it.neckar.open.observable.ObservableObject
import it.neckar.open.observable.ReadOnlyObservableObject

/**
 * Manages mouse movements.
 * Can be used to handle "mouse over" states.
 */
class MouseMovementSupport(val mousePositionProperty: ReadOnlyObservableObject<@Window Coordinates?>) {
  /**
   * Returns an observable boolean with the mouse over state for the given rectangle
   */
  fun mouseOver(boundsObservable: ObservableObject<Rectangle>): ReadOnlyObservableObject<Boolean> {
    return mousePositionProperty.map(boundsObservable) { coords, bounds ->
      return@map coords != null && bounds.contains(coords)
    }
  }

  /**
   * Register a mouse over action that is notified whenever the mouse is over the given coordinates
   */
  fun mouseOver(boundsObservable: ObservableObject<Rectangle>, function: (Boolean) -> Unit) {
    //The property that should contain "true" if the mouse is over the rectangle
    val isOverRectangle = mouseOver(boundsObservable)

    //Notify the function
    isOverRectangle.consumeImmediately(function)
  }
}

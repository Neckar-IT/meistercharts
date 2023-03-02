package com.meistercharts.events.gesture

import com.meistercharts.annotations.Window
import com.meistercharts.model.Coordinates
import com.meistercharts.model.Rectangle
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

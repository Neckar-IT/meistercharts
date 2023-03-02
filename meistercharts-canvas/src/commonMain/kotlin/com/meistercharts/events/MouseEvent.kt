package com.meistercharts.events

import com.meistercharts.events.UiEvent
import com.meistercharts.annotations.Window
import com.meistercharts.annotations.Zoomed
import com.meistercharts.model.Coordinates
import it.neckar.open.unit.other.px
import it.neckar.open.unit.si.ms
import it.neckar.open.unit.time.RelativeMillis


/**
 * Sealed base class for platform-independent mouse events
 */
sealed class MouseEvent(
  relativeTimestamp: @RelativeMillis Double
) : UiEvent(relativeTimestamp) {
  /**
   * The coordinates of the event
   */
  @Window
  @px
  abstract val coordinates: Coordinates?

  /**
   * The modifier combination that is pressed during the mouse event
   */
  abstract val modifierCombination: ModifierCombination
}

/**
 * A platform-independent mouse-move related event.
 */
class MouseMoveEvent(
  relativeTimestamp: @RelativeMillis Double,
  /**
   * The coordinates of the move event; maybe `null` for a mouse-exit event
   */
  override val coordinates: @Window Coordinates?,
  override val modifierCombination: ModifierCombination = ModifierCombination.None
) : MouseEvent(relativeTimestamp) {
  override fun toString(): String {
    return "Mouse Move @ ${coordinates?.format()}"
  }
}

class MouseDragEvent(
  @ms relativeTimestamp: Double,
  override val coordinates: @Window Coordinates,
  override val modifierCombination: ModifierCombination = ModifierCombination.None
) : MouseEvent(relativeTimestamp) {
  override fun toString(): String {
    return "Mouse Drag @ ${coordinates.format()}"
  }
}

/**
 * A platform-independent mouse-click related event
 */
@Deprecated("In most cases onDown and onUp should be used instead")
class MouseClickEvent(
  relativeTimestamp: @RelativeMillis Double,
  /**
   * The coordinates of the click event
   */
  override val coordinates: @Window Coordinates,
  override val modifierCombination: ModifierCombination = ModifierCombination.None
) : MouseEvent(relativeTimestamp) {
  override fun toString(): String {
    return "Mouse Click @ ${coordinates.format()}"
  }
}

class MouseDownEvent(
  relativeTimestamp: @RelativeMillis Double,
  /**
   * The coordinates of the press event
   */
  override val coordinates: @Window Coordinates,
  override val modifierCombination: ModifierCombination = ModifierCombination.None
) : MouseEvent(relativeTimestamp) {
  override fun toString(): String {
    return "Mouse Down @ ${coordinates.format()}"
  }
}

class MouseUpEvent(
  relativeTimestamp: @RelativeMillis Double,
  /**
   * The coordinates of the release event
   */
  override val coordinates: @Window Coordinates,
  override val modifierCombination: ModifierCombination = ModifierCombination.None
) : MouseEvent(relativeTimestamp) {
  override fun toString(): String {
    return "Mouse Up @ ${coordinates.format()}"
  }
}

/**
 * A platform-independent mouse-double-click related event
 */
class MouseDoubleClickEvent(
  relativeTimestamp: @RelativeMillis Double,
  /**
   * The coordinates of the double-click event
   */
  override val coordinates: @Window Coordinates,

  /**
   * The modifier combination that is pressed during the scroll event
   */
  override val modifierCombination: ModifierCombination = ModifierCombination.None

) : MouseEvent(relativeTimestamp) {
  override fun toString(): String {
    return "Double Click @ ${coordinates.format()}"
  }
}

/**
 * A platform independent mouse wheel event
 */
class MouseWheelEvent(
  relativeTimestamp: @RelativeMillis Double,
  /**
   * The coordinates of the double-click event
   */
  override val coordinates: @Window Coordinates,
  /**
   * The scrolled distance in pixels
   */
  val delta: @Zoomed Double,

  override val modifierCombination: ModifierCombination = ModifierCombination.None
) : MouseEvent(relativeTimestamp) {
  override fun toString(): String {
    return "MouseWheel ($delta) @ ${coordinates.format()}"
  }
}

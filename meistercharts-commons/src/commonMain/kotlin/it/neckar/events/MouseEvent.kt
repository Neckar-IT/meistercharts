/*
 * Copyright (C) 2013-2026 Neckar IT GmbH, Mössingen, Germany
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Linking this library statically or dynamically with other modules is
 * making a combined work based on this library. Thus, the terms and
 * conditions of the GNU General Public License cover the whole combination.
 *
 * As a special exception, the copyright holders of this library give you
 * permission to link this library with independent modules, regardless of
 * the license terms of these independent modules, and to copy and distribute
 * the resulting combined work under terms of your choice, provided that every
 * copy of the combined work is accompanied by a complete copy of the source
 * code of this library.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package it.neckar.events

import com.meistercharts.annotations.Window
import com.meistercharts.annotations.Zoomed
import it.neckar.geometry.Coordinates
import it.neckar.open.unit.number.MayBeNaN
import it.neckar.open.unit.other.px
import it.neckar.open.unit.si.ms
import it.neckar.open.unit.time.RelativeMillis


/**
 * Sealed base class for platform-independent mouse events
 */
sealed class MouseEvent(
  relativeTimestamp: @RelativeMillis Double,
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

  /**
   * The x-coordinate of the event.
   * Returns `NaN` if the event has no coordinates.
   */
  val x: @MayBeNaN Double
    get() = coordinates?.x ?: Double.NaN

  /**
   * The y-coordinate of the event.
   * Returns `NaN` if the event has no coordinates.
   */
  val y: @MayBeNaN Double
    get() = coordinates?.y ?: Double.NaN
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
  override val modifierCombination: ModifierCombination = ModifierCombination.None,
) : MouseEvent(relativeTimestamp) {
  override fun toString(): String {
    return "Mouse Move @ ${coordinates?.format()}"
  }
}

class MouseDragEvent(
  @ms relativeTimestamp: Double,
  override val coordinates: @Window Coordinates,
  val button: MouseButton,
  override val modifierCombination: ModifierCombination = ModifierCombination.None,
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
  val button: MouseButton,
  override val modifierCombination: ModifierCombination = ModifierCombination.None,
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
  val button: MouseButton,
  override val modifierCombination: ModifierCombination = ModifierCombination.None,
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
  val button: MouseButton,
  override val modifierCombination: ModifierCombination = ModifierCombination.None,
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
   * The coordinates of the double click event
   */
  override val coordinates: @Window Coordinates,
  val button: MouseButton,

  /**
   * The modifier combination that is pressed during the scroll event
   */
  override val modifierCombination: ModifierCombination = ModifierCombination.None,

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
   * The coordinates of the double click event
   */
  override val coordinates: @Window Coordinates,
  /**
   * The scrolled distance in pixels
   */
  val delta: @Zoomed Double,

  override val modifierCombination: ModifierCombination = ModifierCombination.None,
) : MouseEvent(relativeTimestamp) {
  override fun toString(): String {
    return "MouseWheel ($delta) @ ${coordinates.format()}"
  }
}

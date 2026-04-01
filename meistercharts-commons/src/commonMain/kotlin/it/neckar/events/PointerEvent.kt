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
import it.neckar.geometry.Coordinates
import it.neckar.geometry.Distance
import it.neckar.open.unit.other.px
import it.neckar.open.unit.time.RelativeMillis
import kotlin.jvm.JvmInline

/**
 * Sealed base class for platform-independent pointer events.
 *
 * The idea of pointer-events comes from JavaScript (see [developper.mozilla.org](https://developer.mozilla.org/en-US/docs/Web/API/PointerEvent))
 */
@Deprecated("Use Touch and Mouse events instead")
sealed class PointerEvent(
  relativeTimestamp: @RelativeMillis Double,
  /** The [Pointer] associated with this event */
  val pointer: Pointer,
) : UiEvent(relativeTimestamp) {
  /**
   * The coordinates of the event
   */
  val coordinates: Coordinates
    get() = pointer.coordinates

  /**
   * The modifier combination that is pressed during the pointer event
   */
  abstract val modifierCombination: ModifierCombination
}

/**
 * This event is fired when a pointing device is moved into an element's hit test boundaries.
 */
@Deprecated("Use Touch and Mouse events instead")
class PointerOverEvent(
  relativeTimestamp: @RelativeMillis Double,
  pointer: Pointer,
  override val modifierCombination: ModifierCombination = ModifierCombination.None,
) : PointerEvent(relativeTimestamp, pointer) {
  override fun toString(): String {
    return "Pointer Over @ ${pointer.coordinates}, id=${pointer.pointerId.id}"
  }
}

/**
 * This event is fired when a pointing device is moved into the hit test boundaries of an element
 * or one of its descendants, including as a result of a pointerdown event from a device that does
 * not support hover (see [PointerDownEvent]). This event type is similar to pointerover, but differs in
 * that it does not bubble.
 */
@Deprecated("Use Touch and Mouse events instead")
class PointerEnterEvent(
  relativeTimestamp: @RelativeMillis Double,
  pointer: Pointer,
  override val modifierCombination: ModifierCombination = ModifierCombination.None,
) : PointerEvent(relativeTimestamp, pointer) {
  override fun toString(): String {
    return "Pointer Enter @ ${pointer.coordinates}, id=${pointer.pointerId.id}"
  }
}

/**
 * The event is fired when a pointer becomes active.
 * - For mouse, it is fired when the device transitions from no buttons depressed to at least one button depressed.
 * - For touch, it is fired when physical contact is made with the digitizer.
 * - For pen, it is fired when the stylus makes physical contact with the digitizer.
 */
@Deprecated("Use Touch and Mouse events instead")
class PointerDownEvent(
  relativeTimestamp: @RelativeMillis Double,
  pointer: Pointer,
  override val modifierCombination: ModifierCombination = ModifierCombination.None,
) : PointerEvent(relativeTimestamp, pointer) {
  override fun toString(): String {
    return "Pointer Down @ ${pointer.coordinates}, id=${pointer.pointerId.id}"
  }
}

/**
 * This event is fired when a pointer changes coordinates.
 */
@Deprecated("Use Touch and Mouse events instead")
class PointerMoveEvent(
  relativeTimestamp: @RelativeMillis Double,
  pointer: Pointer,
  override val modifierCombination: ModifierCombination = ModifierCombination.None,
) : PointerEvent(relativeTimestamp, pointer) {
  override fun toString(): String {
    return "Pointer Move @ ${pointer.coordinates}, id=${pointer.pointerId.id}"
  }
}

/**
 * This event is fired when a pointer is no longer active.
 */
@Deprecated("Use Touch and Mouse events instead")
class PointerUpEvent(
  relativeTimestamp: @RelativeMillis Double,
  pointer: Pointer,
  override val modifierCombination: ModifierCombination = ModifierCombination.None,
) : PointerEvent(relativeTimestamp, pointer) {
  override fun toString(): String {
    return "Pointer Up @ ${pointer.coordinates}, id=${pointer.pointerId.id}"
  }
}

/**
 * A browser fires this event if it concludes the pointer will no longer be able to generate events (for example the related device is deactivated).
 */
class PointerCancelEvent(
  relativeTimestamp: @RelativeMillis Double,
  pointer: Pointer,
  override val modifierCombination: ModifierCombination = ModifierCombination.None,
) : PointerEvent(relativeTimestamp, pointer) {
  override fun toString(): String {
    return "Pointer Cancel @ ${pointer.coordinates}, id=${pointer.pointerId.id}"
  }
}

/**
 * This event is fired for several reasons including:
 * - pointing device is moved out of the hit test boundaries of an element;
 * - firing the pointerup event for a device that does not support hover (see [PointerUpEvent]);
 * - after firing the pointercancel event (see [PointerCancelEvent]);
 * - when a pen stylus leaves the hover range detectable by the digitizer.
 */
@Deprecated("Use Touch and Mouse events instead")
class PointerOutEvent(
  relativeTimestamp: @RelativeMillis Double,
  pointer: Pointer,
  override val modifierCombination: ModifierCombination = ModifierCombination.None,
) : PointerEvent(relativeTimestamp, pointer) {
  override fun toString(): String {
    return "Pointer Out @ ${pointer.coordinates}, id=${pointer.pointerId.id}"
  }
}

/**
 * This event is fired when a pointing device is moved out of the hit test boundaries of an element.
 * For pen devices, this event is fired when the stylus leaves the hover range detectable by the digitizer.
 */
@Deprecated("Use Touch and Mouse events instead")
class PointerLeaveEvent(
  relativeTimestamp: @RelativeMillis Double,
  pointer: Pointer,
  override val modifierCombination: ModifierCombination = ModifierCombination.None,
) : PointerEvent(relativeTimestamp, pointer) {
  override fun toString(): String {
    return "Pointer Leave @ ${pointer.coordinates}, id=${pointer.pointerId.id}"
  }
}

/**
 * A hardware agnostic representation of input devices (such as a mouse, pen or contact point on a touch-enable surface).
 * The [Pointer] can target a specific coordinate (or set of coordinates) on the contact surface such as a screen.
 */
@Deprecated("Use Touch and Mouse events instead")
data class Pointer(
  /**
   * A unique identifier for the pointer
   */
  val pointerId: PointerId,
  /**
   * The coordinates of the pointer relative to the canvas
   */
  @Window @px val coordinates: Coordinates,
)

/**
 * Calls [Coordinates.deltaAbsolute] for the coordinates of this [Pointer] and [other].
 */
fun Pointer.distanceDeltaAbsolute(other: Pointer): Distance {
  return this.coordinates.deltaAbsolute(other.coordinates)
}

/**
 * An unique identifier for a [Pointer] causing a [PointerEvent].
 *
 * The identifier does not convey any particular meaning and may be randomly generated.
 *
 * For the general idea see [pointerId](https://developer.mozilla.org/en-US/docs/Web/API/PointerEvent/pointerId)
 */
@JvmInline
value class PointerId(val id: Int)


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
import it.neckar.events.KeyCode
import it.neckar.events.KeyDownEvent
import it.neckar.events.KeyStroke
import it.neckar.events.KeyTypeEvent
import it.neckar.events.KeyUpEvent
import it.neckar.events.ModifierCombination
import it.neckar.events.MouseButton
import it.neckar.events.Pointer
import it.neckar.events.PointerCancelEvent
import it.neckar.events.PointerDownEvent
import it.neckar.events.PointerEnterEvent
import it.neckar.events.PointerId
import it.neckar.events.PointerLeaveEvent
import it.neckar.events.PointerMoveEvent
import it.neckar.events.PointerOutEvent
import it.neckar.events.PointerOverEvent
import it.neckar.events.PointerUpEvent
import it.neckar.events.TouchCancelEvent
import it.neckar.events.TouchEndEvent
import it.neckar.events.TouchEvent
import it.neckar.events.TouchId
import it.neckar.events.TouchMoveEvent
import it.neckar.events.TouchStartEvent
import it.neckar.geometry.Coordinates
import it.neckar.geometry.Rectangle
import org.w3c.dom.DOMRect
import org.w3c.dom.Touch
import org.w3c.dom.TouchList
import org.w3c.dom.events.Event
import org.w3c.dom.events.KeyboardEvent
import org.w3c.dom.events.MouseEvent
import org.w3c.dom.pointerevents.PointerEvent

/**
 * Returns the event timeStamp as Double.
 * The bindings declare timeStamp as Number: on JS a plain cast happens to work, but on Wasm the
 * external Number is not a Kotlin Double - the cast throws for every event (KT-44194 follow-up).
 */
val Event.timeStampAsDoubleWorkaround: Double
  get() = timeStamp.toDouble()

/**
 * Converts a JavaScript [KeyboardEvent] to a [com.meistercharts.events.KeyTypeEvent]
 */
fun KeyboardEvent.convertType(): KeyTypeEvent {
  //for browser compatibility (https://www.w3schools.com/jsref/event_key_keycode.asp)
  val combinedCode = this.keyCode or this.which

  return KeyTypeEvent(
    timeStampAsDoubleWorkaround,
    this.key,
    KeyStroke(
      KeyCode(combinedCode),
      extractModifierCombination()
    )
  )
}

/**
 * Converts a JavaScript [KeyboardEvent] to a [com.meistercharts.events.KeyDownEvent]
 */
fun KeyboardEvent.convertPress(): KeyDownEvent {
  //for browser compatibility (https://www.w3schools.com/jsref/event_key_keycode.asp)
  val combinedCode = this.keyCode or this.which

  return KeyDownEvent(
    timeStampAsDoubleWorkaround,
    this.key,
    KeyStroke(
      KeyCode(combinedCode),
      extractModifierCombination()
    )
  )
}

/**
 * Converts a JavaScript [KeyboardEvent] to a [com.meistercharts.events.KeyUpEvent]
 */
fun KeyboardEvent.convertRelease(): KeyUpEvent {
  //for browser compatibility (https://www.w3schools.com/jsref/event_key_keycode.asp)
  val combinedCode = this.keyCode or this.which

  return KeyUpEvent(
    timeStampAsDoubleWorkaround,
    this.key,
    KeyStroke(
      KeyCode(combinedCode),
      extractModifierCombination()
    )
  )
}

/**
 * Creates a [Pointer] from data of this [PointerEvent]
 */
val PointerEvent.pointer: Pointer
  get() {
    return Pointer(
      PointerId(this.pointerId),
      offset()
    )
  }

/**
 * Converts a JavaScript [PointerEvent] to a [PointerOverEvent]
 */
fun PointerEvent.convertOver(): PointerOverEvent {
  return PointerOverEvent(timeStampAsDoubleWorkaround, this.pointer, this.extractModifierCombination())
}

/**
 * Converts a JavaScript [PointerEvent] to a [PointerEnterEvent]
 */
fun PointerEvent.convertEnter(): PointerEnterEvent {
  return PointerEnterEvent(timeStampAsDoubleWorkaround, this.pointer, this.extractModifierCombination())
}

/**
 * Converts a JavaScript [PointerEvent] to a [PointerDownEvent]
 */
fun PointerEvent.convertDown(): PointerDownEvent {
  return PointerDownEvent(timeStampAsDoubleWorkaround, this.pointer, this.extractModifierCombination())
}

/**
 * Converts a JavaScript [PointerEvent] to a [PointerMoveEvent]
 */
fun PointerEvent.convertMove(): PointerMoveEvent {
  return PointerMoveEvent(timeStampAsDoubleWorkaround, this.pointer, this.extractModifierCombination())
}

/**
 * Converts a JavaScript [PointerEvent] to a [PointerUpEvent]
 */
fun PointerEvent.convertUp(): PointerUpEvent {
  return PointerUpEvent(timeStampAsDoubleWorkaround, this.pointer, this.extractModifierCombination())
}

/**
 * Converts a JavaScript [PointerEvent] to a [PointerCancelEvent]
 */
fun PointerEvent.convertCancel(): PointerCancelEvent {
  return PointerCancelEvent(timeStampAsDoubleWorkaround, this.pointer, this.extractModifierCombination())
}

/**
 * Converts a JavaScript [PointerEvent] to a [PointerOutEvent]
 */
fun PointerEvent.convertOut(): PointerOutEvent {
  return PointerOutEvent(timeStampAsDoubleWorkaround, this.pointer, this.extractModifierCombination())
}

/**
 * Converts a JavaScript [PointerEvent] to a [PointerLeaveEvent]
 */
fun PointerEvent.convertLeave(): PointerLeaveEvent {
  return PointerLeaveEvent(timeStampAsDoubleWorkaround, this.pointer, this.extractModifierCombination())
}

/**
 * Converts a JavaScript [TouchEvent] to a platform independent [TouchStartEvent]
 * @param boundingClientLocation the location of the element (as returned by `getBoundingClientRect`) on which the touch event occurred
 */
fun org.w3c.dom.TouchEvent.convertStart(boundingClientLocation: Coordinates): TouchStartEvent {
  return TouchStartEvent(
    timeStampAsDoubleWorkaround,
    changedTouches.convert(boundingClientLocation),
    targetTouches.convert(boundingClientLocation),
    touches.convert(boundingClientLocation),
    this.extractModifierCombination()
  )
}

/**
 * Converts a JavaScript [TouchEvent] to a platform independent [TouchEndEvent]
 * @param boundingClientLocation the location of the element (as returned by `getBoundingClientRect`) on which the touch event occurred
 */
fun org.w3c.dom.TouchEvent.convertEnd(boundingClientLocation: Coordinates): TouchEndEvent {
  return TouchEndEvent(
    timeStampAsDoubleWorkaround,
    changedTouches.convert(boundingClientLocation),
    targetTouches.convert(boundingClientLocation),
    touches.convert(boundingClientLocation),
    this.extractModifierCombination()
  )
}

/**
 * Converts a JavaScript [TouchEvent] to a platform independent [TouchMoveEvent]
 * @param boundingClientLocation the location of the element (as returned by `getBoundingClientRect`) on which the touch event occurred
 */
fun org.w3c.dom.TouchEvent.convertMove(boundingClientLocation: Coordinates): TouchMoveEvent {
  return TouchMoveEvent(
    timeStampAsDoubleWorkaround,
    changedTouches.convert(boundingClientLocation),
    targetTouches.convert(boundingClientLocation),
    touches.convert(boundingClientLocation),
    this.extractModifierCombination()
  )
}

/**
 * Converts a JavaScript [TouchEvent] to a platform independent [TouchCancelEvent]
 * @param boundingClientLocation the location of the element (as returned by `getBoundingClientRect`) on which the touch event occurred
 */
fun org.w3c.dom.TouchEvent.convertCancel(boundingClientLocation: Coordinates): TouchCancelEvent {
  return TouchCancelEvent(
    timeStampAsDoubleWorkaround,
    changedTouches.convert(boundingClientLocation),
    targetTouches.convert(boundingClientLocation),
    touches.convert(boundingClientLocation),
    this.extractModifierCombination()
  )
}

/**
 * Converts a JavaScript [TouchList] to a platform independent list of [Touch]es
 * @param boundingClientLocation the location of the element (as returned by `getBoundingClientRect`) on which the touch event occurred
 */
fun TouchList.convert(boundingClientLocation: Coordinates): List<it.neckar.events.Touch> {
  val result: MutableList<it.neckar.events.Touch> = mutableListOf()
  for (i in 0 until this.length) {
    this.item(i)?.convert(boundingClientLocation)?.let {
      result.add(it)
    }
  }
  return result
}

/**
 * Converts a JavaScript touch to a platform independent [Touch]
 * @param boundingClientLocation the location of the element (as returned by `getBoundingClientRect`) on which the touch event occurred
 */
fun org.w3c.dom.Touch.convert(boundingClientLocation: Coordinates): it.neckar.events.Touch {
  //TODO additional values
  //https://developer.mozilla.org/en-US/docs/Web/API/Touch#touch_area
  //
  //Touch.radiusX
  //Touch.radiusY
  //Touch.rotationAngle
  //Touch.force

  return it.neckar.events.Touch(
    TouchId(identifier),
    Coordinates(
      clientX - boundingClientLocation.x, // same as "pageX - boundingClientLocation.x - window.pageXOffset",
      clientY - boundingClientLocation.y // same as "pageY - boundingClientLocation.y - window.pageYOffset"
    )
  )
}

/**
 * Extracts the modifiers
 */
fun KeyboardEvent.extractModifierCombination(): ModifierCombination = ModifierCombination(shiftKey, ctrlKey, altKey, metaKey)

/**
 * Extracts the modifiers
 */
fun MouseEvent.extractModifierCombination(): ModifierCombination = ModifierCombination(shiftKey, ctrlKey, altKey, metaKey)

/**
 * Returns the mouse button
 */
fun MouseEvent.extractButton(): MouseButton {
  return MouseButton.fromDomButtonCode(button)
}


/**
 * Extracts the modifiers
 */
fun org.w3c.dom.TouchEvent.extractModifierCombination(): ModifierCombination = ModifierCombination(shiftKey, ctrlKey, altKey, metaKey)


/**
 * Returns the offset of the mouse event
 */
fun MouseEvent.offset(): Coordinates {
  return Coordinates.of(offsetX, offsetY)
}

/**
 * Converts this [DOMRect] into a [Rectangle] with the same size and location.
 */
fun DOMRect.convert(): Rectangle {
  return Rectangle(left, top, width, height)
}

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
import it.neckar.events.KeyCode
import it.neckar.events.KeyDownEvent
import it.neckar.events.KeyStroke
import it.neckar.events.KeyTypeEvent
import it.neckar.events.KeyUpEvent
import it.neckar.events.ModifierCombination
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
import it.neckar.logging.LoggerFactory
import org.w3c.dom.DOMRect
import org.w3c.dom.Touch
import org.w3c.dom.TouchList
import org.w3c.dom.events.Event
import org.w3c.dom.events.KeyboardEvent
import org.w3c.dom.events.MouseEvent
import org.w3c.dom.pointerevents.PointerEvent

private val logger = LoggerFactory.getLogger("com.meistercharts.js.ChartingUtilsJS")


/**
 * Returns the timeStampAsDoubleWorkaround - workaround for issue https://youtrack.jetbrains.com/issue/KT-44194
 */
@Deprecated("No longer required?")
val Event.timeStampAsDoubleWorkaround: Double
  get() = timeStamp as Double

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

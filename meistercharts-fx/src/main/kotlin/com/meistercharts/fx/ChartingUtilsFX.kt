/*
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
package com.meistercharts.fx

import com.meistercharts.annotations.Window
import com.meistercharts.canvas.MouseCursor
import com.meistercharts.environment
import com.meistercharts.events.DefaultMouseEventBroker
import com.meistercharts.events.EventConsumption
import it.neckar.events.KeyDownEvent
import it.neckar.events.KeyStroke
import it.neckar.events.KeyTypeEvent
import it.neckar.events.KeyUpEvent
import it.neckar.events.ModifierCombination
import it.neckar.events.MouseButton
import it.neckar.events.MouseClickEvent
import it.neckar.events.MouseDoubleClickEvent
import it.neckar.events.MouseDownEvent
import it.neckar.events.MouseDragEvent
import it.neckar.events.MouseMoveEvent
import it.neckar.events.MouseUpEvent
import it.neckar.events.MouseWheelEvent
import it.neckar.events.Touch
import it.neckar.events.TouchEndEvent
import it.neckar.events.TouchId
import it.neckar.events.TouchMoveEvent
import it.neckar.events.TouchStartEvent
import it.neckar.geometry.Coordinates
import it.neckar.geometry.Rectangle
import it.neckar.geometry.Size
import it.neckar.open.javafx.cursor.AdditionalCursor
import it.neckar.open.kotlin.lang.requireNotNull
import it.neckar.open.time.nowMillis
import it.neckar.open.unit.other.px
import javafx.geometry.Bounds
import javafx.scene.Cursor
import javafx.scene.Node
import javafx.scene.canvas.Canvas
import javafx.scene.input.GestureEvent
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.input.MouseEvent
import javafx.scene.input.ScrollEvent
import javafx.scene.input.TouchPoint

/**
 * Extension methods / utility methods for JavaFX related to charting
 */

/**
 * Returns the coordinates of the event
 */
val MouseEvent.coordinates: Coordinates
  get() {
    return Coordinates.of(x / environment.devicePixelRatio, y / environment.devicePixelRatio)
  }

/**
 * The button that is held down - null when none is. [extractButton] answers a different question: which button caused
 * the event, which an entry has none of.
 */
fun MouseEvent.extractHeldButton(): MouseButton? {
  return when {
    isPrimaryButtonDown -> MouseButton.Primary
    isMiddleButtonDown -> MouseButton.Middle
    isSecondaryButtonDown -> MouseButton.Secondary
    else -> null
  }
}

/**
 * Entering with a button down continues the gesture it belongs to - a move would report that gesture as released.
 */
fun DefaultMouseEventBroker.notifyEnter(event: MouseEvent): EventConsumption {
  val heldButton = event.extractHeldButton() ?: return notifyMove(event.convertMove())

  return notifyDrag(event.convertDrag(heldButton))
}

/**
 * Converts a JavaFX mouse event to a charting mouse event
 */
fun MouseEvent.convertMove(): MouseMoveEvent {
  return MouseMoveEvent(nowMillis(), coordinates, extractModifierCombination())
}

fun MouseEvent.convertExit(): MouseMoveEvent {
  return MouseMoveEvent(nowMillis(), null, extractModifierCombination())
}

/**
 * Converts a JavaFX mouse event to a charting mouse event
 */
fun MouseEvent.convertDrag(button: MouseButton = extractButton()): MouseDragEvent {
  return MouseDragEvent(nowMillis(), coordinates, button, extractModifierCombination())
}

/**
 * Converts a JavaFX mouse event to a charting mouse click event
 */
fun MouseEvent.convertClick(): MouseClickEvent {
  return MouseClickEvent(nowMillis(), coordinates, extractButton(), extractModifierCombination())
}

fun MouseEvent.convertDown(): MouseDownEvent {
  return MouseDownEvent(
    relativeTimestamp = nowMillis(),
    coordinates = coordinates,
    button = extractButton(),
    modifierCombination = extractModifierCombination(),
  )
}

fun MouseEvent.convertUp(): MouseUpEvent {
  return MouseUpEvent(relativeTimestamp = nowMillis(), coordinates = coordinates, button = extractButton(), modifierCombination = extractModifierCombination())
}

/**
 * Converts a JavaFX mouse event to a charting mouse double click event
 */
fun MouseEvent.convertDoubleClick(): MouseDoubleClickEvent {
  return MouseDoubleClickEvent(relativeTimestamp = nowMillis(), coordinates = coordinates, button = extractButton(), modifierCombination = extractModifierCombination())
}

/**
 * Returns the coordinates for the touch event
 */
val javafx.scene.input.TouchEvent.coordinates: Coordinates
  get() {
    this.touchPoint.requireNotNull { "Touch point must not be null" }

    return Coordinates.of(touchPoint.x / environment.devicePixelRatio, touchPoint.y / environment.devicePixelRatio)
  }


/**
 * The coordinates of a touch point
 */
val TouchPoint.coordinates: Coordinates
  get() {
    return Coordinates(x / environment.devicePixelRatio, y / environment.devicePixelRatio)
  }


/**
 * Converts a (JavaFX) TouchPoint to a MeisterCharts Touch
 */
fun TouchPoint.toTouch(): Touch {
  return Touch(TouchId(id), coordinates)
}

/**
 * Converts a touch event
 */
fun javafx.scene.input.TouchEvent.convertStart(canvas: Canvas): TouchStartEvent {
  //Convert *all* touch points first
  val touches: List<Touch> = touchPoints.map {
    it.toTouch()
  }

  //Collect the changed touches
  val targetTouches = touches.filterIndexed { index, _ ->
    touchPoints[index].belongsTo(canvas)
  }

  //Collect the added touches
  val changedTouches = touches.filterIndexed { index, _ ->
    val touchPoint = touchPoints[index]
    touchPoint.belongsTo(canvas) && touchPoint.state == TouchPoint.State.PRESSED
  }

  return TouchStartEvent(nowMillis(), changedTouches, targetTouches, touches)
}

fun javafx.scene.input.TouchEvent.convertMove(canvas: Canvas): TouchMoveEvent {
  //Convert *all* touch points first
  val touches: List<Touch> = touchPoints.map {
    it.toTouch()
  }

  //Collect the target touches
  val targetTouches = touches.filterIndexed { index, _ ->
    touchPoints[index].belongsTo(canvas)
  }

  //Collect the moved touches
  val changedTouches = touches.filterIndexed { index, _ ->
    val touchPoint = touchPoints[index]
    touchPoint.belongsTo(canvas) && touchPoint.state == TouchPoint.State.MOVED
  }

  return TouchMoveEvent(nowMillis(), changedTouches, targetTouches, touches)
}

fun javafx.scene.input.TouchEvent.convertEnd(canvas: Canvas): TouchEndEvent {
  //Convert *all* touch points first
  val touches: List<Touch> = touchPoints.map {
    it.toTouch()
  }

  //Collect the removed touches
  val removedTouches = touches.filterIndexed { index, _ ->
    val touchPoint = touchPoints[index]
    touchPoint.belongsTo(canvas) && touchPoint.state == TouchPoint.State.RELEASED
  }

  //Collect the remaining touches
  val remainingTouches = touches.filterIndexed { index, _ ->
    touchPoints[index].state != TouchPoint.State.RELEASED
  }

  //Collect the remaining (target) touches.
  //Attention: This must be filtered from [touches] - the indices of [remainingTouches] no longer match those of [touchPoints]
  val targetTouches = touches.filterIndexed { index, _ ->
    val touchPoint = touchPoints[index]
    touchPoint.state != TouchPoint.State.RELEASED && touchPoint.belongsTo(canvas)
  }

  return TouchEndEvent(nowMillis(), removedTouches, targetTouches, remainingTouches)
}

/**
 * Returns the coordinates for a gesture event
 */
val GestureEvent.coordinates: @Window Coordinates
  get() {
    return Coordinates.of(x / environment.devicePixelRatio, y / environment.devicePixelRatio)
  }

fun ScrollEvent.convertWheel(): MouseWheelEvent {
  // We reverse the sign of deltaY to behave the same way a JavaScript wheel event does
  val delta = when {
    deltaX != 0.0 -> deltaX
    deltaY != 0.0 -> deltaY
    else -> 0.0
  }
  return MouseWheelEvent(nowMillis(), coordinates, -delta, extractModifierCombination())
}

fun GestureEvent.extractModifierCombination(): ModifierCombination {
  return ModifierCombination.get(isShiftDown, isControlDown, isAltDown, isMetaDown)
}

fun MouseEvent.extractModifierCombination(): ModifierCombination {
  return ModifierCombination.get(isShiftDown, isControlDown, isAltDown, isMetaDown)
}

fun MouseEvent.extractButton(): MouseButton {
  return when (button.requireNotNull()) {
    javafx.scene.input.MouseButton.NONE -> throw IllegalStateException("Unexpected button NONE")
    javafx.scene.input.MouseButton.PRIMARY -> MouseButton.Primary
    javafx.scene.input.MouseButton.MIDDLE -> MouseButton.Middle
    javafx.scene.input.MouseButton.SECONDARY -> MouseButton.Secondary
    javafx.scene.input.MouseButton.BACK -> MouseButton.Back
    javafx.scene.input.MouseButton.FORWARD -> MouseButton.Forward
  }
}

/**
 * Converts to a JavaFX cursor
 */
fun MouseCursor.toJavaFx(): Cursor {
  return when (this) {
    MouseCursor.Default -> Cursor.DEFAULT
    MouseCursor.Hand -> Cursor.HAND
    MouseCursor.OpenHand -> Cursor.OPEN_HAND
    MouseCursor.ClosedHand -> Cursor.CLOSED_HAND
    MouseCursor.CrossHair -> Cursor.CROSSHAIR
    MouseCursor.Text -> Cursor.TEXT
    MouseCursor.Busy -> Cursor.WAIT
    MouseCursor.Move -> Cursor.MOVE
    MouseCursor.None -> Cursor.NONE
    MouseCursor.ResizeNorth -> Cursor.N_RESIZE
    MouseCursor.ResizeNorthEast -> Cursor.NE_RESIZE
    MouseCursor.ResizeEast -> Cursor.E_RESIZE
    MouseCursor.ResizeSouthEast -> Cursor.SE_RESIZE
    MouseCursor.ResizeSouth -> Cursor.S_RESIZE
    MouseCursor.ResizeSouthWest -> Cursor.SW_RESIZE
    MouseCursor.ResizeWest -> Cursor.W_RESIZE
    MouseCursor.ResizeNorthWest -> Cursor.NW_RESIZE
    MouseCursor.ResizeEastWest -> Cursor.H_RESIZE
    MouseCursor.ResizeNorthSouth -> Cursor.V_RESIZE

    //Additional cursors
    MouseCursor.NotAllowed -> AdditionalCursor.NotAllowed
    MouseCursor.ZoomIn -> AdditionalCursor.ZoomIn
    MouseCursor.ZoomOut -> AdditionalCursor.ZoomOut
    MouseCursor.Help -> AdditionalCursor.Help
    MouseCursor.ContextMenu -> AdditionalCursor.ContextMenu
    MouseCursor.Pointer -> AdditionalCursor.Pointer
    MouseCursor.Progress -> AdditionalCursor.Progress
    MouseCursor.Cell -> AdditionalCursor.Cell
    MouseCursor.AllScroll -> AdditionalCursor.AllScroll
    MouseCursor.ColResize -> AdditionalCursor.ColResize
    MouseCursor.RowResize -> AdditionalCursor.RowResize
    MouseCursor.NoDrop -> AdditionalCursor.NoDrop
  }
}

/**
 * Converts a JavaFX [KeyEvent] to a [com.meistercharts.events.KeyTypeEvent]
 */
fun KeyEvent.convertType(): KeyTypeEvent {
  //  All type events have "UNDEFINED" set. See javadoc in javafx.scene.input.KeyEvent.getCode
  require(code == KeyCode.UNDEFINED) { "Unexpected code for type. Expected UNDEFINED but was <$code>" }

  return KeyTypeEvent(
    nowMillis(), text, KeyStroke(character.toKeyCode(), extractModifierCombination())
  )
}

/**
 * Converts a JavaFX [KeyEvent] to a [com.meistercharts.events.KeyDownEvent]
 */
fun KeyEvent.convertDown(): KeyDownEvent {
  return KeyDownEvent(
    nowMillis(), text, KeyStroke(code.convert(), extractModifierCombination())
  )
}

/**
 * Converts a JavaFX [KeyEvent] to a [com.meistercharts.events.KeyUpEvent]
 */
fun KeyEvent.convertUp(): KeyUpEvent {
  return KeyUpEvent(
    nowMillis(), text, KeyStroke(code.convert(), extractModifierCombination())
  )
}

/**
 * Extracts the modifier combination from a key event
 */
private fun KeyEvent.extractModifierCombination(): ModifierCombination {
  return ModifierCombination(isShiftDown, isControlDown, isAltDown, isMetaDown)
}

/**
 * Converts a JavaFX [KeyCode] to a [com.meistercharts.events.KeyCode]
 */
private fun KeyCode.convert(): it.neckar.events.KeyCode {
  //
  // ATTENTION! it.neckar.events.KeyCode instances use the *HTML* key codes.
  // Therefore, a mapping is sometimes necessary
  //

  //Mapping with different key codes
  return when (this) {
    KeyCode.DELETE -> it.neckar.events.KeyCode.Delete
    else -> it.neckar.events.KeyCode(this.code)
  }
}

/**
 * Converts a character to a key code
 */
private fun String.toKeyCode(): it.neckar.events.KeyCode {
  require(this.isNotEmpty()) { "Not supported for empty" }
  require(length == 1) { "Unexpected length ($length) for <$this>" }

  return it.neckar.events.KeyCode(this[0])
}

/**
 *
 */
@Deprecated("Do use layerSupport instead")
fun DefaultMouseEventBroker.register(node: Node) {
  node.addEventHandler(MouseEvent.MOUSE_MOVED) {
    notifyMove(it.convertMove())
  }

  node.addEventHandler(MouseEvent.MOUSE_ENTERED) {
    notifyEnter(it)
  }

  node.addEventHandler(MouseEvent.MOUSE_EXITED) {
    notifyMove(MouseMoveEvent(nowMillis(), null, ModifierCombination.None)) //TODO fix modifier combination - or better: Use correct method
  }

  node.addEventHandler(MouseEvent.MOUSE_CLICKED) {
    if (it.clickCount == 2) {
      notifyDoubleClick(it.convertDoubleClick())
    } else {
      notifyClick(it.convertClick())
    }
  }
}

/**
 * Converts JavaFX bounds to rectangle
 */
fun Bounds.toRectangle(): Rectangle {
  return Rectangle(minX, minY, width, height)
}

/**
 * Returns the size of the given bounds
 */
fun Bounds.size(): Size {
  return Size(width, height)
}

/**
 * Returns the size of the image
 */
val javafx.scene.image.Image.size: @px Size
  get() {
    return Size(width, height)
  }

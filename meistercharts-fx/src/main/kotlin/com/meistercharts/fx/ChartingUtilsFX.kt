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
package com.meistercharts.fx

import com.meistercharts.algorithms.environment
import com.meistercharts.annotations.Window
import com.meistercharts.canvas.MouseCursor
import com.meistercharts.model.Coordinates
import com.meistercharts.model.Rectangle
import com.meistercharts.model.Size
import it.neckar.open.time.nowMillis
import com.meistercharts.events.DefaultMouseEventBroker
import com.meistercharts.events.KeyStroke
import com.meistercharts.events.ModifierCombination
import com.meistercharts.events.MouseClickEvent
import com.meistercharts.events.MouseDoubleClickEvent
import com.meistercharts.events.MouseDownEvent
import com.meistercharts.events.MouseDragEvent
import com.meistercharts.events.MouseMoveEvent
import com.meistercharts.events.MouseUpEvent
import com.meistercharts.events.MouseWheelEvent
import com.meistercharts.events.Touch
import com.meistercharts.events.TouchEndEvent
import com.meistercharts.events.TouchId
import com.meistercharts.events.TouchMoveEvent
import com.meistercharts.events.TouchStartEvent
import com.meistercharts.events.KeyDownEvent
import com.meistercharts.events.KeyTypeEvent
import com.meistercharts.events.KeyUpEvent
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
fun MouseEvent.convertDrag(): MouseDragEvent {
  return MouseDragEvent(nowMillis(), coordinates, extractModifierCombination())
}

/**
 * Converts a JavaFX mouse event to a charting mouse click event
 */
fun MouseEvent.convertClick(): MouseClickEvent {
  return MouseClickEvent(nowMillis(), coordinates, extractModifierCombination())
}

fun MouseEvent.convertDown(): MouseDownEvent {
  return MouseDownEvent(nowMillis(), coordinates, extractModifierCombination())
}

fun MouseEvent.convertUp(): MouseUpEvent {
  return MouseUpEvent(nowMillis(), coordinates, extractModifierCombination())
}

/**
 * Converts a JavaFX mouse event to a charting mouse double click event
 */
fun MouseEvent.convertDoubleClick(): MouseDoubleClickEvent {
  return MouseDoubleClickEvent(nowMillis(), coordinates, extractModifierCombination())
}

/**
 * Returns the coordinates for the touch event
 */
val javafx.scene.input.TouchEvent.coordinates: Coordinates
  get() {
    requireNotNull(this.touchPoint) {
      "Touch point must not be null"
    }

    if (this.touchCount == 0) {
      //return null
    }

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
  val removedTouches = touches.filterIndexed { index, touch ->
    val touchPoint = touchPoints[index]
    touchPoint.belongsTo(canvas) && touchPoint.state == TouchPoint.State.RELEASED
  }

  //Collect the remaining touches
  val remainingTouches = touches.filterIndexed { index, touch ->
    val touchPoint = touchPoints[index]
    touchPoint.state != TouchPoint.State.RELEASED
  }

  //Collect the remaining (target) touches
  val targetTouches = remainingTouches.filterIndexed { index, touch ->
    val touchPoint = touchPoints[index]
    touchPoint.belongsTo(canvas)
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

    //Currently not supported by MeisterCharts!!!
    //Cursor.DISAPPEAR
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
private fun KeyCode.convert(): com.meistercharts.events.KeyCode {
  //
  // ATTENTION! com.meistercharts.events.KeyCode instances use the *HTML* key codes.
  // Therefore a mapping is sometimes necessary
  //

  //Mapping with different key codes
  return when (this) {
    KeyCode.DELETE -> com.meistercharts.events.KeyCode.Delete
    else -> com.meistercharts.events.KeyCode(this.impl_getCode())
  }
}

/**
 * Converts a character to a key code
 */
private fun String.toKeyCode(): com.meistercharts.events.KeyCode {
  require(this.isNotEmpty()) { "Not supported for empty" }
  require(length == 1) { "Unexpected length ($length) for <$this>" }

  return com.meistercharts.events.KeyCode(this[0])
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
    notifyMove(it.convertMove())
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

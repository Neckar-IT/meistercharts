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
import com.meistercharts.font.FontDescriptorFragment
import com.meistercharts.font.FontFamily
import com.meistercharts.font.FontSize
import com.meistercharts.font.FontStyle
import com.meistercharts.font.FontVariant
import com.meistercharts.font.FontWeight
import com.meistercharts.canvas.Image
import com.meistercharts.canvas.MouseCursor
import com.meistercharts.geometry.Coordinates
import com.meistercharts.geometry.Rectangle
import com.meistercharts.model.Size
import com.meistercharts.events.KeyCode
import com.meistercharts.events.KeyDownEvent
import com.meistercharts.events.KeyStroke
import com.meistercharts.events.KeyTypeEvent
import com.meistercharts.events.KeyUpEvent
import com.meistercharts.events.ModifierCombination
import com.meistercharts.events.Pointer
import com.meistercharts.events.PointerCancelEvent
import com.meistercharts.events.PointerDownEvent
import com.meistercharts.events.PointerEnterEvent
import com.meistercharts.events.PointerId
import com.meistercharts.events.PointerLeaveEvent
import com.meistercharts.events.PointerMoveEvent
import com.meistercharts.events.PointerOutEvent
import com.meistercharts.events.PointerOverEvent
import com.meistercharts.events.PointerUpEvent
import com.meistercharts.events.Touch
import com.meistercharts.events.TouchCancelEvent
import com.meistercharts.events.TouchEndEvent
import com.meistercharts.events.TouchId
import com.meistercharts.events.TouchMoveEvent
import com.meistercharts.events.TouchStartEvent
import it.neckar.open.unit.other.px
import it.neckar.logging.LoggerFactory
import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.DOMRect
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLImageElement
import org.w3c.dom.TouchEvent
import org.w3c.dom.TouchList
import org.w3c.dom.events.Event
import org.w3c.dom.events.KeyboardEvent
import org.w3c.dom.events.MouseEvent
import org.w3c.dom.pointerevents.PointerEvent

private val logger = LoggerFactory.getLogger("com.meistercharts.js.ChartingUtilsJS")


/**
 * Computes the cursor CSS property for this [MouseCursor]
 */
fun MouseCursor.toCss(): String {
  // https://developer.mozilla.org/en-US/docs/Web/CSS/cursor
  return when (this) {
    MouseCursor.Default -> "default"
    MouseCursor.Hand -> "pointer"
    MouseCursor.OpenHand -> "grab"
    MouseCursor.ClosedHand -> "grabbing"
    MouseCursor.CrossHair -> "crosshair"
    MouseCursor.Text -> "text"
    MouseCursor.Busy -> "wait"
    MouseCursor.Move -> "move"
    MouseCursor.None -> "none"
    MouseCursor.ResizeNorth -> "n-resize"
    MouseCursor.ResizeNorthEast -> "ne-resize"
    MouseCursor.ResizeEast -> "e-resize"
    MouseCursor.ResizeSouthEast -> "se-resize"
    MouseCursor.ResizeSouth -> "s-resize"
    MouseCursor.ResizeSouthWest -> "sw-resize"
    MouseCursor.ResizeWest -> "w-resize"
    MouseCursor.ResizeNorthWest -> "nw-resize"
    MouseCursor.ResizeEastWest -> "ew-resize"
    MouseCursor.ResizeNorthSouth -> "ns-resize"
  }
}

/**
 * Contains a list of mouse cursors that are supported by JS but currently not by MeisterCharts
 */
val additionalMouseCursorTypes = listOf(
  "zoom-in",
  "zoom-out",
  "help",
  "context-menu",
  "pointer",
  "progress",
  "cell",
  "not-allowed",
  "all-scroll",
  "col-resize",
  "row-resize",
  "no-drop"
)

/**
 * Creates an [Image] that wraps an [HTMLImageElement] whose src-attribute is set to [src]
 * @see [createImageElement]
 */
fun createImage(src: String): Image {
  throw UnsupportedOperationException("not implemented yet")
  return Image(createImageElement(src), Size.zero)
}

/**
 * Creates an [HTMLImageElement] whose src-attribute is set to [src]
 * @see [createImage]
 */
fun createImageElement(src: String): HTMLImageElement {
  throw UnsupportedOperationException("not implemented yet")
  return (document.createElement("IMG") as HTMLImageElement).also { it.src = src }
}

/**
 * Returns the timeStampAsDoubleWorkaround - workaround for issue https://youtrack.jetbrains.com/issue/KT-44194
 */
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
fun TouchEvent.convertStart(boundingClientLocation: Coordinates): TouchStartEvent {
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
fun TouchEvent.convertEnd(boundingClientLocation: Coordinates): TouchEndEvent {
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
fun TouchEvent.convertMove(boundingClientLocation: Coordinates): TouchMoveEvent {
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
fun TouchEvent.convertCancel(boundingClientLocation: Coordinates): TouchCancelEvent {
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
fun TouchList.convert(boundingClientLocation: Coordinates): List<Touch> {
  val result: MutableList<Touch> = mutableListOf()
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
fun org.w3c.dom.Touch.convert(boundingClientLocation: Coordinates): Touch {
  return Touch(
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
fun TouchEvent.extractModifierCombination(): ModifierCombination = ModifierCombination(shiftKey, ctrlKey, altKey, metaKey)


/**
 * Returns the offset of the mouse event
 */
fun MouseEvent.offset(): Coordinates {
  return Coordinates.of(offsetX, offsetY)
}

/**
 * Makes this [HTMLElement] unselectable by setting the css user-select property to `none`
 */
fun HTMLElement.unselectable() {
  this.style.setProperty("user-select", "none")
  this.style.setProperty("-moz-user-select", "-moz-none")
  this.style.setProperty("-khtml-user-select", "none")
  this.style.setProperty("-webkit-user-select", "none")
  this.style.setProperty("-ms -user-select", "none")
}

/**
 * Disable the border normally painted by the browser if this [HTMLElement] has the focus
 */
fun HTMLElement.noFocusBorder() {
  this.style.setProperty("outline", "none")
}

/**
 * Converts this [DOMRect] into a [Rectangle] with the same size and location.
 */
fun DOMRect.convert(): Rectangle {
  return Rectangle(left, top, width, height)
}

/**
 * Retrieves the computed font of this element (see [getComputedStyle](https://developer.mozilla.org/en-US/docs/Web/API/Window/getComputedStyle))
 */
fun HTMLElement.font(): FontDescriptorFragment {
  val computedStyle = window.getComputedStyle(this)
  val cssFontSize = computedStyle.fontSize
  val cssFontFamily = computedStyle.fontFamily
  val cssFontWeight = computedStyle.fontWeight
  val cssFontStyle = computedStyle.fontStyle
  val cssFontVariant = computedStyle.fontVariant

  return FontDescriptorFragment(
    size = parseCssFontSize(cssFontSize),
    family = parseCssFontFamily(cssFontFamily),
    weight = parseCssFontWeight(cssFontWeight),
    style = parseCssFontStyle(cssFontStyle),
    variant = parseCssFontVariant(cssFontVariant)
  )
}

/**
 * Parses this string and returns a [FontSize].
 *
 * The string is supposed to be the result of a call like `window.getComputedStyle(element).getPropertyValue("font-size")`
 *
 * @return The font size or `null` if parsing failed
 */
@px
fun parseCssFontSize(fontSize: String): FontSize? {
  try {
    if (fontSize.isBlank()) {
      return null
    }
    // Note: empirically all browsers return a pixel-based font size
    val indexOfPx = fontSize.indexOf("px")
    if (indexOfPx == -1) {
      return null
    }

    return fontSize.substring(0, indexOfPx).trim().toDoubleOrNull()?.let { FontSize(it) }
  } catch (e: Exception) {
    logger.warn("failed to parse font size from <$fontSize>: $e")
  }
  return null
}

fun parseCssFontFamily(fontFamily: String): FontFamily? {
  try {
    val trimmedFontFamily = fontFamily.trim()
    if (trimmedFontFamily.isNullOrBlank()) {
      return null
    }
    return FontFamily(trimmedFontFamily)
  } catch (e: Exception) {
    logger.warn("failed to parse font family from <$fontFamily>: $e")
  }
  return null
}

/**
 * Parses the given CSS font-weight and returns a [FontWeight]
 */
fun parseCssFontWeight(fontWeight: String): FontWeight? {
  try {
    return when (fontWeight) {
      "normal" -> FontWeight.Normal
      "bold"   -> FontWeight.Bold
      else     -> {
        fontWeight.toIntOrNull()?.let { FontWeight(it) }
      }
    }
  } catch (e: Exception) {
    logger.warn("failed to parse font weight from <$fontWeight>: $e")
  }
  return null
}

/**
 * Parses the given CSS font-style and returns a [FontStyle]
 */
fun parseCssFontStyle(fontStyle: String): FontStyle? {
  try {
    return when (fontStyle) {
      "normal"  -> FontStyle.Normal
      "italic"  -> FontStyle.Italic
      "oblique" -> FontStyle.Oblique
      else      -> null
    }
  } catch (e: Exception) {
    logger.warn("failed to parse font style from <$fontStyle>: $e")
  }
  return null
}

/**
 * Parses the given CSS font-variant and returns a [FontVariant]
 */
fun parseCssFontVariant(fontVariant: String): FontVariant? {
  try {
    return when (fontVariant) {
      "normal"     -> FontVariant.Normal
      "small-caps" -> FontVariant.SmallCaps
      else         -> null
    }
  } catch (e: Exception) {
    logger.warn("failed to parse font variant from <$fontVariant>: $e")
  }
  return null
}

/**
 * Returns the size of the html canvas element
 */
val HTMLCanvasElement.size: @px Size
  get() {
    return Size(width, height)
  }

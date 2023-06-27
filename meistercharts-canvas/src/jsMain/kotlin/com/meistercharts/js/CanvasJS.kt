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
package com.meistercharts.js

import com.meistercharts.algorithms.environment
import com.meistercharts.annotations.PhysicalPixel
import com.meistercharts.canvas.AbstractCanvas
import com.meistercharts.canvas.CanvasType
import com.meistercharts.canvas.ChartSizeClassification
import com.meistercharts.canvas.ChartSupport
import com.meistercharts.canvas.Image
import com.meistercharts.events.EventConsumption
import com.meistercharts.events.MouseClickEvent
import com.meistercharts.events.MouseDoubleClickEvent
import com.meistercharts.events.MouseDownEvent
import com.meistercharts.events.MouseDragEvent
import com.meistercharts.events.MouseMoveEvent
import com.meistercharts.events.MouseUpEvent
import com.meistercharts.events.MouseWheelEvent
import com.meistercharts.js.CanvasReadBackFrequency.Companion.readBackFrequency
import com.meistercharts.model.Coordinates
import com.meistercharts.model.Size
import convertCancel
import convertDown
import convertEnd
import convertEnter
import convertLeave
import convertMove
import convertOut
import convertOver
import convertPress
import convertRelease
import convertStart
import convertType
import convertUp
import extractModifierCombination
import it.neckar.logging.Logger
import it.neckar.logging.LoggerFactory
import it.neckar.logging.debug
import it.neckar.open.dispose.Disposable
import it.neckar.open.dispose.DisposeSupport
import it.neckar.open.kotlin.lang.abs
import it.neckar.open.observable.ObservableObject
import it.neckar.open.observable.ReadOnlyObservableObject
import it.neckar.open.time.nowMillis
import it.neckar.open.unit.other.px
import it.neckar.open.unit.si.ms
import it.neckar.open.unit.time.RelativeMillis
import kotlinx.browser.document
import kotlinx.browser.window
import noFocusBorder
import offset
import org.w3c.dom.AddEventListenerOptions
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.TouchEvent
import org.w3c.dom.events.Event
import org.w3c.dom.events.KeyboardEvent
import org.w3c.dom.events.MouseEvent
import org.w3c.dom.events.WheelEvent
import org.w3c.dom.pointerevents.PointerEvent
import timeStampAsDoubleWorkaround
import toCss
import unselectable

/**
 * Provides an HTML5 canvas
 */
class CanvasJS(type: CanvasType) : AbstractCanvas(type), Disposable {
  private val logger: Logger = LoggerFactory.getLogger("com.meistercharts.js.CanvasJS")

  /**
   * The html canvas element
   */
  val canvasElement: HTMLCanvasElement = (document.createElement("CANVAS") as HTMLCanvasElement).also {
    it.classList.add(MeisterChartClasses.canvas)
  }

  override val gc: CanvasRenderingContextJS = CanvasRenderingContextJS(this, type.readBackFrequency())

  /**
   * The size of the canvas in *logic* pixels (CSS size).
   *
   * Details related to the optimized rendering with support for high resolution displays and browser zoom is documented
   * in [CanvasRenderingContextJS].
   */
  override val sizeProperty: ReadOnlyObservableObject<Size> = ObservableObject(Size.zero).also {
    it.consumeChanges { oldSize, newSize ->
      require(newSize.bothNotNegative()) { "Invalid size: $newSize" }
      if (type == CanvasType.Main) {
        logger.debug { "Main Canvas size changed from $oldSize to $newSize" }
      }
    }
  }

  /**
   * The size of the canvas in *logic* pixels (CSS size)
   */
  override val size: Size by sizeProperty

  /**
   * The *physical* width of the canvas element
   */
  override val physicalWidth: Double
    get() {
      return canvasElement.width.toDouble()
    }

  /**
   * The *physical* height of the canvas element
   */
  override val physicalHeight: Double
    get() {
      return canvasElement.height.toDouble()
    }

  override val chartSizeClassificationProperty: ReadOnlyObservableObject<ChartSizeClassification> = sizeProperty.map {
    ChartSizeClassification.get(it)
  }

  override val chartSizeClassification: ChartSizeClassification by chartSizeClassificationProperty

  /**
   * The location of the [canvasElement] as returned by `getBoundingClientRect`
   */
  private var boundingClientLocation: Coordinates = Coordinates.none

  /**
   * Indicates whether the size of the [canvasElement] has changed
   */
  private var clientSizeHasChanged: Boolean = true

  /**
   * Indicates whether the document scroll-offset has changed
   */
  private var scrollOffsetHasChanged: Boolean = true

  private val disposeSupport: DisposeSupport = DisposeSupport()

  override fun dispose() {
    disposeSupport.dispose()
    // Set the size of the canvas-element to 0x0 to reduce memory consumption until
    // the safari browser has time to garbage collect the element (the safari browser
    // has a 256 MiB memory limit for the use of canvas-elements).
    canvasElement.width = 0
    canvasElement.height = 0
    canvasElement.style.width = "0px"
    canvasElement.style.height = "0px"
  }

  init {
    if (type == CanvasType.Main) {
      ResizeObserver { entries, _ ->
        if (entries.isNotEmpty()) {
          //Do not apply the new size directly to avoid flickering!
          clientSizeHasChanged = true
        }
      }.apply {
        observe(canvasElement)

        disposeSupport.onDispose {
          this.unobserve(canvasElement)
        }
      }

      val documentScrollListener: (Event) -> Unit = {
        scrollOffsetHasChanged = true
      }
      document.addEventListener("scroll", documentScrollListener)
      disposeSupport.onDispose {
        document.removeEventListener("scroll", documentScrollListener)
      }
    }

    // Set the width and height attributes of the HTMLCanvasElement. Depending on the size and the device pixel ratio
    sizeProperty.consumeImmediately {
      recalculateCanvasRenderingSize()
    }

    canvasElement.unselectable()
    canvasElement.noFocusBorder()

    if (type == CanvasType.Main) {
      mouseCursor.consumeImmediately {
        canvasElement.style.cursor = it.toCss()
      }

      //Both touch and mouse events are added
      setUpTouchEventListeners()
      setUpMouseEventListeners()

      //Keyboard events
      setUpKeyEventListeners()

      //Pointer events are *not* supported / necessary. Use touch events instead
      //setUpPointerEventListeners()
    }
  }

  /**
   * Updates the width/height of the *rendering* size of the canvas
   */
  internal fun recalculateCanvasRenderingSize() {
    // 'size' is in logical pixels. We want the canvas rendering context to provide the total amount of physical pixels.
    // Thus, we must set the width and height attributes (not(!) the CSS properties) of the Html canvas element to the physical
    // pixel values.
    // It holds: physical pixel value = logical pixel * device pixel ratio
    val devicePixelRatio = environment.devicePixelRatio
    @PhysicalPixel val targetRenderingWidth = devicePixelRatio * size.width
    @PhysicalPixel val targetRenderingHeight = devicePixelRatio * size.height

    // Set the width and height attributes to target rendering size
    canvasElement.width = targetRenderingWidth.toInt() //TODO cast correct????
    canvasElement.height = targetRenderingHeight.toInt() //TODO cast correct????

    if (type == CanvasType.Main) {
      logger.debug { "Updated Main canvas element width/height to ${canvasElement.width}/${canvasElement.height}" }
    }
  }

  /**
   * Sets the size of this [CanvasJS] to that of the underlying [HTMLCanvasElement]
   */
  fun applySizeFromClientSize() {
    if (!clientSizeHasChanged && !scrollOffsetHasChanged) {
      return
    }
    clientSizeHasChanged = false
    scrollOffsetHasChanged = false
    // getBoundingClientRect takes the scroll offset of the viewport into account.
    // Hence, we must retrieve it every time the size of the canvas-element or the
    // scroll-offset of the viewport has changed.
    canvasElement.getBoundingClientRect().let {
      logger.debug { "Updating size from BoundingClientRect: ${it.width}/${it.height}" }

      boundingClientLocation = Coordinates(it.left, it.top) // do not(!) use x/y because Edge and IE11 do not support them
      val newSize = Size(it.width, it.height)
      applySize(newSize, "bounding client rect changed to ${it.x}/${it.y} : ${it.width}/${it.height}}")
    }
  }

  /**
   * Applies the new size
   */
  fun applySize(newSize: Size, reason: String) {
    logger.debug { "Applying new size: $newSize (old: ${sizeProperty.value}) because: $reason" }
    (sizeProperty as ObservableObject<Size>).value = newSize
  }

  /**
   * Sets up the mouse event listeners
   */
  private fun setUpMouseEventListeners() {
    requireMainCanvas()

    // Note that we do not add event listeners for
    // contextmenu: The event occurs when the user right-clicks on an element to open a context menu -> not needed yet
    // mouseout: The event occurs when a user moves the mouse pointer out of an element, or out of one of its children -> listen for mouseleave instead
    // mouseover: The event occurs when the pointer is moved onto an element, or onto one of its children -> listen for mouseenter instead

    // The event occurs when the user clicks on an element
    canvasElement.addEventListener("click", { event ->
      canvasElement.focus()
      notifyMouseClick(event.unsafeCast<MouseEvent>())
    }, AddEventListenerOptions(passive = false))

    // The event occurs when the user presses a mouse button over an element
    canvasElement.addEventListener("mousedown", { event ->
      canvasElement.focus()
      notifyMouseDown(event.unsafeCast<MouseEvent>())
    }, AddEventListenerOptions(passive = false))

    // The event occurs when a user releases a mouse button over an element
    canvasElement.addEventListener("mouseup", { event ->
      notifyMouseUp(event.unsafeCast<MouseEvent>())
    }, AddEventListenerOptions(passive = false))

    // The event occurs when the user double-clicks on an element
    canvasElement.addEventListener("dblclick", { event ->
      notifyMouseDblClick(event.unsafeCast<MouseEvent>())
    }, AddEventListenerOptions(passive = false))

    // The event occurs when the pointer is moving while it is over an element
    canvasElement.addEventListener("mousemove", { event ->
      notifyMouseMove(event.unsafeCast<MouseEvent>())
    }, AddEventListenerOptions(passive = false))

    // The event occurs when the pointer is moved onto an element
    canvasElement.addEventListener("mouseenter", { event ->
      notifyMouseEnter(event.unsafeCast<MouseEvent>())
    }, AddEventListenerOptions(passive = false))

    // The event occurs when the pointer is moved out of an element
    canvasElement.addEventListener("mouseleave", { event ->
      notifyMouseLeave(event.unsafeCast<MouseEvent>())
    }, AddEventListenerOptions(passive = false))

    canvasElement.addEventListener("wheel", { event ->
      notifyWheel(event.unsafeCast<WheelEvent>())
    }, AddEventListenerOptions(passive = false))
  }

  private fun notifyWheel(event: WheelEvent) {
    //deltaX = horizontal scroll amount: https://developer.mozilla.org/en-US/docs/Web/API/WheelEvent/deltaX
    //deltaY = vertical scroll amount: https://developer.mozilla.org/en-US/docs/Web/API/WheelEvent/deltaY
    //Note that on Mac pressing SHIFT results in a switch of the scrolling axis. Hence pressing
    //SHIFT means that deltaX is being used instead of deltaY. The following code attempts to take
    //care of this fact.
    val deltaRaw = if (event.deltaY != 0.0) {
      event.deltaY
    } else {
      event.deltaX
    }
    //https://developer.mozilla.org/en-US/docs/Web/API/WheelEvent/deltaMode
    //DOM_DELTA_PIXEL 0x00 The delta values are specified in pixels.
    //DOM_DELTA_LINE  0x01 The delta values are specified in lines.
    //DOM_DELTA_PAGE  0x02 The delta values are specified in pages.
    @px val distance = when (event.deltaMode) {
      0    -> deltaRaw
      1    -> deltaRaw * 25.0 //we assume one line is 25 pixels high
      2    -> deltaRaw * 500.0 //we assume one page is 500 pixels high
      else -> throw IllegalArgumentException("Unsupported mode: ${event.deltaMode}")
    }

    val modifierCombination = event.extractModifierCombination()

    MouseWheelEvent(event.timeStampAsDoubleWorkaround, event.offset(), distance, modifierCombination).let {
      mouseEvents.notifyWheel(it)
        .cancelIfConsumed(event)
    }
  }

  private fun notifyMouseLeave(event: MouseEvent) {
    MouseMoveEvent(event.timeStampAsDoubleWorkaround, null, event.extractModifierCombination()).let {
      mouseEvents.notifyMove(it)
        .cancelIfConsumed(event)
    }
  }

  private fun notifyMouseEnter(event: MouseEvent) {
    MouseMoveEvent(event.timeStampAsDoubleWorkaround, event.offset(), event.extractModifierCombination()).let {
      mouseEvents.notifyMove(it)
        .cancelIfConsumed(event)
    }
  }

  private fun notifyMouseMove(event: MouseEvent) {
    val isPrimaryButton = event.buttons.toInt() and 1 == 1
    if (isPrimaryButton) {
      // treat as drag event
      MouseDragEvent(event.timeStampAsDoubleWorkaround, event.offset(), event.extractModifierCombination()).let {
        mouseEvents.notifyDrag(it)
          .cancelIfConsumed(event)
      }
    } else {
      // treat as "normal" mouse move event
      MouseMoveEvent(event.timeStampAsDoubleWorkaround, event.offset(), event.extractModifierCombination()).let {
        mouseEvents.notifyMove(it)
          .cancelIfConsumed(event)
      }
    }
  }

  private fun notifyMouseDblClick(event: MouseEvent) {
    MouseDoubleClickEvent(event.timeStampAsDoubleWorkaround, event.offset(), event.extractModifierCombination()).let {
      mouseEvents.notifyDoubleClick(it)
        .cancelIfConsumed(event)
    }
  }

  private fun notifyMouseClick(event: MouseEvent) {
    MouseClickEvent(event.timeStampAsDoubleWorkaround, event.offset(), event.extractModifierCombination()).let {
      mouseEvents.notifyClick(it)
        .cancelIfConsumed(event)
    }
  }

  private fun notifyMouseDown(event: MouseEvent) {
    MouseDownEvent(event.timeStampAsDoubleWorkaround, event.offset(), event.extractModifierCombination()).let {
      mouseEvents.notifyDown(it)
        .cancelIfConsumed(event)
    }
  }

  private fun notifyMouseUp(event: MouseEvent) {
    MouseUpEvent(event.timeStampAsDoubleWorkaround, event.offset(), event.extractModifierCombination()).let {
      mouseEvents.notifyUp(it)
        .cancelIfConsumed(event)
    }
  }

  @Deprecated("Use Touch and Mouse events instead")
  fun setUpPointerEventListeners() {
    canvasElement.addEventListener("pointerover", { event ->
      canvasElement.focus()
      event.unsafeCast<PointerEvent>().convertOver().let {
        pointerEvents.notifyOver(it)
          .cancelIfConsumed(event)
      }
    }, AddEventListenerOptions(passive = false))

    canvasElement.addEventListener("pointerenter", { event ->
      canvasElement.focus()
      event.unsafeCast<PointerEvent>().convertEnter().let {
        pointerEvents.notifyEnter(it)
          .cancelIfConsumed(event)
      }
    }, AddEventListenerOptions(passive = false))

    canvasElement.addEventListener("pointerdown", { event ->
      canvasElement.focus()
      event.unsafeCast<PointerEvent>().convertDown().let {
        pointerEvents.notifyDown(it)
          .cancelIfConsumed(event)
      }
    }, AddEventListenerOptions(passive = false))

    canvasElement.addEventListener("pointermove", { event ->
      event.unsafeCast<PointerEvent>().convertMove().let {
        pointerEvents.notifyMove(it)
          .cancelIfConsumed(event)
      }
    }, AddEventListenerOptions(passive = false))

    canvasElement.addEventListener("pointerup", { event ->
      event.unsafeCast<PointerEvent>().convertUp().let {
        pointerEvents.notifyUp(it)
          .cancelIfConsumed(event)
      }
    }, AddEventListenerOptions(passive = false))

    canvasElement.addEventListener("pointercancel", { event ->
      event.unsafeCast<PointerEvent>().convertCancel().let {
        pointerEvents.notifyCancel(it)
          .cancelIfConsumed(event)
      }
    }, AddEventListenerOptions(passive = false))

    canvasElement.addEventListener("pointerout", { event ->
      event.unsafeCast<PointerEvent>().convertOut().let {
        pointerEvents.notifyOut(it)
          .cancelIfConsumed(event)
      }
    }, AddEventListenerOptions(passive = false))

    canvasElement.addEventListener("pointerleave", { event ->
      event.unsafeCast<PointerEvent>().convertLeave().let {
        pointerEvents.notifyLeave(it)
          .cancelIfConsumed(event)
      }
    }, AddEventListenerOptions(passive = false))

  }

  private fun setUpKeyEventListeners() {
    requireMainCanvas()

    //Set the tab index to 1 to allow key presses to arrive
    canvasElement.tabIndex = 1

    canvasElement.addEventListener("keydown", { event ->
      event.unsafeCast<KeyboardEvent>().convertPress().let {
        keyEvents.notifyDown(it)
          .cancelIfConsumed(event)
      }
    }, AddEventListenerOptions(passive = false))

    canvasElement.addEventListener("keyup", { event ->
      event.unsafeCast<KeyboardEvent>().convertRelease().let {
        keyEvents.notifyUp(it)
          .cancelIfConsumed(event)
      }
    }, AddEventListenerOptions(passive = false))

    canvasElement.addEventListener("keypress", { event ->
      event.unsafeCast<KeyboardEvent>().convertType().let {
        keyEvents.notifyTyped(it)
          .cancelIfConsumed(event)
      }
    }, AddEventListenerOptions(passive = false))
  }

  /**
   * Registers the touch event listeners
   */
  private fun setUpTouchEventListeners() {
    canvasElement.addEventListener("touchstart", { event ->
      canvasElement.focus()

      if (event.cancelable.not()) {
        //Skip events that are *not* cancelable, these events are used by the browser (e.g. for scrolling)
        return@addEventListener
      }

      event.unsafeCast<TouchEvent>().convertStart(boundingClientLocation).let {
        touchEvents.notifyOnStart(it)
          .cancelIfConsumed(event)
      }
    }, AddEventListenerOptions(passive = false))

    canvasElement.addEventListener("touchend", { event ->
      if (event.cancelable.not()) {
        //Skip events that are *not* cancelable, these events are used by the browser (e.g. for scrolling)
        return@addEventListener
      }

      event.unsafeCast<TouchEvent>().convertEnd(boundingClientLocation).let {
        touchEvents.notifyOnEnd(it)
          .cancelIfConsumed(event)

        //Always consume the event to avoid browser handling of default actions
        event.stopImmediatePropagation()
        event.preventDefault()
      }
    }, AddEventListenerOptions(passive = false))

    canvasElement.addEventListener("touchmove", { event ->
      if (event.cancelable.not()) {
        //Skip events that are *not* cancelable, these events are used by the browser (e.g. for scrolling)
        return@addEventListener
      }

      event.unsafeCast<TouchEvent>().convertMove(boundingClientLocation).let {
        touchEvents.notifyOnMove(it)
          .cancelIfConsumed(event)
      }
    }, AddEventListenerOptions(passive = false))

    canvasElement.addEventListener("touchcancel", { event ->
      event.unsafeCast<TouchEvent>().convertCancel(boundingClientLocation).let {
        val eventConsumption = touchEvents.notifyOnCancel(it)

        //Special handling for touch cancel events.
        //The cancel event is *always* propagated. But only consumed if possible
        if (event.cancelable) {
          eventConsumption.cancelIfConsumed(event)
        }
      }
    }, AddEventListenerOptions(passive = false))
  }

  override fun takeSnapshot(): Image {
    requireOffScreenCanvas()

    return Image(canvasElement, size)
  }

  /**
   * Takes a snapshot using the physical size
   */
  fun takeSnapshotPhysicalSize(): Image {
    requireOffScreenCanvas()

    return Image(canvasElement, Size(physicalWidth, physicalHeight))
  }
}

/**
 * Cancels the event if this == [EventConsumption.Consumed]
 */
private fun EventConsumption.cancelIfConsumed(event: Event) {
  if (this.consumed) {
    event.stopImmediatePropagation()
    event.preventDefault()
  }
}

/**
 * Starts an animation that paints the canvas for every frame if necessary and calls [HtmlCanvas#applySizeFromClientWidth] on every frame
 */
fun ChartSupport.scheduleRepaints(@ms @RelativeMillis frameTimestamp: Double) {
  //Check if the chart support has already been disposed
  if (disposed) {
    return
  }

  //'frameTimestamp' represents a DOMHighResTimeStamp that states the time passed since the beginning of the current
  //document's lifetime (see https://developer.mozilla.org/en-US/docs/Web/API/window/requestAnimationFrame and
  //https://developer.mozilla.org/en-US/docs/Web/API/DOMHighResTimeStamp#The_time_origin).
  //This implies that 'frameTimestamp' cannot be used as an absolute timestamp.
  //
  //'nowMillis()' on the other hand is not as precise as a DOMHighResTimeStamp. Furthermore, we cannot use 'nowMillis()'
  //as a frame-timestamp without losing the correct delta of two consecutive frames rendered by the browser.
  //
  //Solution: adjust 'frameTimestamp' only if differs more than 'deltaRelativeMillisToAbsoluteThreshold' from 'nowMillis()'

  val now = nowMillis()
  //We use the stored delta to calculate the best absolute timestamp
  var exactAbsoluteTimestamp = frameTimestamp + deltaRelativeMillisToAbsolute
  if ((exactAbsoluteTimestamp - now).abs() > deltaRelativeMillisToAbsoluteThreshold) {
    deltaRelativeMillisToAbsolute = now - frameTimestamp
    exactAbsoluteTimestamp = frameTimestamp + deltaRelativeMillisToAbsolute
  }

  //Trigger size update. Do this during a refresh to avoid flickering.
  (canvas as CanvasJS).applySizeFromClientSize()

  refresh(exactAbsoluteTimestamp)

  window.requestAnimationFrame { scheduleRepaints(it) }
}

/**
 * Stores the delta between the relative millis to absolute millis
 */
private var deltaRelativeMillisToAbsolute: Double = 0.0

/**
 * The greatest delta between the relative millis and the absolute millis that is still acceptable
 */
private const val deltaRelativeMillisToAbsoluteThreshold: @ms Double = 10.0 // empirically evaluated; do not go below 10 milliseconds to avoid too many adjustments

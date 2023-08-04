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
package com.meistercharts.algorithms.layers.debug

import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.text.TextPainter
import com.meistercharts.color.Color
import com.meistercharts.canvas.ChartSupport
import com.meistercharts.canvas.DirtyReason
import com.meistercharts.font.FontDescriptorFragment
import com.meistercharts.canvas.text.LineSpacing
import com.meistercharts.canvas.events.CanvasKeyEventHandler
import com.meistercharts.canvas.events.CanvasMouseEventHandler
import com.meistercharts.canvas.events.CanvasPointerEventHandler
import com.meistercharts.canvas.events.CanvasTouchEventHandler
import it.neckar.geometry.Direction
import it.neckar.geometry.HorizontalAlignment
import it.neckar.open.collections.EvictingQueue
import com.meistercharts.events.EventConsumption
import it.neckar.events.KeyDownEvent
import it.neckar.events.KeyEvent
import it.neckar.events.KeyTypeEvent
import it.neckar.events.KeyUpEvent
import it.neckar.events.MouseClickEvent
import it.neckar.events.MouseDoubleClickEvent
import it.neckar.events.MouseDownEvent
import it.neckar.events.MouseDragEvent
import it.neckar.events.MouseEvent
import it.neckar.events.MouseMoveEvent
import it.neckar.events.MouseUpEvent
import it.neckar.events.MouseWheelEvent
import it.neckar.events.PointerCancelEvent
import it.neckar.events.PointerDownEvent
import it.neckar.events.PointerEnterEvent
import it.neckar.events.PointerEvent
import it.neckar.events.PointerLeaveEvent
import it.neckar.events.PointerMoveEvent
import it.neckar.events.PointerOutEvent
import it.neckar.events.PointerOverEvent
import it.neckar.events.PointerUpEvent
import it.neckar.events.TouchCancelEvent
import it.neckar.events.TouchEndEvent
import it.neckar.events.TouchEvent
import it.neckar.events.TouchMoveEvent
import it.neckar.events.TouchStartEvent
import it.neckar.open.formatting.formatAsInt
import com.meistercharts.style.BoxStyle

/**
 * Paints debug information about events
 */
class EventsDebugLayer(
  val data: Data = Data()
) : AbstractLayer() {
  override val type: LayerType = LayerType.Notification
  override val description: String = "Paints debug information about events"

  private val painter = TextPainter()

  private val eventDescriptionQueue: EvictingQueue<String> = EvictingQueue(15)

  /**
   * Removes all processed events
   */
  fun clearEventQueue() {
    eventDescriptionQueue.clear()
  }

  override val mouseEventHandler: CanvasMouseEventHandler = object : CanvasMouseEventHandler {

    private fun processMouseEvent(event: MouseEvent, chartSupport: ChartSupport): EventConsumption {
      if (data.mouseEventFilter(event)) {
        chartSupport.markAsDirty(DirtyReason.UserInteraction)
        eventDescriptionQueue.add("${event.timestamp.formatAsInt()} $event ${event.modifierCombination.description()}")
      }
      return EventConsumption.Ignored
    }

    override fun onDown(event: MouseDownEvent, chartSupport: ChartSupport): EventConsumption {
      return processMouseEvent(event, chartSupport)
    }

    override fun onUp(event: MouseUpEvent, chartSupport: ChartSupport): EventConsumption {
      return processMouseEvent(event, chartSupport)
    }

    override fun onDrag(event: MouseDragEvent, chartSupport: ChartSupport): EventConsumption {
      return processMouseEvent(event, chartSupport)
    }

    override fun onClick(event: MouseClickEvent, chartSupport: ChartSupport): EventConsumption {
      return processMouseEvent(event, chartSupport)
    }

    override fun onDoubleClick(event: MouseDoubleClickEvent, chartSupport: ChartSupport): EventConsumption {
      return processMouseEvent(event, chartSupport)
    }

    override fun onMove(event: MouseMoveEvent, chartSupport: ChartSupport): EventConsumption {
      return processMouseEvent(event, chartSupport)
    }

    override fun onWheel(event: MouseWheelEvent, chartSupport: ChartSupport): EventConsumption {
      return processMouseEvent(event, chartSupport)
    }
  }

  override val keyEventHandler: CanvasKeyEventHandler = object : CanvasKeyEventHandler {

    private fun processKeyEvent(event: KeyEvent, chartSupport: ChartSupport): EventConsumption {
      if (data.keyEventFilter(event)) {
        chartSupport.markAsDirty(DirtyReason.UserInteraction)
        eventDescriptionQueue.add("${event.timestamp.formatAsInt()} $event")
      }
      return EventConsumption.Ignored
    }

    override fun onDown(event: KeyDownEvent, chartSupport: ChartSupport): EventConsumption {
      return processKeyEvent(event, chartSupport)
    }

    override fun onUp(event: KeyUpEvent, chartSupport: ChartSupport): EventConsumption {
      return processKeyEvent(event, chartSupport)
    }

    override fun onType(event: KeyTypeEvent, chartSupport: ChartSupport): EventConsumption {
      return processKeyEvent(event, chartSupport)
    }
  }

  override val pointerEventHandler: CanvasPointerEventHandler = object : CanvasPointerEventHandler {

    private fun processPointerEvent(event: PointerEvent, chartSupport: ChartSupport): EventConsumption {
      if (data.pointerEventFilter(event)) {
        chartSupport.markAsDirty(DirtyReason.UserInteraction)
        eventDescriptionQueue.add("${event.timestamp.formatAsInt()} $event ${event.modifierCombination.description()}")
      }
      return EventConsumption.Ignored
    }

    override fun onOver(event: PointerOverEvent, chartSupport: ChartSupport): EventConsumption {
      return processPointerEvent(event, chartSupport)
    }

    override fun onEnter(event: PointerEnterEvent, chartSupport: ChartSupport): EventConsumption {
      return processPointerEvent(event, chartSupport)
    }

    override fun onDown(event: PointerDownEvent, chartSupport: ChartSupport): EventConsumption {
      return processPointerEvent(event, chartSupport)
    }

    override fun onMove(event: PointerMoveEvent, chartSupport: ChartSupport): EventConsumption {
      return processPointerEvent(event, chartSupport)
    }

    override fun onUp(event: PointerUpEvent, chartSupport: ChartSupport): EventConsumption {
      return processPointerEvent(event, chartSupport)
    }

    override fun onCancel(event: PointerCancelEvent, chartSupport: ChartSupport): EventConsumption {
      return processPointerEvent(event, chartSupport)
    }

    override fun onOut(event: PointerOutEvent, chartSupport: ChartSupport): EventConsumption {
      return processPointerEvent(event, chartSupport)
    }

    override fun onLeave(event: PointerLeaveEvent, chartSupport: ChartSupport): EventConsumption {
      return processPointerEvent(event, chartSupport)
    }
  }

  override val touchEventHandler: CanvasTouchEventHandler = object : CanvasTouchEventHandler {

    private fun processTouchEvent(event: TouchEvent, chartSupport: ChartSupport): EventConsumption {
      if (data.touchEventFilter(event)) {
        chartSupport.markAsDirty(DirtyReason.UserInteraction)
        eventDescriptionQueue.add("${event.timestamp.formatAsInt()} $event ${event.modifierCombination.description()}")
      }
      return EventConsumption.Ignored
    }

    override fun onStart(event: TouchStartEvent, chartSupport: ChartSupport): EventConsumption {
      return processTouchEvent(event, chartSupport)
    }

    override fun onEnd(event: TouchEndEvent, chartSupport: ChartSupport): EventConsumption {
      return processTouchEvent(event, chartSupport)
    }

    override fun onMove(event: TouchMoveEvent, chartSupport: ChartSupport): EventConsumption {
      return processTouchEvent(event, chartSupport)
    }

    override fun onCancel(event: TouchCancelEvent, chartSupport: ChartSupport): EventConsumption {
      return processTouchEvent(event, chartSupport)
    }
  }

  override fun paint(paintingContext: LayerPaintingContext) {
    val eventDescriptionList = eventDescriptionQueue.toList()
    if (eventDescriptionList.isEmpty()) {
      return
    }
    val gc = paintingContext.gc
    gc.font(FontDescriptorFragment.S)
    painter.paintText(
      gc,
      eventDescriptionList,
      Color.black,
      BoxStyle.none,
      LineSpacing.Single,
      HorizontalAlignment.Left,
      Direction.TopLeft,
      10.0,
      10.0,
    )
  }

  class Data {
    /**
     * Determines which mouse events should be processed (true) or not (false).
     *
     * @see MouseEventFilter
     */
    var mouseEventFilter: (MouseEvent) -> Boolean = MouseEventFilter()::filter

    /**
     * Determines which key events should be processed (true) or not (false).
     *
     * @see KeyEventFilter
     */
    var keyEventFilter: (KeyEvent) -> Boolean = KeyEventFilter()::filter

    /**
     * Determines which pointer events should be processed (true) or not (false).
     *
     * @see PointerEventFilter
     */
    var pointerEventFilter: (PointerEvent) -> Boolean = PointerEventFilter()::filter

    /**
     * Determines which touch events should be processed (true) or not (false).
     *
     * @see TouchEventFilter
     */
    var touchEventFilter: (TouchEvent) -> Boolean = TouchEventFilter()::filter
  }
}

/**
 * A filter for [MouseEvent]s
 *
 * Filters all but [MouseMoveEvent]s by default.
 */
class MouseEventFilter {
  /** Whether [MouseMoveEvent]s should be processed (true) or not (false) */
  var filterMouseMove: Boolean = false

  /** Whether [MouseDragEvent]s should be processed (true) or not (false) */
  var filterMouseDrag: Boolean = true

  /** Whether [MouseClickEvent]s should be processed (true) or not (false) */
  var filterMouseClick: Boolean = true

  /** Whether [MouseDownEvent]s should be processed (true) or not (false) */
  var filterMouseDown: Boolean = true

  /** Whether [MouseUpEvent]s should be processed (true) or not (false) */
  var filterMouseUp: Boolean = true

  /** Whether [MouseDoubleClickEvent]s should be processed (true) or not (false) */
  var filterMouseDoubleClick: Boolean = true

  /** Whether [MouseWheelEvent]s should be processed (true) or not (false) */
  var filterMouseWheel: Boolean = true

  /**
   * @return whether [event] should be processed (true) or not (false).
   */
  fun filter(event: MouseEvent): Boolean {
    return when (event) {
      is MouseMoveEvent -> filterMouseMove
      is MouseDragEvent -> filterMouseDrag
      is MouseClickEvent -> filterMouseClick
      is MouseDownEvent -> filterMouseDown
      is MouseUpEvent -> filterMouseUp
      is MouseDoubleClickEvent -> filterMouseDoubleClick
      is MouseWheelEvent -> filterMouseWheel
    }
  }
}

/**
 * A filter for [KeyEvent]s
 *
 * Filters all [KeyEvent]s by default.
 */
class KeyEventFilter {
  /** Whether [KeyUpEvent]s should be processed (true) or not (false) */
  var filterKeyUp: Boolean = true

  /** Whether [KeyDownEvent]s should be processed (true) or not (false) */
  var filterKeyDown: Boolean = true

  /** Whether [KeyTypeEvent]s should be processed (true) or not (false) */
  var filterKeyType: Boolean = true

  fun filter(event: KeyEvent): Boolean {
    return when (event) {
      is KeyTypeEvent -> filterKeyType
      is KeyDownEvent -> filterKeyDown
      is KeyUpEvent -> filterKeyUp
    }
  }
}

/**
 * A filter for [PointerEvent]s
 *
 * Filters all but [PointerMoveEvent]s by default.
 */
class PointerEventFilter {
  /** Whether [PointerLeaveEvent]s should be processed (true) or not (false) */
  var filterPointerLeave: Boolean = true

  /** Whether [PointerOutEvent]s should be processed (true) or not (false) */
  var filterPointerOut: Boolean = true

  /** Whether [PointerCancelEvent]s should be processed (true) or not (false) */
  var filterPointerCancel: Boolean = true

  /** Whether [PointerUpEvent]s should be processed (true) or not (false) */
  var filterPointerUp: Boolean = true

  /** Whether [PointerMoveEvent]s should be processed (true) or not (false) */
  var filterPointerMove: Boolean = false

  /** Whether [PointerDownEvent]s should be processed (true) or not (false) */
  var filterPointerDown: Boolean = true

  /** Whether [PointerEnterEvent]s should be processed (true) or not (false) */
  var filterPointerEnter: Boolean = true

  /** Whether [PointerOverEvent]s should be processed (true) or not (false) */
  var filterPointerOver: Boolean = true

  fun filter(event: PointerEvent): Boolean {
    return when (event) {
      is PointerOverEvent -> filterPointerOver
      is PointerEnterEvent -> filterPointerEnter
      is PointerDownEvent -> filterPointerDown
      is PointerMoveEvent -> filterPointerMove
      is PointerUpEvent -> filterPointerUp
      is PointerCancelEvent -> filterPointerCancel
      is PointerOutEvent -> filterPointerOut
      is PointerLeaveEvent -> filterPointerLeave
    }
  }
}

/**
 * A filter for [TouchEvent]s
 *
 * Filters all but [TouchMoveEvent]s by default.
 */
class TouchEventFilter {
  /** Whether [TouchCancelEvent]s should be processed (true) or not (false) */
  var filterTouchCancel: Boolean = true

  /** Whether [TouchMoveEvent]s should be processed (true) or not (false) */
  var filterTouchMove: Boolean = false

  /** Whether [TouchEndEvent]s should be processed (true) or not (false) */
  var filterTouchEnd: Boolean = true

  /** Whether [TouchStartEvent]s should be processed (true) or not (false) */
  var filterTouchStart: Boolean = true

  fun filter(event: TouchEvent): Boolean {
    return when (event) {
      is TouchStartEvent -> filterTouchStart
      is TouchEndEvent -> filterTouchEnd
      is TouchMoveEvent -> filterTouchMove
      is TouchCancelEvent -> filterTouchCancel
    }
  }
}

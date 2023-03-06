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
package com.meistercharts.canvas.events

import com.meistercharts.canvas.ChartSupport
import it.neckar.open.kotlin.lang.consumeUntil
import com.meistercharts.events.EventConsumption
import com.meistercharts.events.MouseClickEvent
import com.meistercharts.events.MouseDoubleClickEvent
import com.meistercharts.events.MouseDownEvent
import com.meistercharts.events.MouseDragEvent
import com.meistercharts.events.MouseEventHandler
import com.meistercharts.events.MouseMoveEvent
import com.meistercharts.events.MouseUpEvent
import com.meistercharts.events.MouseWheelEvent

/**
 * Event handler for mouse events from a canvas
 */
interface CanvasMouseEventHandler {
  /*
  * Called when the canvas received a mouse-click event
  *
  * Attention: Clicks also happen when the mouse has been moved between down and up (drag).
  * Therefore, it is necessary to check if down and up happened on the same element.
  */
  @Deprecated("In most cases onDown and onUp should be used instead")
  fun onClick(event: MouseClickEvent, chartSupport: ChartSupport): EventConsumption {
    return EventConsumption.Ignored
  }

  /**
   * Is notified about mouse press events
   */
  fun onDown(event: MouseDownEvent, chartSupport: ChartSupport): EventConsumption {
    return EventConsumption.Ignored
  }

  /**
   * Is notified about mouse release events
   */
  fun onUp(event: MouseUpEvent, chartSupport: ChartSupport): EventConsumption {
    return EventConsumption.Ignored
  }

  /**
   * Called when the canvas received a mouse-double-click event
   */
  fun onDoubleClick(event: MouseDoubleClickEvent, chartSupport: ChartSupport): EventConsumption {
    return EventConsumption.Ignored
  }

  /**
   * Called when the canvas received a mouse-move event
   */
  fun onMove(event: MouseMoveEvent, chartSupport: ChartSupport): EventConsumption {
    return EventConsumption.Ignored
  }

  /**
   * Called when the canvas received a mouse-move event
   */
  fun onDrag(event: MouseDragEvent, chartSupport: ChartSupport): EventConsumption {
    return EventConsumption.Ignored
  }

  /**
   * Called when the canvas received a mouse wheel event
   */
  fun onWheel(event: MouseWheelEvent, chartSupport: ChartSupport): EventConsumption {
    return EventConsumption.Ignored
  }
}


/**
 * Converts a default mouse event handler to a canvas mouse event handler.
 * This implementation simply ignores the chart support parameter
 */
@Deprecated("Probably no longer required")
fun MouseEventHandler.toCanvasMouseEventHandler(): CanvasMouseEventHandler {
  return object : CanvasMouseEventHandler {
    override fun onClick(event: MouseClickEvent, chartSupport: ChartSupport): EventConsumption {
      return onClick(event)
    }

    override fun onDown(event: MouseDownEvent, chartSupport: ChartSupport): EventConsumption {
      return onDown(event)
    }

    override fun onUp(event: MouseUpEvent, chartSupport: ChartSupport): EventConsumption {
      return onUp(event)
    }

    override fun onDoubleClick(event: MouseDoubleClickEvent, chartSupport: ChartSupport): EventConsumption {
      return onDoubleClick(event)
    }

    override fun onMove(event: MouseMoveEvent, chartSupport: ChartSupport): EventConsumption {
      return onMove(event)
    }

    override fun onDrag(event: MouseDragEvent, chartSupport: ChartSupport): EventConsumption {
      return onDrag(event)
    }

    override fun onWheel(event: MouseWheelEvent, chartSupport: ChartSupport): EventConsumption {
      return onWheel(event)
    }
  }
}

/**
 * Delegates the events to multiple delegates
 */
class CanvasMouseEventHandlerBroker() : CanvasMouseEventHandler {
  private val delegates = mutableListOf<CanvasMouseEventHandler>()

  fun delegate(eventHandler: CanvasMouseEventHandler) {
    delegates.add(eventHandler)
  }

  override fun onClick(event: MouseClickEvent, chartSupport: ChartSupport): EventConsumption {
    super.onClick(event, chartSupport)

    return delegates.consumeUntil(EventConsumption.Consumed) {
      it.onClick(event, chartSupport)
    } ?: EventConsumption.Ignored
  }

  override fun onDown(event: MouseDownEvent, chartSupport: ChartSupport): EventConsumption {
    super.onDown(event, chartSupport)

    return delegates.consumeUntil(EventConsumption.Consumed) {
      it.onDown(event, chartSupport)
    } ?: EventConsumption.Ignored
  }

  override fun onUp(event: MouseUpEvent, chartSupport: ChartSupport): EventConsumption {
    super.onUp(event, chartSupport)

    return delegates.consumeUntil(EventConsumption.Consumed) {
      it.onUp(event, chartSupport)
    } ?: EventConsumption.Ignored
  }

  override fun onDoubleClick(event: MouseDoubleClickEvent, chartSupport: ChartSupport): EventConsumption {
    super.onDoubleClick(event, chartSupport)

    return delegates.consumeUntil(EventConsumption.Consumed) {
      it.onDoubleClick(event, chartSupport)
    } ?: EventConsumption.Ignored
  }

  override fun onMove(event: MouseMoveEvent, chartSupport: ChartSupport): EventConsumption {
    super.onMove(event, chartSupport)

    return delegates.consumeUntil(EventConsumption.Consumed) {
      it.onMove(event, chartSupport)
    } ?: EventConsumption.Ignored
  }

  override fun onDrag(event: MouseDragEvent, chartSupport: ChartSupport): EventConsumption {
    super.onDrag(event, chartSupport)

    return delegates.consumeUntil(EventConsumption.Consumed) {
      it.onDrag(event, chartSupport)
    } ?: EventConsumption.Ignored
  }

  override fun onWheel(event: MouseWheelEvent, chartSupport: ChartSupport): EventConsumption {
    super.onWheel(event, chartSupport)

    return delegates.consumeUntil(EventConsumption.Consumed) {
      it.onWheel(event, chartSupport)
    } ?: EventConsumption.Ignored
  }
}

/**
 * Converts default mouse events to canvas mouse events
 */
class MouseEvent2CanvasHandler(
  /**
   * The chart support that is supplied
   */
  val chartSupport: ChartSupport,
  /**
   * The delegate that is informed about the events
   */
  val delegate: CanvasMouseEventHandler
) : MouseEventHandler {
  override fun onClick(event: MouseClickEvent): EventConsumption {
    return delegate.onClick(event, chartSupport)
  }

  override fun onDoubleClick(event: MouseDoubleClickEvent): EventConsumption {
    return delegate.onDoubleClick(event, chartSupport)
  }

  override fun onMove(event: MouseMoveEvent): EventConsumption {
    return delegate.onMove(event, chartSupport)
  }

  override fun onDrag(event: MouseDragEvent): EventConsumption {
    return delegate.onDrag(event, chartSupport)
  }

  override fun onWheel(event: MouseWheelEvent): EventConsumption {
    return delegate.onWheel(event, chartSupport)
  }

  override fun onDown(event: MouseDownEvent): EventConsumption {
    return delegate.onDown(event, chartSupport)
  }

  override fun onUp(event: MouseUpEvent): EventConsumption {
    return delegate.onUp(event, chartSupport)
  }
}

/**
 * Converts a canvas mouse event handler to a mouse event handler by providing the given chart support for all events
 */
fun CanvasMouseEventHandler.asMouseEventHandler(chartSupport: ChartSupport): MouseEventHandler {
  return MouseEvent2CanvasHandler(
    chartSupport,
    this
  )
}

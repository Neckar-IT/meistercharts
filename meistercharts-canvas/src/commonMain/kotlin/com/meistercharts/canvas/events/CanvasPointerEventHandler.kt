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
import com.meistercharts.events.EventConsumption.Consumed
import com.meistercharts.events.EventConsumption.Ignored
import it.neckar.events.PointerCancelEvent
import it.neckar.events.PointerDownEvent
import it.neckar.events.PointerEnterEvent
import com.meistercharts.events.PointerEventHandler
import it.neckar.events.PointerLeaveEvent
import it.neckar.events.PointerMoveEvent
import it.neckar.events.PointerOutEvent
import it.neckar.events.PointerOverEvent
import it.neckar.events.PointerUpEvent

/**
 * Event handler for pointer events from a canvas
 */
interface CanvasPointerEventHandler {
  /**
   * Is notified when a [PointerOverEvent] is fired
   */
  fun onOver(event: PointerOverEvent, chartSupport: ChartSupport): EventConsumption {
    return EventConsumption.Ignored
  }

  /**
   * Is notified when a [PointerEnterEvent] is fired
   */
  fun onEnter(event: PointerEnterEvent, chartSupport: ChartSupport): EventConsumption {
    return EventConsumption.Ignored
  }

  /**
   * Is notified when a [PointerDownEvent] is fired
   */
  fun onDown(event: PointerDownEvent, chartSupport: ChartSupport): EventConsumption {
    return EventConsumption.Ignored
  }

  /**
   * Is notified when a [PointerMoveEvent] is fired
   */
  fun onMove(event: PointerMoveEvent, chartSupport: ChartSupport): EventConsumption {
    return EventConsumption.Ignored
  }

  /**
   * Is notified when a [PointerUpEvent] is fired
   */
  fun onUp(event: PointerUpEvent, chartSupport: ChartSupport): EventConsumption {
    return EventConsumption.Ignored
  }

  /**
   * Is notified when a [PointerCancelEvent] is fired
   */
  fun onCancel(event: PointerCancelEvent, chartSupport: ChartSupport): EventConsumption {
    return EventConsumption.Ignored
  }

  /**
   * Is notified when a [PointerOutEvent] is fired
   */
  fun onOut(event: PointerOutEvent, chartSupport: ChartSupport): EventConsumption {
    return EventConsumption.Ignored
  }

  /**
   * Is notified when a [PointerLeaveEvent] is fired
   */
  fun onLeave(event: PointerLeaveEvent, chartSupport: ChartSupport): EventConsumption {
    return EventConsumption.Ignored
  }

}

@Deprecated("Probably no longer required")
fun PointerEventHandler.toCanvasPointerEventHandler(): CanvasPointerEventHandler {
  return object : CanvasPointerEventHandler {
    override fun onOver(event: PointerOverEvent, chartSupport: ChartSupport): EventConsumption {
      return onOver(event)
    }

    override fun onEnter(event: PointerEnterEvent, chartSupport: ChartSupport): EventConsumption {
      return onEnter(event)
    }

    override fun onDown(event: PointerDownEvent, chartSupport: ChartSupport): EventConsumption {
      return onDown(event)
    }

    override fun onMove(event: PointerMoveEvent, chartSupport: ChartSupport): EventConsumption {
      return onMove(event)
    }

    override fun onUp(event: PointerUpEvent, chartSupport: ChartSupport): EventConsumption {
      return onUp(event)
    }

    override fun onCancel(event: PointerCancelEvent, chartSupport: ChartSupport): EventConsumption {
      return onCancel(event)
    }

    override fun onOut(event: PointerOutEvent, chartSupport: ChartSupport): EventConsumption {
      return onOut(event)
    }

    override fun onLeave(event: PointerLeaveEvent, chartSupport: ChartSupport): EventConsumption {
      return onLeave(event)
    }

  }
}

class CanvasPointerEventHandlerBroker : CanvasPointerEventHandler {
  private val delegates = mutableListOf<CanvasPointerEventHandler>()

  fun delegate(eventHandler: CanvasPointerEventHandler) {
    delegates.add(eventHandler)
  }

  override fun onOver(event: PointerOverEvent, chartSupport: ChartSupport): EventConsumption {
    return delegates.consumeUntil(Consumed) {
      it.onOver(event, chartSupport)
    } ?: Ignored
  }

  override fun onEnter(event: PointerEnterEvent, chartSupport: ChartSupport): EventConsumption {
    return delegates.consumeUntil(Consumed) {
      it.onEnter(event, chartSupport)
    } ?: Ignored
  }

  override fun onDown(event: PointerDownEvent, chartSupport: ChartSupport): EventConsumption {
    return delegates.consumeUntil(Consumed) {
      it.onDown(event, chartSupport)
    } ?: Ignored
  }

  override fun onMove(event: PointerMoveEvent, chartSupport: ChartSupport): EventConsumption {
    return delegates.consumeUntil(Consumed) {
      it.onMove(event, chartSupport)
    } ?: Ignored
  }

  override fun onUp(event: PointerUpEvent, chartSupport: ChartSupport): EventConsumption {
    return delegates.consumeUntil(Consumed) {
      it.onUp(event, chartSupport)
    } ?: Ignored
  }

  override fun onCancel(event: PointerCancelEvent, chartSupport: ChartSupport): EventConsumption {
    return delegates.consumeUntil(Consumed) {
      it.onCancel(event, chartSupport)
    } ?: Ignored
  }

  override fun onOut(event: PointerOutEvent, chartSupport: ChartSupport): EventConsumption {
    return delegates.consumeUntil(Consumed) {
      it.onOut(event, chartSupport)
    } ?: Ignored
  }

  override fun onLeave(event: PointerLeaveEvent, chartSupport: ChartSupport): EventConsumption {
    return delegates.consumeUntil(Consumed) {
      it.onLeave(event, chartSupport)
    } ?: Ignored
  }
}

/**
 * Converts pointer events to canvas pointer events
 */
class PointerEvent2CanvasHandler(
  /**
   * The chart support that is supplied
   */
  val chartSupport: ChartSupport,
  /**
   * The delegate that is informed about the events
   */
  val delegate: CanvasPointerEventHandler

) : PointerEventHandler {
  override fun onOver(event: PointerOverEvent): EventConsumption {
    return delegate.onOver(event, chartSupport)
  }

  override fun onEnter(event: PointerEnterEvent): EventConsumption {
    return delegate.onEnter(event, chartSupport)
  }

  override fun onDown(event: PointerDownEvent): EventConsumption {
    return delegate.onDown(event, chartSupport)
  }

  override fun onMove(event: PointerMoveEvent): EventConsumption {
    return delegate.onMove(event, chartSupport)
  }

  override fun onUp(event: PointerUpEvent): EventConsumption {
    return delegate.onUp(event, chartSupport)
  }

  override fun onCancel(event: PointerCancelEvent): EventConsumption {
    return delegate.onCancel(event, chartSupport)
  }

  override fun onOut(event: PointerOutEvent): EventConsumption {
    return delegate.onOut(event, chartSupport)
  }

  override fun onLeave(event: PointerLeaveEvent): EventConsumption {
    return delegate.onLeave(event, chartSupport)
  }
}

/**
 * Converts a canvas pointer event handler to a pointer event handler by providing the given chart support for all events
 */
fun CanvasPointerEventHandler.asPointerEventHandler(chartSupport: ChartSupport): PointerEventHandler {
  return PointerEvent2CanvasHandler(
    chartSupport,
    this
  )
}

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
import it.neckar.events.TouchCancelEvent
import it.neckar.events.TouchEndEvent
import it.neckar.events.TouchEvent
import com.meistercharts.events.TouchEventHandler
import it.neckar.events.TouchMoveEvent
import it.neckar.events.TouchStartEvent

/**
 * Handler of [TouchEvent]s from a canvas
 *
 */
interface CanvasTouchEventHandler {
  /**
   * Is notified when a [TouchStartEvent] is fired
   */
  fun onStart(event: TouchStartEvent, chartSupport: ChartSupport): EventConsumption {
    return EventConsumption.Ignored
  }

  /**
   * Is notified when a [TouchEndEvent] is fired
   */
  fun onEnd(event: TouchEndEvent, chartSupport: ChartSupport): EventConsumption {
    return EventConsumption.Ignored
  }

  /**
   * Is notified when a [TouchMoveEvent] is fired
   */
  fun onMove(event: TouchMoveEvent, chartSupport: ChartSupport): EventConsumption {
    return EventConsumption.Ignored
  }

  /**
   * Is notified when a [TouchCancelEvent] is fired
   */
  fun onCancel(event: TouchCancelEvent, chartSupport: ChartSupport): EventConsumption {
    return EventConsumption.Ignored
  }
}

fun TouchEventHandler.toCanvasTouchEventHandler(): CanvasTouchEventHandler {
  return object : CanvasTouchEventHandler {
    override fun onStart(event: TouchStartEvent, chartSupport: ChartSupport): EventConsumption {
      return onStart(event)
    }

    override fun onEnd(event: TouchEndEvent, chartSupport: ChartSupport): EventConsumption {
      return onEnd(event)
    }

    override fun onMove(event: TouchMoveEvent, chartSupport: ChartSupport): EventConsumption {
      return onMove(event)
    }

    override fun onCancel(event: TouchCancelEvent, chartSupport: ChartSupport): EventConsumption {
      return onCancel(event)
    }
  }
}

class CanvasTouchEventHandlerBroker : CanvasTouchEventHandler {
  private val delegates = mutableListOf<CanvasTouchEventHandler>()

  fun delegate(eventHandler: CanvasTouchEventHandler) {
    delegates.add(eventHandler)
  }

  override fun onStart(event: TouchStartEvent, chartSupport: ChartSupport): EventConsumption {
    return delegates.consumeUntil(EventConsumption.Consumed) {
      it.onStart(event, chartSupport)
    } ?: EventConsumption.Ignored
  }

  override fun onEnd(event: TouchEndEvent, chartSupport: ChartSupport): EventConsumption {
    return delegates.consumeUntil(EventConsumption.Consumed) {
      it.onEnd(event, chartSupport)
    } ?: EventConsumption.Ignored
  }

  override fun onMove(event: TouchMoveEvent, chartSupport: ChartSupport): EventConsumption {
    return delegates.consumeUntil(EventConsumption.Consumed) {
      it.onMove(event, chartSupport)
    } ?: EventConsumption.Ignored
  }

  override fun onCancel(event: TouchCancelEvent, chartSupport: ChartSupport): EventConsumption {
    return delegates.consumeUntil(EventConsumption.Consumed) {
      it.onCancel(event, chartSupport)
    } ?: EventConsumption.Ignored
  }
}

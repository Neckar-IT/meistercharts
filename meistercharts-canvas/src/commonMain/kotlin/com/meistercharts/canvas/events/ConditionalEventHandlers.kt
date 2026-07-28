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
package com.meistercharts.canvas.events

import com.meistercharts.canvas.ChartSupport
import com.meistercharts.events.EventConsumption
import it.neckar.events.KeyDownEvent
import it.neckar.events.KeyTypeEvent
import it.neckar.events.KeyUpEvent
import it.neckar.events.MouseClickEvent
import it.neckar.events.MouseDoubleClickEvent
import it.neckar.events.MouseDownEvent
import it.neckar.events.MouseDragEvent
import it.neckar.events.MouseMoveEvent
import it.neckar.events.MouseUpEvent
import it.neckar.events.MouseWheelEvent

/**
 * Returns a handler that passes events on only while [enabled] returns true, and ignores every event otherwise.
 *
 * Use this to state a precondition - a layer that only reacts in a certain mode, for example - in one place instead of
 * repeating it at the top of every event method.
 *
 * ```
 * override val mouseEventHandler: CanvasMouseEventHandler = broker.enabledWhen { configuration.planningEnabled }
 * ```
 */
fun CanvasMouseEventHandler.enabledWhen(enabled: () -> Boolean): CanvasMouseEventHandler {
  val delegate = this

  return object : CanvasMouseEventHandler {
    override fun onClick(event: MouseClickEvent, chartSupport: ChartSupport): EventConsumption {
      return if (enabled()) delegate.onClick(event, chartSupport) else EventConsumption.Ignored
    }

    override fun onDown(event: MouseDownEvent, chartSupport: ChartSupport): EventConsumption {
      return if (enabled()) delegate.onDown(event, chartSupport) else EventConsumption.Ignored
    }

    override fun onUp(event: MouseUpEvent, chartSupport: ChartSupport): EventConsumption {
      return if (enabled()) delegate.onUp(event, chartSupport) else EventConsumption.Ignored
    }

    override fun onDoubleClick(event: MouseDoubleClickEvent, chartSupport: ChartSupport): EventConsumption {
      return if (enabled()) delegate.onDoubleClick(event, chartSupport) else EventConsumption.Ignored
    }

    override fun onMove(event: MouseMoveEvent, chartSupport: ChartSupport): EventConsumption {
      return if (enabled()) delegate.onMove(event, chartSupport) else EventConsumption.Ignored
    }

    override fun onDrag(event: MouseDragEvent, chartSupport: ChartSupport): EventConsumption {
      return if (enabled()) delegate.onDrag(event, chartSupport) else EventConsumption.Ignored
    }

    override fun onWheel(event: MouseWheelEvent, chartSupport: ChartSupport): EventConsumption {
      return if (enabled()) delegate.onWheel(event, chartSupport) else EventConsumption.Ignored
    }
  }
}

/**
 * Returns a handler that passes key events on only while [enabled] returns true, and ignores every event otherwise.
 */
fun CanvasKeyEventHandler.enabledWhen(enabled: () -> Boolean): CanvasKeyEventHandler {
  val delegate = this

  return object : CanvasKeyEventHandler {
    override fun onDown(event: KeyDownEvent, chartSupport: ChartSupport): EventConsumption {
      return if (enabled()) delegate.onDown(event, chartSupport) else EventConsumption.Ignored
    }

    override fun onUp(event: KeyUpEvent, chartSupport: ChartSupport): EventConsumption {
      return if (enabled()) delegate.onUp(event, chartSupport) else EventConsumption.Ignored
    }

    override fun onType(event: KeyTypeEvent, chartSupport: ChartSupport): EventConsumption {
      return if (enabled()) delegate.onType(event, chartSupport) else EventConsumption.Ignored
    }
  }
}

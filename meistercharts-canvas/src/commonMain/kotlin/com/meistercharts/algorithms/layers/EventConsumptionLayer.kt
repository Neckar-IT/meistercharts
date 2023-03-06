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
package com.meistercharts.algorithms.layers

import com.meistercharts.canvas.ChartSupport
import com.meistercharts.canvas.events.CanvasKeyEventHandler
import com.meistercharts.canvas.events.CanvasMouseEventHandler
import com.meistercharts.canvas.events.CanvasPointerEventHandler
import com.meistercharts.events.EventConsumption
import com.meistercharts.events.EventConsumption.Consumed
import com.meistercharts.events.KeyDownEvent
import com.meistercharts.events.KeyTypeEvent
import com.meistercharts.events.KeyUpEvent
import com.meistercharts.events.MouseClickEvent
import com.meistercharts.events.MouseDoubleClickEvent
import com.meistercharts.events.MouseMoveEvent
import com.meistercharts.events.MouseWheelEvent
import com.meistercharts.events.PointerCancelEvent
import com.meistercharts.events.PointerDownEvent
import com.meistercharts.events.PointerEnterEvent
import com.meistercharts.events.PointerLeaveEvent
import com.meistercharts.events.PointerMoveEvent
import com.meistercharts.events.PointerOutEvent
import com.meistercharts.events.PointerOverEvent
import com.meistercharts.events.PointerUpEvent

/**
 * An [Layer] that consumes every event it receives.
 *
 */
class EventConsumptionLayer : AbstractLayer() {

  override val type: LayerType
    get() = LayerType.Content

  override fun paint(paintingContext: LayerPaintingContext) {
  }

  override val mouseEventHandler: CanvasMouseEventHandler? = object : CanvasMouseEventHandler {
    override fun onClick(event: MouseClickEvent, chartSupport: ChartSupport): EventConsumption {
      return Consumed
    }

    override fun onDoubleClick(event: MouseDoubleClickEvent, chartSupport: ChartSupport): EventConsumption {
      return Consumed
    }

    override fun onMove(event: MouseMoveEvent, chartSupport: ChartSupport): EventConsumption {
      return Consumed
    }

    override fun onWheel(event: MouseWheelEvent, chartSupport: ChartSupport): EventConsumption {
      return Consumed
    }
  }

  override val keyEventHandler: CanvasKeyEventHandler? = object : CanvasKeyEventHandler {
    override fun onDown(event: KeyDownEvent, chartSupport: ChartSupport): EventConsumption {
      return Consumed
    }

    override fun onUp(event: KeyUpEvent, chartSupport: ChartSupport): EventConsumption {
      return Consumed
    }

    override fun onType(event: KeyTypeEvent, chartSupport: ChartSupport): EventConsumption {
      return Consumed
    }
  }

  override val pointerEventHandler: CanvasPointerEventHandler? = object : CanvasPointerEventHandler {
    override fun onOver(event: PointerOverEvent, chartSupport: ChartSupport): EventConsumption {
      return Consumed
    }

    override fun onEnter(event: PointerEnterEvent, chartSupport: ChartSupport): EventConsumption {
      return Consumed
    }

    override fun onDown(event: PointerDownEvent, chartSupport: ChartSupport): EventConsumption {
      return Consumed
    }

    override fun onMove(event: PointerMoveEvent, chartSupport: ChartSupport): EventConsumption {
      return Consumed
    }

    override fun onUp(event: PointerUpEvent, chartSupport: ChartSupport): EventConsumption {
      return Consumed
    }

    override fun onCancel(event: PointerCancelEvent, chartSupport: ChartSupport): EventConsumption {
      return Consumed
    }

    override fun onOut(event: PointerOutEvent, chartSupport: ChartSupport): EventConsumption {
      return Consumed
    }

    override fun onLeave(event: PointerLeaveEvent, chartSupport: ChartSupport): EventConsumption {
      return Consumed
    }
  }
}

/**
 * Adds an [EventConsumptionLayer] to this [Layers]
 */
fun Layers.consumeEvents() {
  addLayer(EventConsumptionLayer())
}

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
import com.meistercharts.canvas.events.CanvasTouchEventHandler
import com.meistercharts.canvas.saved
import it.neckar.open.kotlin.lang.consumeUntil
import com.meistercharts.events.EventConsumption
import com.meistercharts.events.EventConsumption.Consumed
import com.meistercharts.events.EventConsumption.Ignored
import com.meistercharts.events.KeyDownEvent
import com.meistercharts.events.KeyTypeEvent
import com.meistercharts.events.KeyUpEvent
import com.meistercharts.events.MouseClickEvent
import com.meistercharts.events.MouseDoubleClickEvent
import com.meistercharts.events.MouseDownEvent
import com.meistercharts.events.MouseDragEvent
import com.meistercharts.events.MouseMoveEvent
import com.meistercharts.events.MouseUpEvent
import com.meistercharts.events.MouseWheelEvent
import com.meistercharts.events.PointerCancelEvent
import com.meistercharts.events.PointerDownEvent
import com.meistercharts.events.PointerEnterEvent
import com.meistercharts.events.PointerLeaveEvent
import com.meistercharts.events.PointerMoveEvent
import com.meistercharts.events.PointerOutEvent
import com.meistercharts.events.PointerOverEvent
import com.meistercharts.events.PointerUpEvent
import com.meistercharts.events.TouchCancelEvent
import com.meistercharts.events.TouchEndEvent
import com.meistercharts.events.TouchMoveEvent
import com.meistercharts.events.TouchStartEvent

/**
 * An (immutable) list of layers that can be added/removed together
 */
class LayerList(
  /**
   * The list of the layers - in paint order
   */
  val layers: List<Layer>,
  val layerType: LayerType = LayerType.Content,
) : AbstractLayer() {
  override val type: LayerType
    get() = layerType

  constructor(
    vararg layers: Layer,
    layerType: LayerType = LayerType.Content
  ) : this(layers.asList(), layerType)

  override val mouseEventHandler: CanvasMouseEventHandler = object : CanvasMouseEventHandler {
    override fun onClick(event: MouseClickEvent, chartSupport: ChartSupport): EventConsumption {
      return layers.consumeUntil(Consumed) {
        it.mouseEventHandler?.onClick(event, chartSupport)
      } ?: Ignored
    }

    override fun onDown(event: MouseDownEvent, chartSupport: ChartSupport): EventConsumption {
      return layers.consumeUntil(Consumed) {
        it.mouseEventHandler?.onDown(event, chartSupport)
      } ?: Ignored
    }

    override fun onUp(event: MouseUpEvent, chartSupport: ChartSupport): EventConsumption {
      return layers.consumeUntil(Consumed) {
        it.mouseEventHandler?.onUp(event, chartSupport)
      } ?: Ignored
    }

    override fun onDoubleClick(event: MouseDoubleClickEvent, chartSupport: ChartSupport): EventConsumption {
      return layers.consumeUntil(Consumed) {
        it.mouseEventHandler?.onDoubleClick(event, chartSupport)
      } ?: Ignored
    }

    override fun onMove(event: MouseMoveEvent, chartSupport: ChartSupport): EventConsumption {
      return layers.consumeUntil(Consumed) {
        it.mouseEventHandler?.onMove(event, chartSupport)
      } ?: Ignored
    }

    override fun onDrag(event: MouseDragEvent, chartSupport: ChartSupport): EventConsumption {
      return layers.consumeUntil(Consumed) {
        it.mouseEventHandler?.onDrag(event, chartSupport)
      } ?: Ignored
    }

    override fun onWheel(event: MouseWheelEvent, chartSupport: ChartSupport): EventConsumption {
      return layers.consumeUntil(Consumed) {
        it.mouseEventHandler?.onWheel(event, chartSupport)
      } ?: Ignored
    }
  }

  override val keyEventHandler: CanvasKeyEventHandler? = object : CanvasKeyEventHandler {
    override fun onDown(event: KeyDownEvent, chartSupport: ChartSupport): EventConsumption {
      return layers.consumeUntil(Consumed) {
        it.keyEventHandler?.onDown(event, chartSupport)
      } ?: Ignored
    }

    override fun onUp(event: KeyUpEvent, chartSupport: ChartSupport): EventConsumption {
      return layers.consumeUntil(Consumed) {
        it.keyEventHandler?.onUp(event, chartSupport)
      } ?: Ignored
    }

    override fun onType(event: KeyTypeEvent, chartSupport: ChartSupport): EventConsumption {
      return layers.consumeUntil(Consumed) {
        it.keyEventHandler?.onType(event, chartSupport)
      } ?: Ignored
    }
  }

  override fun layout(paintingContext: LayerPaintingContext) {
    super.layout(paintingContext)
    layers.forEach { layer ->
      paintingContext.gc.saved {
        layer.layout(paintingContext)
      }
    }
  }

  @Suppress("RedundantOverride")
  override fun initialize(paintingContext: LayerPaintingContext) {
    super.initialize(paintingContext)
    //Do nothing - we call layout for each layer
  }

  override fun paint(paintingContext: LayerPaintingContext) {
    layers.forEach { layer ->
      paintingContext.gc.saved {
        layer.paint(paintingContext)
      }
    }
  }

  override val pointerEventHandler: CanvasPointerEventHandler? = object : CanvasPointerEventHandler {
    override fun onOver(event: PointerOverEvent, chartSupport: ChartSupport): EventConsumption {
      return layers.consumeUntil(Consumed) {
        it.pointerEventHandler?.onOver(event, chartSupport)
      } ?: Ignored
    }

    override fun onEnter(event: PointerEnterEvent, chartSupport: ChartSupport): EventConsumption {
      return layers.consumeUntil(Consumed) {
        it.pointerEventHandler?.onEnter(event, chartSupport)
      } ?: Ignored
    }

    override fun onDown(event: PointerDownEvent, chartSupport: ChartSupport): EventConsumption {
      return layers.consumeUntil(Consumed) {
        it.pointerEventHandler?.onDown(event, chartSupport)
      } ?: Ignored
    }

    override fun onMove(event: PointerMoveEvent, chartSupport: ChartSupport): EventConsumption {
      return layers.consumeUntil(Consumed) {
        it.pointerEventHandler?.onMove(event, chartSupport)
      } ?: Ignored
    }

    override fun onUp(event: PointerUpEvent, chartSupport: ChartSupport): EventConsumption {
      return layers.consumeUntil(Consumed) {
        it.pointerEventHandler?.onUp(event, chartSupport)
      } ?: Ignored
    }

    override fun onCancel(event: PointerCancelEvent, chartSupport: ChartSupport): EventConsumption {
      return layers.consumeUntil(Consumed) {
        it.pointerEventHandler?.onCancel(event, chartSupport)
      } ?: Ignored
    }

    override fun onOut(event: PointerOutEvent, chartSupport: ChartSupport): EventConsumption {
      return layers.consumeUntil(Consumed) {
        it.pointerEventHandler?.onOut(event, chartSupport)
      } ?: Ignored
    }

    override fun onLeave(event: PointerLeaveEvent, chartSupport: ChartSupport): EventConsumption {
      return layers.consumeUntil(Consumed) {
        it.pointerEventHandler?.onLeave(event, chartSupport)
      } ?: Ignored
    }
  }

  override val touchEventHandler: CanvasTouchEventHandler? = object : CanvasTouchEventHandler {
    override fun onStart(event: TouchStartEvent, chartSupport: ChartSupport): EventConsumption {
      return layers.consumeUntil(Consumed) {
        it.touchEventHandler?.onStart(event, chartSupport)
      } ?: Ignored
    }

    override fun onEnd(event: TouchEndEvent, chartSupport: ChartSupport): EventConsumption {
      return layers.consumeUntil(Consumed) {
        it.touchEventHandler?.onEnd(event, chartSupport)
      } ?: Ignored
    }

    override fun onMove(event: TouchMoveEvent, chartSupport: ChartSupport): EventConsumption {
      return layers.consumeUntil(Consumed) {
        it.touchEventHandler?.onMove(event, chartSupport)
      } ?: Ignored
    }

    override fun onCancel(event: TouchCancelEvent, chartSupport: ChartSupport): EventConsumption {
      return layers.consumeUntil(Consumed) {
        it.touchEventHandler?.onCancel(event, chartSupport)
      } ?: Ignored
    }
  }
}

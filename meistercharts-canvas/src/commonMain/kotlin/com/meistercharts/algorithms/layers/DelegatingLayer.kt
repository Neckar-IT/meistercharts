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
import it.neckar.events.PointerCancelEvent
import it.neckar.events.PointerDownEvent
import it.neckar.events.PointerEnterEvent
import it.neckar.events.PointerLeaveEvent
import it.neckar.events.PointerMoveEvent
import it.neckar.events.PointerOutEvent
import it.neckar.events.PointerOverEvent
import it.neckar.events.PointerUpEvent
import it.neckar.events.TouchCancelEvent
import it.neckar.events.TouchEndEvent
import it.neckar.events.TouchMoveEvent
import it.neckar.events.TouchStartEvent
import it.neckar.open.provider.BooleanProvider

/**
 * Base class for delegating layers that delegate to a *single* other layer
 */
abstract class DelegatingLayer<out T : Layer>(
  val delegate: T,
  /**
   * If the check is provided, the lambda will be called for every event.
   * The events are only delegated if the condition returns true.
   */
  val delegateEventsCondition: BooleanProvider = BooleanProvider.True,
) : AbstractLayer() {

  override val type: LayerType = delegate.type

  override fun layout(paintingContext: LayerPaintingContext) {
    super.layout(paintingContext)
    layoutDelegate(paintingContext)
  }

  /**
   * Calls layout on the delegate.
   * This method has been extracted to allow subclasses to override this method
   */
  protected open fun layoutDelegate(paintingContext: LayerPaintingContext) {
    delegate.layout(paintingContext)
  }

  override fun paint(paintingContext: LayerPaintingContext) {
    delegate.paint(paintingContext)
  }

  private fun delegateEvents(): Boolean {
    return delegateEventsCondition()
  }

  /**
   * Delegating mouse event handler
   */
  override val mouseEventHandler: CanvasMouseEventHandler = object : CanvasMouseEventHandler {
    override fun onClick(event: MouseClickEvent, chartSupport: ChartSupport): EventConsumption {
      if (delegateEvents()) {
        return delegate.mouseEventHandler?.onClick(event, chartSupport) ?: EventConsumption.Ignored
      }
      return EventConsumption.Ignored
    }

    override fun onDown(event: MouseDownEvent, chartSupport: ChartSupport): EventConsumption {
      if (delegateEvents()) {
        return delegate.mouseEventHandler?.onDown(event, chartSupport) ?: EventConsumption.Ignored
      }
      return EventConsumption.Ignored
    }

    override fun onUp(event: MouseUpEvent, chartSupport: ChartSupport): EventConsumption {
      if (delegateEvents()) {
        return delegate.mouseEventHandler?.onUp(event, chartSupport) ?: EventConsumption.Ignored
      }
      return EventConsumption.Ignored
    }

    override fun onDoubleClick(event: MouseDoubleClickEvent, chartSupport: ChartSupport): EventConsumption {
      if (delegateEvents()) {
        return delegate.mouseEventHandler?.onDoubleClick(event, chartSupport) ?: EventConsumption.Ignored
      }
      return EventConsumption.Ignored
    }

    override fun onMove(event: MouseMoveEvent, chartSupport: ChartSupport): EventConsumption {
      if (delegateEvents()) {
        return delegate.mouseEventHandler?.onMove(event, chartSupport) ?: EventConsumption.Ignored
      }
      return EventConsumption.Ignored
    }

    override fun onDrag(event: MouseDragEvent, chartSupport: ChartSupport): EventConsumption {
      if (delegateEvents()) {
        return delegate.mouseEventHandler?.onDrag(event, chartSupport) ?: EventConsumption.Ignored
      }
      return EventConsumption.Ignored
    }

    override fun onWheel(event: MouseWheelEvent, chartSupport: ChartSupport): EventConsumption {
      if (delegateEvents()) {
        return delegate.mouseEventHandler?.onWheel(event, chartSupport) ?: EventConsumption.Ignored
      }
      return EventConsumption.Ignored
    }
  }

  override val keyEventHandler: CanvasKeyEventHandler = object : CanvasKeyEventHandler {
    override fun onDown(event: KeyDownEvent, chartSupport: ChartSupport): EventConsumption {
      if (delegateEvents()) {
        return delegate.keyEventHandler?.onDown(event, chartSupport) ?: EventConsumption.Ignored
      }
      return EventConsumption.Ignored
    }

    override fun onUp(event: KeyUpEvent, chartSupport: ChartSupport): EventConsumption {
      if (delegateEvents()) {
        return delegate.keyEventHandler?.onUp(event, chartSupport) ?: EventConsumption.Ignored
      }
      return EventConsumption.Ignored
    }

    override fun onType(event: KeyTypeEvent, chartSupport: ChartSupport): EventConsumption {
      if (delegateEvents()) {
        return delegate.keyEventHandler?.onType(event, chartSupport) ?: EventConsumption.Ignored
      }
      return EventConsumption.Ignored
    }
  }

  @Deprecated("Pointer events are not supported")
  override val pointerEventHandler: CanvasPointerEventHandler = object : CanvasPointerEventHandler {
    override fun onOver(event: PointerOverEvent, chartSupport: ChartSupport): EventConsumption {
      if (delegateEvents()) {
        return delegate.pointerEventHandler?.onOver(event, chartSupport) ?: EventConsumption.Ignored
      }
      return EventConsumption.Ignored
    }

    override fun onEnter(event: PointerEnterEvent, chartSupport: ChartSupport): EventConsumption {
      if (delegateEvents()) {
        return delegate.pointerEventHandler?.onEnter(event, chartSupport) ?: EventConsumption.Ignored
      }
      return EventConsumption.Ignored
    }

    override fun onDown(event: PointerDownEvent, chartSupport: ChartSupport): EventConsumption {
      if (delegateEvents()) {
        return delegate.pointerEventHandler?.onDown(event, chartSupport) ?: EventConsumption.Ignored
      }
      return EventConsumption.Ignored
    }

    override fun onMove(event: PointerMoveEvent, chartSupport: ChartSupport): EventConsumption {
      if (delegateEvents()) {
        return delegate.pointerEventHandler?.onMove(event, chartSupport) ?: EventConsumption.Ignored
      }
      return EventConsumption.Ignored
    }

    override fun onUp(event: PointerUpEvent, chartSupport: ChartSupport): EventConsumption {
      if (delegateEvents()) {
        return delegate.pointerEventHandler?.onUp(event, chartSupport) ?: EventConsumption.Ignored
      }
      return EventConsumption.Ignored
    }

    override fun onCancel(event: PointerCancelEvent, chartSupport: ChartSupport): EventConsumption {
      if (delegateEvents()) {
        return delegate.pointerEventHandler?.onCancel(event, chartSupport) ?: EventConsumption.Ignored
      }
      return EventConsumption.Ignored
    }

    override fun onOut(event: PointerOutEvent, chartSupport: ChartSupport): EventConsumption {
      if (delegateEvents()) {
        return delegate.pointerEventHandler?.onOut(event, chartSupport) ?: EventConsumption.Ignored
      }
      return EventConsumption.Ignored
    }

    override fun onLeave(event: PointerLeaveEvent, chartSupport: ChartSupport): EventConsumption {
      if (delegateEvents()) {
        return delegate.pointerEventHandler?.onLeave(event, chartSupport) ?: EventConsumption.Ignored
      }
      return EventConsumption.Ignored
    }
  }

  override val touchEventHandler: CanvasTouchEventHandler = object : CanvasTouchEventHandler {
    override fun onStart(event: TouchStartEvent, chartSupport: ChartSupport): EventConsumption {
      if (delegateEvents()) {
        return delegate.touchEventHandler?.onStart(event, chartSupport) ?: EventConsumption.Ignored
      }
      return EventConsumption.Ignored
    }

    override fun onEnd(event: TouchEndEvent, chartSupport: ChartSupport): EventConsumption {
      if (delegateEvents()) {
        return delegate.touchEventHandler?.onEnd(event, chartSupport) ?: EventConsumption.Ignored
      }
      return EventConsumption.Ignored
    }

    override fun onMove(event: TouchMoveEvent, chartSupport: ChartSupport): EventConsumption {
      if (delegateEvents()) {
        return delegate.touchEventHandler?.onMove(event, chartSupport) ?: EventConsumption.Ignored
      }
      return EventConsumption.Ignored
    }

    override fun onCancel(event: TouchCancelEvent, chartSupport: ChartSupport): EventConsumption {
      if (delegateEvents()) {
        return delegate.touchEventHandler?.onCancel(event, chartSupport) ?: EventConsumption.Ignored
      }
      return EventConsumption.Ignored
    }
  }

  override fun removed() {
    super.removed()
    delegate.removed()
  }
}

/**
 * Returns true if:
 * * this is a delegating layer
 * * this is delegating to the given layer
 * * this is delegating to one (or more) other delegating layers which are then delegating to the given layer
 */
fun Layer.isDelegatingTo(layer: Layer): Boolean {
  if (this !is DelegatingLayer<*>) {
    return false
  }

  if (this.delegate == layer) {
    return true
  }

  return this.delegate.isDelegatingTo(layer)
}

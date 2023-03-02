package com.meistercharts.algorithms.layers

import com.meistercharts.canvas.ChartSupport
import com.meistercharts.canvas.events.CanvasKeyEventHandler
import com.meistercharts.canvas.events.CanvasMouseEventHandler
import com.meistercharts.canvas.events.CanvasPointerEventHandler
import com.meistercharts.canvas.events.CanvasTouchEventHandler
import com.meistercharts.events.EventConsumption
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
 * Holds an [Layer] delegate and paints it depending on value of visible property
 */
open class LayerVisibilityAdapter(
  val delegate: Layer,
  /**
   * The visibility condition. If it returns true, the layer is visible
   */
  val visibleCondition: () -> Boolean,
  /**
   * If set to true the events will be delegated, even if the layer is invisible
   */
  val delegateEventsIfInvisible: Boolean = false
) : AbstractLayer() {
  /**
   * If the layer is visible
   */
  open val visible: Boolean
    get() = visibleCondition()

  override val type: LayerType
    get() = delegate.type

  override val description: String
    get() = "VisibilityAdapter{${delegate.description}}"

  /**
   * Returns true if the events should be delegated at the moment, false otherwise
   */
  private fun delegateEvents() = delegateEventsIfInvisible || visible

  override fun layout(paintingContext: LayerPaintingContext) {
    super.layout(paintingContext)
    if (visible) {
      delegate.layout(paintingContext)
    }
  }

  override fun paint(paintingContext: LayerPaintingContext) {
    if (visible) {
      delegate.paint(paintingContext)
    }
  }

  /**
   * Delegating mouse event handler
   */
  override val mouseEventHandler: CanvasMouseEventHandler = object : CanvasMouseEventHandler {
    override fun onClick(event: MouseClickEvent, chartSupport: ChartSupport): EventConsumption {
      if (delegateEvents()) {
        return delegate.mouseEventHandler?.onClick(event, chartSupport) ?: Ignored
      }
      return Ignored
    }

    override fun onDown(event: MouseDownEvent, chartSupport: ChartSupport): EventConsumption {
      if (delegateEvents()) {
        return delegate.mouseEventHandler?.onDown(event, chartSupport) ?: Ignored
      }
      return Ignored
    }

    override fun onUp(event: MouseUpEvent, chartSupport: ChartSupport): EventConsumption {
      if (delegateEvents()) {
        return delegate.mouseEventHandler?.onUp(event, chartSupport) ?: Ignored
      }
      return Ignored
    }

    override fun onDoubleClick(event: MouseDoubleClickEvent, chartSupport: ChartSupport): EventConsumption {
      if (delegateEvents()) {
        return delegate.mouseEventHandler?.onDoubleClick(event, chartSupport) ?: Ignored
      }
      return Ignored
    }

    override fun onMove(event: MouseMoveEvent, chartSupport: ChartSupport): EventConsumption {
      if (delegateEvents()) {
        return delegate.mouseEventHandler?.onMove(event, chartSupport) ?: Ignored
      }
      return Ignored
    }

    override fun onDrag(event: MouseDragEvent, chartSupport: ChartSupport): EventConsumption {
      if (delegateEvents()) {
        return delegate.mouseEventHandler?.onDrag(event, chartSupport) ?: Ignored
      }
      return Ignored
    }

    override fun onWheel(event: MouseWheelEvent, chartSupport: ChartSupport): EventConsumption {
      if (delegateEvents()) {
        return delegate.mouseEventHandler?.onWheel(event, chartSupport) ?: Ignored
      }
      return Ignored
    }
  }

  override val keyEventHandler: CanvasKeyEventHandler = object : CanvasKeyEventHandler {
    override fun onDown(event: KeyDownEvent, chartSupport: ChartSupport): EventConsumption {
      if (delegateEvents()) {
        return delegate.keyEventHandler?.onDown(event, chartSupport) ?: Ignored
      }
      return Ignored
    }

    override fun onUp(event: KeyUpEvent, chartSupport: ChartSupport): EventConsumption {
      if (delegateEvents()) {
        return delegate.keyEventHandler?.onUp(event, chartSupport) ?: Ignored
      }
      return Ignored
    }

    override fun onType(event: KeyTypeEvent, chartSupport: ChartSupport): EventConsumption {
      if (delegateEvents()) {
        return delegate.keyEventHandler?.onType(event, chartSupport) ?: Ignored
      }
      return Ignored
    }
  }

  override val pointerEventHandler: CanvasPointerEventHandler = object : CanvasPointerEventHandler {
    override fun onOver(event: PointerOverEvent, chartSupport: ChartSupport): EventConsumption {
      if (delegateEvents()) {
        return delegate.pointerEventHandler?.onOver(event, chartSupport) ?: Ignored
      }
      return Ignored
    }

    override fun onEnter(event: PointerEnterEvent, chartSupport: ChartSupport): EventConsumption {
      if (delegateEvents()) {
        return delegate.pointerEventHandler?.onEnter(event, chartSupport) ?: Ignored
      }
      return Ignored
    }

    override fun onDown(event: PointerDownEvent, chartSupport: ChartSupport): EventConsumption {
      if (delegateEvents()) {
        return delegate.pointerEventHandler?.onDown(event, chartSupport) ?: Ignored
      }
      return Ignored
    }

    override fun onMove(event: PointerMoveEvent, chartSupport: ChartSupport): EventConsumption {
      if (delegateEvents()) {
        return delegate.pointerEventHandler?.onMove(event, chartSupport) ?: Ignored
      }
      return Ignored
    }

    override fun onUp(event: PointerUpEvent, chartSupport: ChartSupport): EventConsumption {
      if (delegateEvents()) {
        return delegate.pointerEventHandler?.onUp(event, chartSupport) ?: Ignored
      }
      return Ignored
    }

    override fun onCancel(event: PointerCancelEvent, chartSupport: ChartSupport): EventConsumption {
      if (delegateEvents()) {
        return delegate.pointerEventHandler?.onCancel(event, chartSupport) ?: Ignored
      }
      return Ignored
    }

    override fun onOut(event: PointerOutEvent, chartSupport: ChartSupport): EventConsumption {
      if (delegateEvents()) {
        return delegate.pointerEventHandler?.onOut(event, chartSupport) ?: Ignored
      }
      return Ignored
    }

    override fun onLeave(event: PointerLeaveEvent, chartSupport: ChartSupport): EventConsumption {
      if (delegateEvents()) {
        return delegate.pointerEventHandler?.onLeave(event, chartSupport) ?: Ignored
      }
      return Ignored
    }
  }

  override val touchEventHandler: CanvasTouchEventHandler = object : CanvasTouchEventHandler {
    override fun onStart(event: TouchStartEvent, chartSupport: ChartSupport): EventConsumption {
      if (delegateEvents()) {
        return delegate.touchEventHandler?.onStart(event, chartSupport) ?: Ignored
      }
      return Ignored
    }

    override fun onEnd(event: TouchEndEvent, chartSupport: ChartSupport): EventConsumption {
      if (delegateEvents()) {
        return delegate.touchEventHandler?.onEnd(event, chartSupport) ?: Ignored
      }
      return Ignored
    }

    override fun onMove(event: TouchMoveEvent, chartSupport: ChartSupport): EventConsumption {
      if (delegateEvents()) {
        return delegate.touchEventHandler?.onMove(event, chartSupport) ?: Ignored
      }
      return Ignored
    }

    override fun onCancel(event: TouchCancelEvent, chartSupport: ChartSupport): EventConsumption {
      if (delegateEvents()) {
        return delegate.touchEventHandler?.onCancel(event, chartSupport) ?: Ignored
      }
      return Ignored
    }
  }
}

/**
 * Wraps the layer and only shows it if the given condition returns true
 */
fun Layer.visibleIf(delegateEventsIfInvisible: Boolean = false, visibleCondition: () -> Boolean): LayerVisibilityAdapter {
  return LayerVisibilityAdapter(this, visibleCondition, delegateEventsIfInvisible)
}

/**
 * Only wraps the layer in a [LayerVisibilityAdapter] if the provided [visibleCondition] is not null
 */
fun Layer.visibleIf(delegateEventsIfInvisible: Boolean = false, visibleCondition: (() -> Boolean)?): Layer {
  if (visibleCondition == null) {
    return this
  }

  return visibleIf(delegateEventsIfInvisible, visibleCondition)
}


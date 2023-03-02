package com.meistercharts.algorithms.layers

import com.meistercharts.canvas.events.CanvasKeyEventHandler
import com.meistercharts.canvas.events.CanvasMouseEventHandler
import com.meistercharts.canvas.events.CanvasPointerEventHandler
import com.meistercharts.canvas.events.CanvasTouchEventHandler

/**
 * Base class for delegating layers that delegate to a *single* other layer
 */
abstract class DelegatingLayer<T : Layer>(val delegate: T) : AbstractLayer() {

  override val type: LayerType = delegate.type

  override fun layout(paintingContext: LayerPaintingContext) {
    super.layout(paintingContext)
    delegate.layout(paintingContext)
  }

  override fun paint(paintingContext: LayerPaintingContext) {
    delegate.paint(paintingContext)
  }

  override val mouseEventHandler: CanvasMouseEventHandler?
    get() = delegate.mouseEventHandler

  override val keyEventHandler: CanvasKeyEventHandler?
    get() = delegate.keyEventHandler

  @Deprecated("Pointer events are not supported")
  override val pointerEventHandler: CanvasPointerEventHandler?
    get() = delegate.pointerEventHandler

  override val touchEventHandler: CanvasTouchEventHandler?
    get() = delegate.touchEventHandler

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

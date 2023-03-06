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

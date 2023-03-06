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

import com.meistercharts.canvas.CanvasRenderingContext
import com.meistercharts.canvas.events.CanvasKeyEventHandler
import com.meistercharts.canvas.events.CanvasMouseEventHandler
import com.meistercharts.canvas.events.CanvasPointerEventHandler
import com.meistercharts.canvas.events.CanvasTouchEventHandler

/**
 * Represents a layer that is painted above (or below?) a charting canvas.
 *
 * ## Painting
 * First [layout] is called. Immediately after all layout calls to all layers, [paint] is called for each layer.
 * This allows layers that are above other layers to use information calculated during layout in the painting method
 */
interface Layer {
  /**
   * The type of the layer
   */
  val type: LayerType

  /**
   * Returns the description of the layer.
   * Only used for debugging purposes
   */
  val description: String
    get() = this::class.simpleName ?: toString()

  /**
   * This method is called first (before [paint]). It should/could be used to calculate the layout.
   * Do *NOT* paint anything on the painting context. Painting is only allowed in [paint].
   *
   * It is allowed to modify the graphics context in this method: E.g. setting the font to calculate text widths etc.
   */
  fun layout(paintingContext: LayerPaintingContext) {}

  /**
   * Tells this [Layer] to paint itself within the given context.
   *
   * The [CanvasRenderingContext] is (usually) placed at the top left of the window.
   * In some cases, the [CanvasRenderingContext] can be translated - e.g. when using delegating layers.
   * These delegating layers *must* understand the internals of the delegating implementation and translate the context accordingly.
   *
   * @param paintingContext the context that contains further objects that can be used to paint the layers
   */
  fun paint(paintingContext: LayerPaintingContext)

  /**
   * The (optional) mouse event handler for this layer
   */
  val mouseEventHandler: CanvasMouseEventHandler?
    get() = null

  /**
   * The (optional) key event handler for this layer.
   * Is notified about key events
   */
  val keyEventHandler: CanvasKeyEventHandler?
    get() = null

  /**
   * The (optional) pointer event handler for this layer.
   * Is notified about pointer events
   */
  @Deprecated("Pointer events are not supported")
  val pointerEventHandler: CanvasPointerEventHandler?
    get() = null

  /**
   * The (optional) touch event handler for this layer.
   * Is notified about touch events
   */
  val touchEventHandler: CanvasTouchEventHandler?
    get() = null

  /**
   * Is called if the layer has been removed from the [Layers]
   */
  fun removed() {
  }

  /**
   * Returns the painting variables for this layer
   */
  fun paintingVariables(): PaintingVariables? {
    return null
  }
}

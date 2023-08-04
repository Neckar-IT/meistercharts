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
package com.meistercharts.canvas.resize

import com.meistercharts.algorithms.layers.Layer
import it.neckar.geometry.Direction
import it.neckar.geometry.Distance
import it.neckar.geometry.HorizontalAlignment
import it.neckar.geometry.Rectangle
import it.neckar.geometry.VerticalAlignment
import it.neckar.open.unit.other.px


/**
 * The model that holds the resizable object information.
 *
 * The values are rendered and handled by the [ResizeByHandlesLayer]
 *
 * ATTENTION: This implementation does only support *ONE* resizable content at a time.
 * Therefore if multiple resizable contents are registered, only the latest one is processed.
 *
 * Each layer may register a [ResizeHandler]. The layer is only notified about resize events for its
 * resizable.
 *
 * Hot it works:
 * 1. a handler that recognizes resize-events (typically a [ResizeByHandlesLayer])
 * calls [notifyResize] every time it detects a resize
 * 2. a [Layer] with potentially resizable content passes itself and a [ResizeHandler] to [onResize].
 * That handler resizes the content when it gets notified about a resize event.
 * 3. the [Layer] passes itself and the current bounds of the resizable content to [setResizable]
 * everytime its layout-function is called.
 * 4. the [Layer] calls [clear] when its content is no longer resizable.
 */
class ResizeHandlesSupport {
  /**
   * Registers the resizable object.
   * Overwrites previous values set by other layers (if there are any).
   */
  fun setResizable(layer: Layer, bounds: Rectangle) {
    if (resizeHandlers[layer] == null) {
      throw IllegalStateException("Register the resize handler for layer $layer first")
    }

    this.resizableContentBounds = bounds
    this.resizableContentLayer = layer
  }

  /**
   * Holds the resize handlers for each layer
   */
  private val resizeHandlers: MutableMap<Layer, ResizeHandler?> = mutableMapOf()

  /**
   * The content bounds for resizable.
   */
  var resizableContentBounds: Rectangle? = null
    private set

  /**
   * The layer that has set the resizable content.
   * The layer is required to be able to notify the correct layer about resize events
   */
  private var resizableContentLayer: Layer? = null

  /**
   * Registers the resize handler that is notified on a resize event
   */
  fun onResize(layer: Layer, resizeHandler: ResizeHandler) {
    resizeHandlers[layer] = resizeHandler
  }

  /**
   * Clears the resizable selection for the given layer
   */
  fun clear(layer: Layer) {
    if (layer == resizableContentLayer) {
      //Only delete content bounds, if this layer has set these values
      resizableContentBounds = null
      resizableContentLayer = null
    }
  }

  /**
   * Is called when the mouse is hovering above a handle
   */
  fun notifyArmed(handleDirection: Direction) {
    currentResizeHandler().armed(handleDirection)
  }

  /**
   * Is notified when the mouse cursor has been removed from above the handle
   */
  fun notifyDisarmed() {
    currentResizeHandler().disarmed()
  }

  /**
   * Resize is starting.
   */
  fun notifyBeginResize(handleDirection: Direction) {
    currentResizeHandler().beginResizing(handleDirection)
  }

  /**
   * Is called on a drag event
   */
  fun notifyResize(handleDirection: Direction, distance: @px Distance) {
    //set values to 0.0 if a center handle is selected
    val deltaX = if (handleDirection.horizontalAlignment == HorizontalAlignment.Center) 0.0 else distance.x
    val deltaY = if (handleDirection.verticalAlignment == VerticalAlignment.Center) 0.0 else distance.y

    currentResizeHandler().resizing(distance, handleDirection, deltaX, deltaY)
  }

  fun notifyResizingFinished() {
    currentResizeHandler().resizingFinished()
  }

  /**
   * Returns the current resize handler - or throws an exception if there is none
   */
  private fun currentResizeHandler(): ResizeHandler {
    requireNotNull(resizableContentLayer) { "resizableContentLayer is required" }
    val resizeHandler = this.resizeHandlers[resizableContentLayer]
    requireNotNull(resizeHandler) { "resizeHandler not found for $resizableContentLayer" }
    return resizeHandler
  }
}

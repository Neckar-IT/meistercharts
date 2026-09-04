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
package com.meistercharts.canvas.resize

import com.meistercharts.algorithms.layers.Layer
import com.meistercharts.loop.PaintingLoopIndex
import it.neckar.geometry.Direction
import it.neckar.geometry.Distance
import it.neckar.geometry.HorizontalAlignment
import it.neckar.geometry.Rectangle
import it.neckar.geometry.VerticalAlignment
import it.neckar.open.kotlin.lang.requireNotNull
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
 * How it works:
 * 1. a handler that recognizes resize-events (typically a [ResizeByHandlesLayer])
 * calls [notifyResize] every time it detects a resize
 * 2. a [Layer] with potentially resizable content passes itself and a [ResizeHandler] to [onResize].
 * That handler resizes the content when it gets notified about a resize event.
 * 3. the [Layer] passes itself and the current bounds of the resizable content to [setResizable]
 * everytime its layout-function is called - before the [ResizeByHandlesLayer] lays out, which otherwise
 * never sees a registration.
 * 4. the [Layer] calls [clear] when its content is no longer resizable.
 *
 * A registration counts for the paint loop it was made in. A layer that stops laying out at all - because it
 * is hidden, for example - therefore loses its handles although it never reaches its [clear].
 */
class ResizeHandlesSupport {
  /**
   * Registers the resizable object for [loopIndex].
   * Overwrites previous values set by other layers (if there are any).
   */
  fun setResizable(layer: Layer, bounds: Rectangle, loopIndex: PaintingLoopIndex) {
    check(resizeHandlers[layer] != null) { "Register the resize handler for layer $layer first" }

    this.resizableContentBounds = bounds
    this.resizableContentLayer = layer
    this.resizableContentLoopIndex = loopIndex
  }

  /**
   * Holds the resize handlers for each layer
   */
  private val resizeHandlers: MutableMap<Layer, ResizeHandler?> = mutableMapOf()

  /**
   * The content bounds of the last registration - only valid for [resizableContentLoopIndex].
   */
  private var resizableContentBounds: Rectangle? = null

  /**
   * The paint loop [resizableContentBounds] was registered in.
   */
  private var resizableContentLoopIndex: PaintingLoopIndex = PaintingLoopIndex.Unknown

  /**
   * The layer that has set the resizable content last. Outlives its [resizableContentBounds] - see [clear].
   */
  private var resizableContentLayer: Layer? = null

  /**
   * Returns the content bounds for the resizable - null if no layer has registered one in [loopIndex].
   */
  fun resizableContentBounds(loopIndex: PaintingLoopIndex): Rectangle? {
    if (resizableContentLoopIndex != loopIndex) {
      return null
    }

    return resizableContentBounds
  }

  /**
   * Registers the resize handler that is notified on a resize event
   */
  fun onResize(layer: Layer, resizeHandler: ResizeHandler) {
    resizeHandlers[layer] = resizeHandler
  }

  /**
   * Clears the resizable selection for the given layer. The layer stays the one that is notified: a gesture on the
   * vanished resizable ends one layout later, and that notification has to reach the handler that began it.
   */
  fun clear(layer: Layer) {
    if (layer == resizableContentLayer) {
      resizableContentBounds = null
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
   * Is called on a drag event with the gesture's total distance - see [ResizeHandler.resizing] for the contract.
   */
  fun notifyResize(handleDirection: Direction, totalDistance: @px Distance) {
    //A center handle moves only the one edge it sits on - the other axis keeps its size
    val totalX = if (handleDirection.horizontalAlignment == HorizontalAlignment.Center) 0.0 else totalDistance.x
    val totalY = if (handleDirection.verticalAlignment == VerticalAlignment.Center) 0.0 else totalDistance.y

    currentResizeHandler().resizing(totalDistance, handleDirection, totalX, totalY)
  }

  fun notifyResizingFinished() {
    currentResizeHandler().resizingFinished()
  }

  /**
   * Returns the current resize handler - or throws an exception if there is none
   */
  private fun currentResizeHandler(): ResizeHandler {
    val layer = resizableContentLayer.requireNotNull { "resizableContentLayer is required" }
    return resizeHandlers[layer].requireNotNull { "resizeHandler not found for $layer" }
  }
}

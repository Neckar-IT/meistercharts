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

import com.meistercharts.model.Direction

/**
 * The ui state of the resize by handles layer
 */
sealed interface ResizeByHandlesLayerState {
  /**
   * Is called if there is a move above a handle
   */
  fun hoveringAboveHandle(handleDirection: Direction?): ResizeByHandlesLayerState {
    throw UnsupportedOperationException("hoveringAboveHandle not supported for $this")
  }

  /**
   * Is called when started dragging on a handle
   */
  fun startDragging(handleDirection: Direction): ResizeByHandlesLayerState {
    throw UnsupportedOperationException("startDragging not supported for $this")
  }

  /**
   * is called when finished dragging a handle
   */
  fun finishedDragging(hoverHandleDirection: Direction?): ResizeByHandlesLayerState {
    throw UnsupportedOperationException("finishedDragging not supported for $this")
  }
}

/**
 * Default state
 */
object DefaultState : ResizeByHandlesLayerState {
  override fun hoveringAboveHandle(handleDirection: Direction?): ResizeByHandlesLayerState {
    if (handleDirection == null) {
      return this
    }

    return HoveringOverHandle(handleDirection)
  }

  override fun startDragging(handleDirection: Direction): DraggingHandle {
    return DraggingHandle(handleDirection)
  }

  override fun toString(): String {
    return "DefaultState"
  }
}

/**
 * Hovering above a handle
 */
data class HoveringOverHandle(
  /**
   * The direction of the handle the mouse is currently hovering above
   */
  var handleDirection: Direction
) : ResizeByHandlesLayerState {
  override fun hoveringAboveHandle(handleDirection: Direction?): ResizeByHandlesLayerState {
    if (handleDirection == null) {
      return DefaultState
    }
    return this
  }

  override fun startDragging(handleDirection: Direction): DraggingHandle {
    return DraggingHandle(handleDirection)
  }

  override fun toString(): String {
    return "Hovering above handle $handleDirection"
  }
}

/**
 * Currently dragging the handle
 */
data class DraggingHandle(var handleDirection: Direction) : ResizeByHandlesLayerState {
  override fun finishedDragging(hoverHandleDirection: Direction?): ResizeByHandlesLayerState {
    if (hoverHandleDirection == null) {
      return DefaultState
    }

    return HoveringOverHandle(hoverHandleDirection)
  }

  override fun hoveringAboveHandle(handleDirection: Direction?): ResizeByHandlesLayerState {
    if (handleDirection == null) {
      return this
    }

    return HoveringOverHandle(handleDirection)
  }

  override fun toString(): String {
    return "Dragging handle $handleDirection"
  }
}

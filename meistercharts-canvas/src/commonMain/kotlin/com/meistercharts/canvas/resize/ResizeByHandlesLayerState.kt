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

import it.neckar.geometry.Direction

/**
 * The ui state of the resize by handles layer. [DraggingHandle] is what says a gesture runs; a move above a handle is
 * the only decision this type makes.
 */
sealed interface ResizeByHandlesLayerState {
  /**
   * Is called on every move, with the handle the pointer is above - null when it is above none
   */
  fun hoveringAboveHandle(handleDirection: Direction?): ResizeByHandlesLayerState
}

/**
 * Default state
 */
data object DefaultState : ResizeByHandlesLayerState {
  override fun hoveringAboveHandle(handleDirection: Direction?): ResizeByHandlesLayerState {
    if (handleDirection == null) {
      return this
    }

    return HoveringOverHandle(handleDirection)
  }
}

/**
 * Hovering above a handle
 */
data class HoveringOverHandle(
  /**
   * The direction of the handle the mouse is currently hovering above
   */
  val handleDirection: Direction
) : ResizeByHandlesLayerState {
  override fun hoveringAboveHandle(handleDirection: Direction?): ResizeByHandlesLayerState {
    if (handleDirection == null) {
      return DefaultState
    }
    if (handleDirection == this.handleDirection) {
      return this
    }
    return HoveringOverHandle(handleDirection)
  }

  override fun toString(): String {
    return "Hovering above handle $handleDirection"
  }
}

/**
 * Currently dragging the handle
 */
data class DraggingHandle(val handleDirection: Direction) : ResizeByHandlesLayerState {
  /**
   * A running gesture survives every move - [ResizeByHandlesLayer] is what ends it.
   */
  override fun hoveringAboveHandle(handleDirection: Direction?): ResizeByHandlesLayerState {
    return this
  }

  override fun toString(): String {
    return "Dragging handle $handleDirection"
  }
}

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

import com.meistercharts.annotations.Zoomed
import com.meistercharts.model.Direction
import com.meistercharts.geometry.Distance
import it.neckar.open.unit.other.px

/**
 * Handler that receives resize events.
 */
interface ResizeHandler {
  /**
   * Is notified when the mouse is above the handle
   */
  fun armed(handleDirection: Direction) {
  }

  /**
   * Is
   */
  fun disarmed() {
  }

  fun beginResizing(handleDirection: Direction) {
  }

  /**
   * Is called for each resize event
   */
  fun resizing(
    /**
     * The mouse movement distance taken from the original event
     */
    rawDistance: @px Distance,
    /**
     * The handle direction
     */
    handleDirection: Direction,
    /**
     * The horizontal distance (0.0 for center handles)
     */
    deltaX: @px @Zoomed Double,
    /**
     * The vertical distance (0.0 for center handles)
     */
    deltaY: @px @Zoomed Double,
  )

  fun resizingFinished() {
  }
}

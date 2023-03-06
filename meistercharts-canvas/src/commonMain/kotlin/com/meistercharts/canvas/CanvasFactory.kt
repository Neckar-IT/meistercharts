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
package com.meistercharts.canvas

import it.neckar.open.unit.number.MayBeZero
import com.meistercharts.annotations.PhysicalPixel
import it.neckar.open.unit.number.Positive
import com.meistercharts.model.Size

/**
 * Creates [Canvas] instances
 */
interface CanvasFactory {
  /**
   * Creates a [Canvas] with the given (initial) [size] (*logical* pixels - *not* dependent on the device pixel ratio)
   */
  fun createCanvas(type: CanvasType, size: @MayBeZero Size = Size.zero): Canvas

  /**
   * Creates a canvas with the given physical size
   */
  fun createCanvasWithPhysicalSize(physicalSize: @PhysicalPixel @Positive Size, type: CanvasType): Canvas

  companion object {
    /**
     * Returns the canvas factory
     */
    fun get(): CanvasFactory {
      return meisterChartsFactory().canvasFactory
    }
  }
}

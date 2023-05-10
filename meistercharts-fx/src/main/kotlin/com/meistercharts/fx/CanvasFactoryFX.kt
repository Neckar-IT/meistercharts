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
package com.meistercharts.fx

import com.meistercharts.algorithms.environment
import it.neckar.open.unit.number.MayBeZero
import com.meistercharts.annotations.PhysicalPixel
import com.meistercharts.canvas.Canvas
import com.meistercharts.canvas.CanvasFactory
import com.meistercharts.canvas.CanvasType
import com.meistercharts.model.Size

/**
 * A [CanvasFactory] that creates [Canvas] instances for JavaFx.
 */
class CanvasFactoryFX : CanvasFactory {
  override fun createCanvas(type: CanvasType, size: @MayBeZero Size): CanvasFX {
    return createCanvasWithPhysicalSize(size.times(environment.devicePixelRatio), type)
  }

  /**
   * Creates a canvas with the given physical size
   */
  override fun createCanvasWithPhysicalSize(physicalSize: @PhysicalPixel Size, type: CanvasType): CanvasFX {
    return CanvasFX(type = type).also {
      it.canvas.width = physicalSize.width
      it.canvas.height = physicalSize.height
    }
  }
}

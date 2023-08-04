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
package com.meistercharts.js

import com.meistercharts.environment
import it.neckar.open.unit.number.MayBeZero
import com.meistercharts.annotations.PhysicalPixel
import it.neckar.open.unit.number.Positive
import com.meistercharts.canvas.Canvas
import com.meistercharts.canvas.CanvasFactory
import com.meistercharts.canvas.CanvasType
import it.neckar.geometry.Size

/**
 * A [CanvasFactory] that creates [Canvas] instances for Html.
 */
class CanvasFactoryJS : CanvasFactory {
  override fun createCanvas(type: CanvasType, size: @MayBeZero Size): CanvasJS {
    return CanvasJS(type)
      .also {
        it.applySize(size, "set initial size for canvas with type [$type]")

        it.canvasElement.style.width = "${size.width} px"
        it.canvasElement.style.height = "${size.height} px"

        it.canvasElement.style.margin = "0"
        it.canvasElement.style.padding = "0"
      }
  }

  override fun createCanvasWithPhysicalSize(physicalSize: @PhysicalPixel @Positive Size, type: CanvasType): Canvas {
    return createCanvas(type, physicalSize.divide(environment.devicePixelRatio))
  }
}

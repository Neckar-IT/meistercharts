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

import com.meistercharts.canvas.Canvas
import com.meistercharts.model.Size
import com.meistercharts.fx.BaseCanvasRenderingContextFX
import javafx.scene.canvas.GraphicsContext

/**
 * FX canvas rendering context that just uses a graphics context.
 * Only used for legacy components
 *
 */
class LegacyCanvasRenderingContextFX(
  override val context: GraphicsContext
) : BaseCanvasRenderingContextFX() {

  override val canvas: Canvas
    get() {
      throw UnsupportedOperationException("Canvas is not available for legacy context")
    }

  init {
    applyDefaults()
  }

  override val canvasSize: Size
    get() {
      return Size(width, height)
    }

  override val width: Double
    get() {
      return context.canvas.width
    }
  override val height: Double
    get() {
      return context.canvas.height
    }
}

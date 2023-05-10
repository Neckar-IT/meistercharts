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

import com.meistercharts.canvas.saved
import it.neckar.open.provider.SizedProvider
import it.neckar.open.provider.fastForEach

/**
 * Delegates to multiple other layers.
 * Depending on some condition some of these layers might be visible or hidden.
 *
 * ATTENTION: This layer does *not* support interactions/events
 * TODO: Add events support using the broker classes
 */
class MultipleLayersDelegatingLayer<T : Layer>(
  /**
   * Provides the layers that are painted to.
   * Do only provide the visible layers
   */
  val delegates: SizedProvider<T>,
) : AbstractLayer() {
  override val type: LayerType = LayerType.Content

  override fun layout(paintingContext: LayerPaintingContext) {
    super.layout(paintingContext)
    delegates.fastForEach {
      it.layout(paintingContext)
    }
  }

  override fun paint(paintingContext: LayerPaintingContext) {
    delegates.fastForEach { layer ->
      paintingContext.gc.saved {
        layer.paint(paintingContext)
      }
    }
  }
}

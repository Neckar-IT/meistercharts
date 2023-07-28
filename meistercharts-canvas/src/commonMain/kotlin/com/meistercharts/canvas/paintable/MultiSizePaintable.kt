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
package com.meistercharts.canvas.paintable

import com.meistercharts.algorithms.layers.LayerPaintingContext
import it.neckar.geometry.Size

/**
 * A paintable with multiple paintables
 */
class MultiSizePaintable(
  /**
   * All paintables - sorted by size (large to small)
   */
  val paintables: List<Paintable>

) {

  init {
    require(paintables.isNotEmpty()) { "Need at least one paintable" }
  }

  /**
   * Returns a paintable with the same size (or smaller)
   */
  fun sameOrSmaller(size: Size, paintingContext: LayerPaintingContext): Paintable {
    return sameOrSmaller(size.width, size.height, paintingContext)
  }

  fun sameOrSmaller(width: Double, height: Double, paintingContext: LayerPaintingContext): Paintable {
    return paintables.firstOrNull {
      it.boundingBox(paintingContext).size
        .bothSmallerThanOrEqual(width, height)
    } ?: paintables.last()
  }

  /**
   * Returns the paintable that has the same size (or the next larger one)
   */
  fun sameOrLarger(width: Double, height: Double, paintingContext: LayerPaintingContext): Paintable {
    return paintables.lastOrNull {
      it.boundingBox(paintingContext).size
        .bothLargerThanOrEqual(width, height)
    } ?: paintables.first()
  }
}

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
import com.meistercharts.annotations.Zoomed
import com.meistercharts.geometry.Rectangle
import it.neckar.open.unit.number.IsFinite
import it.neckar.open.unit.number.MayBeZero

/**
 * Advanced paintable that supports layout and painting variables.
 */
interface Paintable2 : Paintable {
  /**
   * Returns the painting variables for this paintable
   */
  fun paintingVariables(): PaintablePaintingVariables

  /**
   * Recalculates the layout for this paintable.
   * The layout method has to be called once in every paint tick.
   *
   * This method is automatically called if necessary by [layoutIfNecessary]
   * from [paint] and [boundingBox].
   *
   * Therefore, usually it is *not* necessary to call [layout] manually.
   *
   * Returns the bounding box - which might have a width and/or height of 0.0
   */
  fun layout(paintingContext: LayerPaintingContext): @Zoomed @IsFinite @MayBeZero Rectangle

  /**
   * Calls [layout] if necessary - by checking the loop index
   */
  fun layoutIfNecessary(paintingContext: LayerPaintingContext)

  /**
   * Is called after the painting variables have been updated by calling [layout]
   */
  fun paintAfterLayout(paintingContext: LayerPaintingContext, x: @Zoomed Double, y: @Zoomed Double)
}

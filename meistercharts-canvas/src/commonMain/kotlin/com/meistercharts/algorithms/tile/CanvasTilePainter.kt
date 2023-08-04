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
package com.meistercharts.algorithms.tile

import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.annotations.Zoomed
import it.neckar.geometry.Size
import kotlin.reflect.KProperty0

/**
 * Paints a single [Tile] on a canvas - only used for a [CanvasTileProvider]
 */
fun interface CanvasTilePainter {
  /**
   * Called when the tile identified by  [identifier] must be painted
   */
  fun paint(identifier: TileIdentifier, paintingContext: LayerPaintingContext, tileSize: @Zoomed Size): TileCreationInfo
}

/**
 * Creates a new instance that delegates to the current value of this property
 */
fun KProperty0<CanvasTilePainter>.delegate(): CanvasTilePainter {
  return CanvasTilePainter { identifier, paintingContext, tileSize ->
    get().paint(identifier, paintingContext, tileSize)
  }
}

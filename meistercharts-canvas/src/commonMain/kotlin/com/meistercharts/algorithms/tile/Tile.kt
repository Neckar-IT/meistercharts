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
import com.meistercharts.canvas.CanvasRenderingContext
import it.neckar.geometry.Size
import it.neckar.open.dispose.Disposable

/**
 * Represents a tile
 */
interface Tile : Disposable {
  /**
   * The identifier for this tile
   */
  val identifier: TileIdentifier

  /**
   * Returns the size of the tile
   */
  val tileSize: @Zoomed Size

  /**
   * Paints the tile on the given rendering context
   */
  fun paint(gc: CanvasRenderingContext, paintingContext: LayerPaintingContext)

  override fun dispose(): Unit = Unit
}

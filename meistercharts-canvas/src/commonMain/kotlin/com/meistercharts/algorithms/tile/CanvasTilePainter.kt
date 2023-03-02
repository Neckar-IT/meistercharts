package com.meistercharts.algorithms.tile

import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.annotations.Zoomed
import com.meistercharts.model.Size
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

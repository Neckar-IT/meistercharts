package com.meistercharts.algorithms.tile

import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.annotations.Zoomed
import com.meistercharts.canvas.CanvasRenderingContext
import com.meistercharts.model.Size
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

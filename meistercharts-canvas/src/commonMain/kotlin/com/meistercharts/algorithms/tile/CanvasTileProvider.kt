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

import com.meistercharts.algorithms.environment
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.annotations.PhysicalPixel
import com.meistercharts.annotations.Zoomed
import com.meistercharts.canvas.Canvas
import com.meistercharts.canvas.CanvasFactory
import com.meistercharts.canvas.CanvasRenderingContext
import com.meistercharts.canvas.CanvasType
import com.meistercharts.canvas.Image
import com.meistercharts.model.Size
import it.neckar.open.dispose.DisposeSupport
import it.neckar.open.annotations.Slow
import it.neckar.open.unit.si.ms

/**
 * A tile provider that holds a canvas for each tile.
 * Each tile is filled using a [CanvasTilePainter].
 *
 * Attention: The tiles are painted using their *physical* size and not the logical size.
 * Therefore, the tile size depends on the current device pixel ratio.
 */
class CanvasTileProvider(
  /**
   * The *physical* size of the tiles.
   */
  val physicalTileSize: @PhysicalPixel Size,

  val tilePainter: CanvasTilePainter,

  private val canvasFactory: CanvasFactory = CanvasFactory.get(),
) : TileProvider {

  /**
   * Returns the tile size.
   * The size depends on the current device pixel ratio
   */
  override val tileSize: Size
    get() = physicalTileSize.divide(environment.devicePixelRatio)

  override fun getTile(identifier: TileIdentifier): Tile? { // TODO async
    val canvas = canvasFactory.createCanvas(CanvasType.OffScreen, tileSize) // TODO async
    return CanvasTile(canvas, tilePainter, identifier)
  }
}

/**
 * A tile that uses a canvas
 */
class CanvasTile(
  val canvas: Canvas,
  val tilePainter: CanvasTilePainter,
  override val identifier: TileIdentifier
) : Tile {

  override val tileSize: @Zoomed Size
    get() = canvas.size

  private val disposeSupport = DisposeSupport().also {
    it.onDispose {
      canvas.dispose()

      //Clear the snapshot to simplify GC and minimize memory usage
      //This might be relevant for Safari
      clearSnapshot()
    }
  }

  /**
   * The snapshot - if there is one
   */
  private var snapshot: Image? = null

  /**
   * The time when the snapshot has been created.
   * The creationInfo is null, if there is no snapshot.
   */
  var creationInfo: @ms TileCreationInfo? = null

  /**
   * Returns true if this canvas tile contains a snapshot
   */
  val hasSnapshot: Boolean
    get() {
      return snapshot != null
    }

  override fun paint(gc: CanvasRenderingContext, paintingContext: LayerPaintingContext) {
    check(!disposeSupport.disposed) { "CanvasTile already disposed" }

    if (snapshot == null) {
      paintingContext.withGraphicsContext(canvas.gc).let {
        it.gc.let { gc ->
          gc.clear()
          gc.applyDefaults()
        }
        creationInfo = tilePainter.paint(identifier, it, tileSize)
        snapshot = canvas.takeSnapshot()
      }
    }

    requireNotNull(snapshot) {
      "Snapshot must not be null!"
    }
    requireNotNull(creationInfo) {
      "creationInfo must not be null!"
    }

    //Check if the tile is empty
    if (creationInfo!!.isEmpty) {
      //If the tile is empty there is no need to paint it
      return
    }

    snapshot!!.let {
      //TODO: when animated paintImage looks better than paintImagePixelPerfect
      gc.paintImagePixelPerfect(it, 0.0, 0.0)
    }
  }

  /**
   * Clears the snapshot and forces a recreation of the snapshot on the next call to paint
   */
  fun clearSnapshot(): SnapshotClearResult {
    if (snapshot == null) {
      return SnapshotClearResult.AlreadyCleared
    }

    snapshot = null
    creationInfo = null

    return SnapshotClearResult.Cleared
  }

  override fun dispose() {
    disposeSupport.dispose()
  }
}

enum class SnapshotClearResult {
  /**
   * The snapshot has been cleared
   */
  Cleared,

  /**
   * The snapshot has already been cleared - nothing has changed
   */
  AlreadyCleared,
}

/**
 * Creates a copy of this [LayerPaintingContext] and replaces its graphics context
 */
fun LayerPaintingContext.withGraphicsContext(gc: CanvasRenderingContext): LayerPaintingContext {
  return LayerPaintingContext(gc, layerSupport, frameTimestamp, frameTimestampDelta, loopIndex, dirtyReasons)
}

/**
 * Returns the cached tiles cast to canvas tile
 */
@Suppress("UNCHECKED_CAST")
@Slow
fun CachedTileProvider.canvasTiles(): Collection<CanvasTile> {
  return tiles() as Collection<CanvasTile>
}

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

import com.meistercharts.annotations.Window
import com.meistercharts.annotations.Zoomed
import com.meistercharts.canvas.paintable.Paintable
import com.meistercharts.canvas.paintable.PaintableProvider
import com.meistercharts.canvas.paintable.ResizablePaintable
import it.neckar.geometry.Direction
import it.neckar.geometry.Distance
import com.meistercharts.model.Insets
import it.neckar.geometry.Size


/**
 * Paints a single paintable in the center of the window.
 */
class PaintableLayer(
  var layoutMode: PaintableLayoutMode = PaintableLayoutMode.UseBoundingBox,
  val paintableProvider: PaintableProvider
) : AbstractLayer() {

  override val type: LayerType
    get() = LayerType.Content

  override fun paintingVariables(): PaintingVariables {
    return paintingVariables
  }

  private val paintingVariables = object : PaintingVariables {
    /**
     * The paintable that is painted
     */
    var paintable: Paintable = Paintable.NoOp

    /**
     * The last x location where the paintable has been painted
     */
    var x: @Window Double = 0.0

    /**
     * The last y location where the paintable has been painted
     */
    var y: @Window Double = 0.0

    override fun calculate(paintingContext: LayerPaintingContext) {
      paintable = paintableProvider(paintingContext)

      val gc = paintingContext.gc

      x = gc.width / 2.0 + offset.x
      y = gc.height / 2.0 + offset.y
    }
  }

  /**
   * The offset relative to the center of the window
   */
  @Zoomed
  var offset: Distance = Distance.none

  override fun paint(paintingContext: LayerPaintingContext) {
    when (layoutMode) {
      PaintableLayoutMode.UseBoundingBox -> paintingVariables.paintable.paintInBoundingBox(
        paintingContext = paintingContext,
        x = paintingVariables.x,
        y = paintingVariables.y,
        anchorDirection = Direction.Center,
        gapHorizontal = 0.0,
        gapVertical = 0.0,
        width = paintingContext.width,
        height = paintingContext.height
      )

      PaintableLayoutMode.Paintable -> paintingVariables.paintable.paint(paintingContext, paintingVariables.x, paintingVariables.y)
    }
  }

  /**
   * The last x location where the paintable has been painted
   */
  val lastX: @Window Double
    get() = paintingVariables.x

  /**
   * The last y location where the paintable has been painted
   */
  val lastY: @Window Double
    get() = paintingVariables.y

  /**
   * The layout mode for the paintable
   */
  enum class PaintableLayoutMode {
    /**
     * Let the paintable decide where to paint to
     */
    Paintable,

    /**
     * Place the bounding box of the paintable in the center
     */
    UseBoundingBox,
  }
}

/**
 * A layer that resizes a paintable
 */
class ResizablePaintableLayer(
  var insets: Insets = Insets.empty,
  val provider: (LayerPaintingContext) -> ResizablePaintable
) : AbstractLayer() {

  override val type: LayerType
    get() = LayerType.Content

  override fun paint(paintingContext: LayerPaintingContext) {
    val gc = paintingContext.gc

    val paintable = provider(paintingContext)
    val size = Size(gc.width - insets.offsetWidth, gc.height - insets.offsetHeight)
    paintable.size = size
    paintable.paintInBoundingBox(paintingContext, insets.left, insets.top, Direction.TopLeft, 0.0, 0.0, size.width, size.height)
  }
}

/**
 * Adds an [PaintableLayer] to this [Layers]
 */
fun Layers.addPaintable(paintableProvider: PaintableProvider) {
  addLayer(PaintableLayer(PaintableLayer.PaintableLayoutMode.UseBoundingBox, paintableProvider))
}

/**
 * Adds a layer that paints the given paintable with full size
 */
fun Layers.addPaintableFull(insets: Insets = Insets.empty, paintableProvider: (LayerPaintingContext) -> ResizablePaintable) {
  addLayer(ResizablePaintableLayer(insets, paintableProvider))
}

/**
 * Adds the paintable
 */
fun Layers.addPaintable(paintable: Paintable) {
  addLayer(PaintableLayer {
    paintable
  })
}

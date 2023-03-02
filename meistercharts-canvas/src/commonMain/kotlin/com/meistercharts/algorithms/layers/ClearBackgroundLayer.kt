package com.meistercharts.algorithms.layers

/**
 * Clears the canvas
 */
class ClearBackgroundLayer : AbstractLayer() {
  override val type: LayerType = LayerType.Background

  override fun paint(paintingContext: LayerPaintingContext) {
    paintingContext.gc.clear()
  }
}

/**
 * Adds a clear background layer
 */
fun Layers.addClearBackground(): ClearBackgroundLayer {
  return ClearBackgroundLayer().also {
    addLayer(it)
  }
}

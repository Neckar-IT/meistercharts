package com.meistercharts.algorithms.layers

object EmptyLayer : AbstractLayer() {
  override val type: LayerType = LayerType.Content

  override fun paint(paintingContext: LayerPaintingContext) {
    //noop
  }
}

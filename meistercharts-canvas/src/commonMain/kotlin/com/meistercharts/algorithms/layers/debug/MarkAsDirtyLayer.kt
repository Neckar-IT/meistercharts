package com.meistercharts.algorithms.layers.debug

import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.Layer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType

/**
 * A special layer that marks the canvas support as dirty on every paint,
 * effectively enforcing repaints as fast as possible
 */
class MarkAsDirtyLayer : AbstractLayer() {
  override val type: LayerType
    get() = LayerType.Content

  override fun paint(paintingContext: LayerPaintingContext) {
    paintingContext.layerSupport.markAsDirty()
  }
}

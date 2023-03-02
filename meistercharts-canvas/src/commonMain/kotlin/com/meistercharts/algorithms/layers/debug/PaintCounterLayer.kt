package com.meistercharts.algorithms.layers.debug

import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.Layer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.model.Direction

/**
 * A layer that shows the paint count
 */
class PaintCounterLayer : AbstractLayer() {

  override val type: LayerType
    get() = LayerType.Notification

  /**
   * Contains the current paint count
   */
  var count: Int = 0

  override fun paint(paintingContext: LayerPaintingContext) {
    count++

    val gc = paintingContext.gc
    gc.stroke(Color.chocolate)
    gc.fillText("Paint count: $count", 10.0, 200.0, Direction.TopLeft)
  }
}

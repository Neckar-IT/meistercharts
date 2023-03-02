package com.meistercharts.canvas.mock

import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.canvas.ChartSupport
import com.meistercharts.canvas.DefaultLayerSupport
import com.meistercharts.canvas.MockCanvas
import com.meistercharts.canvas.PaintingLoopIndex
import it.neckar.open.unit.si.ms

object MockLayerPaintingContext {
  /**
   * Returns a new instance
   */
  operator fun invoke(frameTimestamp: @ms Double = 10.0, frameTimestampDelta: @ms Double = 0.0, loopIndex: PaintingLoopIndex = PaintingLoopIndex(0)): LayerPaintingContext {
    return LayerPaintingContext(MockCanvasRenderingContext(), DefaultLayerSupport(ChartSupport(MockCanvas())), frameTimestamp, frameTimestampDelta, loopIndex)
  }
}

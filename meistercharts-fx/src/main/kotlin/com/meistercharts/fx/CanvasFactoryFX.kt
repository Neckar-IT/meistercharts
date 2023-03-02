package com.meistercharts.fx

import com.meistercharts.algorithms.environment
import it.neckar.open.unit.number.MayBeZero
import com.meistercharts.annotations.PhysicalPixel
import com.meistercharts.canvas.Canvas
import com.meistercharts.canvas.CanvasFactory
import com.meistercharts.canvas.CanvasType
import com.meistercharts.model.Size

/**
 * A [CanvasFactory] that creates [Canvas] instances for JavaFx.
 */
class CanvasFactoryFX : CanvasFactory {
  override fun createCanvas(type: CanvasType, size: @MayBeZero Size): CanvasFX {
    return createCanvasWithPhysicalSize(size.times(environment.devicePixelRatio), type)
  }

  /**
   * Creates a canvas with the given physical size
   */
  override fun createCanvasWithPhysicalSize(physicalSize: @PhysicalPixel Size, type: CanvasType): CanvasFX {
    return CanvasFX(type = type).also {
      it.canvas.width = physicalSize.width
      it.canvas.height = physicalSize.height
    }
  }
}

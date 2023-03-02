package com.meistercharts.canvas

import it.neckar.open.unit.number.MayBeZero
import com.meistercharts.annotations.PhysicalPixel
import it.neckar.open.unit.number.Positive
import com.meistercharts.model.Size

/**
 * Creates [Canvas] instances
 */
interface CanvasFactory {
  /**
   * Creates a [Canvas] with the given (initial) [size] (*logical* pixels - *not* dependent on the device pixel ratio)
   */
  fun createCanvas(type: CanvasType, size: @MayBeZero Size = Size.zero): Canvas

  /**
   * Creates a canvas with the given physical size
   */
  fun createCanvasWithPhysicalSize(physicalSize: @PhysicalPixel @Positive Size, type: CanvasType): Canvas

  companion object {
    /**
     * Returns the canvas factory
     */
    fun get(): CanvasFactory {
      return meisterChartsFactory().canvasFactory
    }
  }
}

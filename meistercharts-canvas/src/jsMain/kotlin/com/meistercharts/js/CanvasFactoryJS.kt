package com.meistercharts.js

import com.meistercharts.algorithms.environment
import it.neckar.open.unit.number.MayBeZero
import com.meistercharts.annotations.PhysicalPixel
import it.neckar.open.unit.number.Positive
import com.meistercharts.canvas.Canvas
import com.meistercharts.canvas.CanvasFactory
import com.meistercharts.canvas.CanvasType
import com.meistercharts.model.Size

/**
 * A [CanvasFactory] that creates [Canvas] instances for Html.
 */
class CanvasFactoryJS : CanvasFactory {
  override fun createCanvas(type: CanvasType, size: @MayBeZero Size): CanvasJS {
    return CanvasJS(type)
      .also {
        it.applySize(size)

        it.canvasElement.style.width = "${size.width} px"
        it.canvasElement.style.height = "${size.height} px"

        it.canvasElement.style.margin = "0"
        it.canvasElement.style.padding = "0"
      }
  }

  override fun createCanvasWithPhysicalSize(physicalSize: @PhysicalPixel @Positive Size, type: CanvasType): Canvas {
    return createCanvas(type, physicalSize.divide(environment.devicePixelRatio))
  }
}

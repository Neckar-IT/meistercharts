package com.meistercharts.canvas

import com.meistercharts.canvas.mock.MockCanvasRenderingContext
import com.meistercharts.model.Size
import it.neckar.open.observable.ObservableObject
import it.neckar.open.observable.ReadOnlyObservableObject

/**
 * Mock implementation for a canvas
 */
class MockCanvas(
  override val gc: CanvasRenderingContext = MockCanvasRenderingContext(),
  type: CanvasType = CanvasType.Main
) : AbstractCanvas(type) {

  override val sizeProperty: ObservableObject<Size> = ObservableObject(Size.zero)
  override var size: Size by sizeProperty

  override val chartSizeClassificationProperty: ReadOnlyObservableObject<ChartSizeClassification> = ObservableObject(ChartSizeClassification.zero)
  override val chartSizeClassification: ChartSizeClassification by chartSizeClassificationProperty

  override fun takeSnapshot(): Image {
    return Image(object {}, size)
  }

  override val physicalWidth: Double
    get() = 0.0

  override val physicalHeight: Double
    get() = 0.0

  override fun dispose() = Unit
}

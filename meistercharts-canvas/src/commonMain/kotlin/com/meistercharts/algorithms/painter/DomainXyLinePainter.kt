package com.meistercharts.algorithms.painter

import com.meistercharts.algorithms.ChartCalculator
import com.meistercharts.annotations.DomainRelative
import com.meistercharts.annotations.TimeRelative
import com.meistercharts.canvas.CanvasRenderingContext

/**
 * Paints a xy line
 *
 */
@Deprecated("convert manually")
class DomainXyLinePainter(
  calculator: ChartCalculator,
  private val delegate: XYPainter
) : AbstractDomainRelativePainter(calculator, delegate.isSnapXValues, delegate.isSnapYValues) {

  constructor(
    calculator: ChartCalculator,
    gc: CanvasRenderingContext,
    snapXValues: Boolean,
    snapYValues: Boolean
  ) : this(calculator, FancyXyLinePainter(snapXValues, snapYValues))

  fun addPoint(gc: CanvasRenderingContext, @TimeRelative x: Double, @DomainRelative y: Double) {
    delegate.addCoordinate(
      gc,
      calculator.domainRelative2windowX(x),
      calculator.domainRelative2windowY(y)
    )
  }

  /**
   * Is called at the end of the path
   */
  fun finish(gc: CanvasRenderingContext) {
    delegate.finish(gc)
  }
}

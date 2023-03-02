package com.meistercharts.fx.time

import it.neckar.open.annotations.PaintContext
import com.meistercharts.algorithms.ValueRange
import com.meistercharts.algorithms.ZoomAndTranslationModifier
import com.meistercharts.algorithms.time.DataPoint
import com.meistercharts.annotations.Domain
import it.neckar.open.time.nowMillis
import javafx.scene.canvas.GraphicsContext

/**
 * Default implementation that provides a model
 *
 */
@Deprecated("use algorithms.module instead")
abstract class TimeDomainChartCanvas<T>
protected constructor(
  val model: TimeDiagramModel2<T>,
  @Domain domainValueRange: ValueRange,
  zoomAndPanModifier: ZoomAndTranslationModifier
) :
  BaseTimeDomainChartCanvas(
    model.createTimeRange(nowMillis()),
    domainValueRange,
    zoomAndPanModifier
  ) {

  init {
    model.addListener {
      markAsDirty()
      timeRange = model.createTimeRange(nowMillis())
    }
  }

  override fun paintDiagram(gc: GraphicsContext) {
    if (model.dataPoints.size < 2) {
      //we need at least two data points to paint
      return
    }

    paintCurves(gc, model.dataPoints)
  }

  /**
   * Paints the curves.
   * When this method is called the data points contains at least two elements
   */
  @PaintContext
  protected abstract fun paintCurves(gc: GraphicsContext, dataPoints: List<out @JvmWildcard DataPoint<T>>)
}

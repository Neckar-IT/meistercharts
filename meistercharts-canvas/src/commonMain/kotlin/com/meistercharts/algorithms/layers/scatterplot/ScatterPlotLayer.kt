package com.meistercharts.algorithms.layers.scatterplot

import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.linechart.PointStyle
import com.meistercharts.annotations.Domain
import com.meistercharts.annotations.Window
import com.meistercharts.canvas.CanvasRenderingContext
import com.meistercharts.canvas.StyleDsl
import com.meistercharts.design.corporateDesign
import com.meistercharts.painter.PointPainter
import com.meistercharts.painter.PointStylePainter
import com.meistercharts.provider.ValueRangeProvider
import it.neckar.open.provider.DoublesProvider
import it.neckar.open.provider.fastForEachIndexed

/**
 * The layer painting the scatter plot
 */
class ScatterPlotLayer(
  val data: Data,
  styleConfiguration: Style.() -> Unit = {}
) : AbstractLayer() {

  val style: Style = Style().also(styleConfiguration)

  override val type: LayerType
    get() = LayerType.Content

  override fun paint(paintingContext: LayerPaintingContext) {
    val gc = paintingContext.gc
    val chartCalculator = paintingContext.chartCalculator

    val valueRangeX = data.valueRangeXProvider()
    val valueRangeY = data.valueRangeYProvider()

    data.xValues.fastForEachIndexed { index, xValue: @Domain Double ->
      val yValue: @Domain Double = data.yValues.valueAt(index)

      val x = chartCalculator.domain2windowX(xValue, valueRangeX)
      val y = chartCalculator.domain2windowY(yValue, valueRangeY)

      style.pointPainter.paintPoint(gc, x, y)
    }
  }

  open class Data(
    /**
     * Provides the x values.
     * Must be of same size as [yValues]
     */
    var xValues: @Domain DoublesProvider,
    /**
     * Provides the y values
     * Must be of same size as [xValues]
     */
    var yValues: @Domain DoublesProvider,

    /**
     * Provides the value range for the x axis
     */
    val valueRangeXProvider: ValueRangeProvider,
    /**
     * Provides the value range for the y axis
     */
    val valueRangeYProvider: ValueRangeProvider
  )

  @StyleDsl
  class Style {
    /**
     * The point painter that is used to point the paints
     */
    var pointPainter: PointPainter = object : PointPainter {
      val delegate = PointStylePainter(PointStyle.Cross, 1.0, false, false)

      override fun paintPoint(gc: CanvasRenderingContext, x: @Window Double, y: @Window Double) {
        gc.stroke(
          if (x < gc.width / 2.0) {
            if (y < gc.height / 2.0) {
              corporateDesign.primaryColor
            } else {
              corporateDesign.secondaryColor
            }
          } else {
            if (y < gc.height / 2.0) {
              corporateDesign.primaryColorLighter
            } else {
              corporateDesign.primaryColorDarker
            }
          }
        )

        delegate.paintPoint(gc, x, y)
      }
    }
  }
}

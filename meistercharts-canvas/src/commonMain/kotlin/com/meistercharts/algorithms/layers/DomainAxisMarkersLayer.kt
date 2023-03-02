package com.meistercharts.algorithms.layers

import com.meistercharts.algorithms.painter.Color
import com.meistercharts.canvas.StyleDsl
import com.meistercharts.canvas.strokeCross
import com.meistercharts.provider.CoordinatesProvider

/**
 * Marks a coordinate on the domain axises by painting lines
 */
class DomainAxisMarkersLayer(
  private val coordinatesProvider: CoordinatesProvider,
  styleConfiguration: Style.() -> Unit = {}
) : AbstractLayer() {

  val style: Style = Style().also(styleConfiguration)

  override val type: LayerType
    get() = LayerType.Content

  override fun paint(paintingContext: LayerPaintingContext) {
    val gc = paintingContext.gc
    val chartCalculator = paintingContext.chartCalculator

    val coordinates = coordinatesProvider()

    val x = chartCalculator.domainRelative2windowX(coordinates.x)
    val y = chartCalculator.domainRelative2windowY(coordinates.y)

    gc.stroke(style.lineColor)
    gc.strokeLine(x, y, chartCalculator.contentAreaRelative2windowX(0.0), y)
    gc.strokeLine(x, y, x, chartCalculator.contentAreaRelative2windowY(1.0))

    if (style.paintMarker) {
      gc.strokeCross(x, y, 15.0)
    }
  }

  @StyleDsl
  class Style {
    /**
     * Color for the current position lines
     */
    var lineColor: Color = Color.web("#097CBC")

    /**
     * Whether to paint a marker at the Coordinate position
     */
    var paintMarker: Boolean = true
  }
}

package com.meistercharts.algorithms.layers

import com.meistercharts.algorithms.axis.AxisSelection
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.annotations.Window
import com.meistercharts.canvas.StyleDsl
import it.neckar.open.unit.other.px

/**
 * Paints (endless lines) at zero
 */
class ZeroLinesLayer(
  styleConfiguration: Style.() -> Unit = {}
) : AbstractLayer() {

  val style: Style = Style().also(styleConfiguration)

  override val type: LayerType
    get() = LayerType.Content

  override fun paint(paintingContext: LayerPaintingContext) {
    val gc = paintingContext.gc
    gc.lineWidth = style.lineWidth
    gc.stroke(style.color)

    val chartCalculator = paintingContext.chartCalculator
    if (style.axisToPaint.containsX) {
      @Window val y = chartCalculator.domainRelative2windowY(0.0)
      gc.strokeLine(0.0, y, gc.width, y)
    }

    if (style.axisToPaint.containsY) {
      @Window val x = chartCalculator.domainRelative2windowX(0.0)
      gc.strokeLine(x, 0.0, x, gc.height)
    }
  }

  @StyleDsl
  open class Style {
    /**
     * The color for the lines
     */
    var color: Color = Color.silver

    var lineWidth: @px Double = 1.0

    /**
     * Which axis to paint
     */
    var axisToPaint: AxisSelection = AxisSelection.Both
  }
}

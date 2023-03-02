package com.meistercharts.algorithms.layers.compass

import com.meistercharts.algorithms.ValueRange
import com.meistercharts.algorithms.domain2rad
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.canvas.StyleDsl
import com.meistercharts.model.Coordinates
import com.meistercharts.model.PolarCoordinates
import it.neckar.open.unit.other.px
import it.neckar.open.unit.si.rad

/**
 * Paints a simple line
 */
class LineValuePainter(
  styleConfiguration: Style.() -> Unit = {}
) : GaugeValuePainter {
  val style: Style = Style().also(styleConfiguration)

  override fun paintCurrentValue(gaugePaintable: GaugePaintable, paintingContext: LayerPaintingContext, radius: Double, startAngle: Double, extendWithRotationDirection: Double, valueRange: ValueRange, value: Double) {
    val gc = paintingContext.gc

    @rad val currentValueRad = domain2rad(value, valueRange, startAngle, extendWithRotationDirection)

    val end = PolarCoordinates((radius - style.lineGap).coerceAtLeast(0.0), currentValueRad).toCartesian()

    gc.lineWidth = style.pointerWidth
    gc.stroke(style.pointerColor)
    gc.strokeLine(Coordinates.origin, end)
  }

  @StyleDsl
  class Style {
    /**
     * The gap to the outline of the gauge
     */
    var lineGap: @px Double = 3.0

    /**
     * The pointer stroke width
     */
    var pointerWidth: @px Double = 3.0

    /**
     * The color to paint the pointer
     */
    var pointerColor: Color = Color.red
  }
}


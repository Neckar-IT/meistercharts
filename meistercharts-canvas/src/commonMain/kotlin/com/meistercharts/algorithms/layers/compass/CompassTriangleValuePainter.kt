package com.meistercharts.algorithms.layers.compass

import com.meistercharts.algorithms.ValueRange
import com.meistercharts.algorithms.domain2rad
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.canvas.LineJoin
import com.meistercharts.canvas.StyleDsl
import com.meistercharts.model.Coordinates
import com.meistercharts.model.PolarCoordinates
import it.neckar.open.unit.other.px
import it.neckar.open.unit.si.rad
import kotlin.math.PI

/**
 * Paints a compass needle
 */
class CompassTriangleValuePainter(
  styleConfiguration: Style.() -> Unit = {}
) : GaugeValuePainter {
  val style: Style = Style().also(styleConfiguration)

  override fun paintCurrentValue(gaugePaintable: GaugePaintable, paintingContext: LayerPaintingContext, radius: Double, startAngle: Double, extendWithRotationDirection: Double, valueRange: ValueRange, value: Double) {
    val gc = paintingContext.gc

    val currentValueRad: @rad Double = domain2rad(value, valueRange, startAngle, extendWithRotationDirection)

    //The outer point
    val radiusAtTip = (radius - style.gap).coerceAtLeast(0.0)
    val radiusAtBase = (radiusAtTip - style.height).coerceAtLeast(0.0)

    val tip = PolarCoordinates(radiusAtTip, currentValueRad).toCartesian()

    @rad val baseLeftAngle = currentValueRad - style.baseWidthRad / 2.0
    @rad val baseRightAngle = currentValueRad + style.baseWidthRad / 2.0

    val baseLeft = PolarCoordinates(radiusAtBase, baseLeftAngle).toCartesian()
    val baseRight = PolarCoordinates(radiusAtBase, baseRightAngle).toCartesian()


    gc.beginPath()
    gc.moveTo(baseLeft)
    gc.lineTo(tip)
    gc.lineTo(baseRight)

    gc.arcCenter(Coordinates.origin, radiusAtBase, baseRightAngle, baseLeftAngle - baseRightAngle)
    gc.closePath()

    gc.fill(style.fill)
    gc.fill()

    if (style.lineWidth > 0.0) {
      gc.lineWidth = style.lineWidth
      gc.lineJoin = LineJoin.Round
      gc.stroke(style.stroke)
      gc.stroke()
    }
  }

  @StyleDsl
  class Style {
    /**
     * The gap to the outline of the gauge
     */
    var gap: @px Double = 18.0

    /**
     * The height of the triangle
     */
    var height: @px Double = 42.0

    /**
     * The width of the base arrow in rad
     */
    var baseWidthRad: @rad Double = PI / 25.0

    var lineWidth: @px Double = 5.0

    var stroke: Color = Color("#185ba6")

    var fill: Color = Color.white
  }
}


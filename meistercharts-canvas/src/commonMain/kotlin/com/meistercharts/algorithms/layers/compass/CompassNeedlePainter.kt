package com.meistercharts.algorithms.layers.compass

import com.meistercharts.algorithms.ValueRange
import com.meistercharts.algorithms.domain2rad
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.canvas.StyleDsl
import com.meistercharts.model.PolarCoordinates
import it.neckar.open.unit.other.px
import it.neckar.open.unit.si.rad

/**
 * Paints a compass needle
 */
class CompassNeedlePainter(
  styleConfiguration: Style.() -> Unit = {}
) : GaugeValuePainter {
  val style: Style = Style().also(styleConfiguration)
  override fun paintCurrentValue(
    gaugePaintable: GaugePaintable, paintingContext: LayerPaintingContext,
    radius: Double, startAngle: Double, extendWithRotationDirection: Double, valueRange: ValueRange, value: Double
  ) {
    val gc = paintingContext.gc

    val currentValueRad: @rad Double = domain2rad(value, valueRange, startAngle, extendWithRotationDirection)

    //The outer point
    @px val radiusAtTip = (radius - style.gap(radius)).coerceAtLeast(0.0)

    val needleWidth = style.width(radius)
    val needleHeight = style.height(radius)
    val lowerArcRadius = style.lowerArcRadius(radius)

    val tip = PolarCoordinates(radiusAtTip, currentValueRad).toCartesian()

    //Move tip to the correct location
    gc.translate(tip.x, tip.y)

    //Rotate around the tip location
    gc.rotateRadians(currentValueRad - startAngle)

    //Paint the path
    gc.beginPath()
    gc.moveTo(0.0, 0.0)
    gc.arcTo(needleWidth * 0.5, needleHeight, 0.0, needleHeight, lowerArcRadius)
    gc.arcTo(-needleWidth * 0.5, needleHeight, 0.0, 0.0, lowerArcRadius)
    gc.closePath()

    gc.fill(style.fill)
    gc.fill()
  }

  @StyleDsl
  class Style {
    /**
     * The radius of the lower corners
     */
    var lowerArcRadius: (@px Double) -> @px Double = { radius -> if (radius > 250.0) 7.0 else 4.0 }

    /**
     * The gap to the outline of the gauge
     */
    var gap: (@px Double) -> @px Double = { 0.0 }

    /**
     * The height of the compass needle
     */
    var height: (@px Double) -> @px Double = { radius -> radius * 0.4 }

    /**
     * The width of the compass needle
     */
    var width: (@px Double) -> @px Double = { radius -> height(radius) * 0.33333 }

    /**
     * The color of the needle
     */
    var fill: Color = Color("rgba(0,161,229,0.7)")
  }
}


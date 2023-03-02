package com.meistercharts.algorithms.layers.compass

import com.meistercharts.algorithms.ValueRange
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.painter.Arrows
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.annotations.DomainRelative
import com.meistercharts.canvas.StyleDsl
import com.meistercharts.history.impl.MockSinusHistoryStorage.Companion.valueRange
import com.meistercharts.model.PolarCoordinates
import it.neckar.open.unit.si.rad
import kotlin.math.PI

/**
 * Paints an arrow
 */
class ArrowValuePainter(
  styleConfiguration: Style.() -> Unit = {}
) : GaugeValuePainter {
  val style: Style = Style().also(styleConfiguration)
  override fun paintCurrentValue(gaugePaintable: GaugePaintable, paintingContext: LayerPaintingContext, radius: Double, startAngle: Double, extendWithRotationDirection: Double, valueRange: ValueRange, value: Double) {
    val gc = paintingContext.gc

    @DomainRelative val domainRelative = valueRange.toDomainRelative(value)

    @rad val currentValue = (startAngle + domainRelative * extendWithRotationDirection)
    gc.lineWidth = style.pointerLineWidth
    gc.stroke(style.pointerColor)

    val end = PolarCoordinates(radius, currentValue).toCartesian()

    gc.translate(end.x, end.y)
    gc.rotateRadians(currentValue + PI / 2.0)
    gc.stroke(Arrows.toTop(radius, 20.0, 10.0))
  }

  @StyleDsl
  class Style {
    /**
     * The pointer stroke width
     */
    var pointerLineWidth: Double = 3.0

    /**
     * The color to paint the pointer
     */
    var pointerColor: Color = Color.red
  }
}

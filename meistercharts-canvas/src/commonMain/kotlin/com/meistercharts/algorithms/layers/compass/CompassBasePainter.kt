package com.meistercharts.algorithms.layers.compass

import com.meistercharts.algorithms.ValueRange
import com.meistercharts.algorithms.axis.AxisEndConfiguration
import com.meistercharts.algorithms.axis.IntermediateValuesMode
import com.meistercharts.algorithms.axis.LinearAxisTickCalculator
import com.meistercharts.algorithms.axis.LinearAxisTickCalculator.calculateTickValues
import com.meistercharts.algorithms.domain2rad
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.annotations.Domain
import com.meistercharts.canvas.ArcType
import com.meistercharts.canvas.FontDescriptorFragment
import com.meistercharts.canvas.StyleDsl
import com.meistercharts.model.Coordinates
import com.meistercharts.model.Direction
import com.meistercharts.model.PolarCoordinates
import it.neckar.open.formatting.CachedNumberFormat
import it.neckar.open.formatting.decimalFormat
import it.neckar.open.unit.si.rad
import kotlin.math.PI

/**
 * Paints a class compass base
 */
class CompassBasePainter(
  styleConfiguration: Style.() -> Unit = {}
) : GaugeBasePainter {

  val style: Style = Style().also(styleConfiguration)

  override fun paintBase(gaugePaintable: GaugePaintable, paintingContext: LayerPaintingContext, radius: Double, startAngle: Double, extendWithRotationDirection: Double, valueRange: ValueRange) {
    val gc = paintingContext.gc

    gc.fill(style.backgroundColor)
    gc.fillArcCenter(0.0, 0.0, radius, startAngle, extendWithRotationDirection, ArcType.Round)

    gc.stroke(style.compassColor)
    gc.strokeArcCenter(0.0, 0.0, radius, startAngle, extendWithRotationDirection, ArcType.Open)

    // paint inner circles
    gc.stroke(style.tickColor)
    for (circle in 1..style.numberInnerCircles) {
      gc.strokeArcCenter(0.0, 0.0, (radius / (style.numberInnerCircles + 1)) * circle, startAngle, extendWithRotationDirection, ArcType.Open)
    }

    // paint ticks
    gc.fill(style.labelsColor)
    gc.font(style.font)

    style.ticksProvider.calculateTicks(valueRange).forEach { tickValue: Double ->
      @rad val tickAngle = domain2rad(tickValue, valueRange, startAngle, extendWithRotationDirection)
      gc.strokeLine(
        Coordinates.origin.x,
        Coordinates.origin.y,
        PolarCoordinates.toCartesianX(radius, tickAngle),
        PolarCoordinates.toCartesianY(radius, tickAngle)
      )

      // paint tick label
      gc.fillText(
        style.valueFormat.format(tickValue),
        PolarCoordinates.toCartesianX(radius + style.labelsGap, tickAngle),
        PolarCoordinates.toCartesianY(radius + style.labelsGap, tickAngle),
        Direction.Center
      )
    }

    // check whether to draw start and end lines
    if (gaugePaintable.style.extend != PI * 2) {
      gc.stroke(style.compassColor)
      gc.strokeLine(
        Coordinates.origin.x,
        Coordinates.origin.y,
        PolarCoordinates.toCartesianX(radius, startAngle),
        PolarCoordinates.toCartesianY(radius, startAngle)
      )
      gc.strokeLine(
        Coordinates.origin.x,
        Coordinates.origin.y,
        PolarCoordinates.toCartesianX(radius, startAngle + extendWithRotationDirection),
        PolarCoordinates.toCartesianY(radius, startAngle + extendWithRotationDirection)
      )
    }
  }

  @StyleDsl
  class Style {
    /**
     * Provides the ticks
     */
    val ticksProvider: GaugeTicksProvider = AutoGaugeTicksProvider()

    /**
     * The color to paint the background
     */
    var backgroundColor: Color = Color.color(1.0, 1.0, 1.0, 0.0)

    /**
     * The color to paint the compass frame
     */
    var compassColor: Color = Color.black

    /**
     * The color to paint the ticks
     */
    var tickColor: Color = Color.lightgray

    /**
     * The color to paint the tick labels
     */
    var labelsColor: Color = Color.blue

    /**
     * The gap between the compass rose and the labels
     */
    var labelsGap: Double = 25.0

    /**
     * Number of inner circles to draw
     */
    var numberInnerCircles: Int = 3

    /**
     * Font used for values
     */
    var font: FontDescriptorFragment = FontDescriptorFragment.empty

    /**
     * Format for formatting values
     */
    var valueFormat: CachedNumberFormat = decimalFormat

  }
}

/**
 * Calculates ticks for the gauge
 */
fun interface GaugeTicksProvider {
  fun calculateTicks(valueRange: ValueRange): @Domain DoubleArray
}

/**
 * Calculates ticks using the [LinearAxisTickCalculator]]
 */
class AutoGaugeTicksProvider(val tickCount: Int = 20) : GaugeTicksProvider {
  override fun calculateTicks(valueRange: ValueRange): @Domain DoubleArray {
    return calculateTickValues(valueRange.start, valueRange.end, AxisEndConfiguration.Default, tickCount, 0.0, IntermediateValuesMode.Also5and2)
  }
}

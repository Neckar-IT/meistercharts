package com.meistercharts.fx.painter.lane.opacity

import com.meistercharts.algorithms.ChartCalculator
import com.meistercharts.algorithms.ChartingUtils
import com.meistercharts.algorithms.ValueRange
import com.meistercharts.algorithms.painter.AbstractPainter
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.annotations.Domain
import com.meistercharts.annotations.DomainRelative
import com.meistercharts.annotations.Window
import com.meistercharts.canvas.CanvasRenderingContext
import com.meistercharts.fx.painter.lane.LanesInformation
import it.neckar.open.unit.other.pct
import it.neckar.open.unit.other.px

/**
 * Paints an opacity diagram.
 * The x axis is interpreted from 0...100
 *
 */
class OpacityPainter(
  val calculator: ChartCalculator,
  val domainValueRange: ValueRange,
  snapXValues: Boolean,
  snapYValues: Boolean
) : AbstractPainter(snapXValues, snapYValues) {

  var stroke: Color = Color.darkgray

  /**
   * Used for the horizontal line to the right where the labels are
   */
  var labelLineStroke: Color? = Color.darkgray

  @px
  var lineWidth: Double = 1.0

  /**
   * Paints the opacity
   */
  fun paint(gc: CanvasRenderingContext, lanes: List<out @JvmWildcard LanesInformation.Lane>) {
    gc.beginPath()

    for (lane in lanes) {
      @Domain @pct val brightness = lane.brightness
      @Window @px val x = ChartingUtils.lineWithin(snapXPosition(calculator.domainRelative2windowX(brightness)), 0.0, calculator.chartState.contentAreaWidth, lineWidth)

      @DomainRelative val lower = domainValueRange.toDomainRelative(lane.lower)
      @DomainRelative val upper = domainValueRange.toDomainRelative(lane.upper)

      @Window val yLower = snapYPosition(calculator.domainRelative2windowY(lower))
      @Window val yUpper = snapYPosition(calculator.domainRelative2windowY(upper))

      gc.lineTo(x, yLower)
      gc.lineTo(x, yUpper)

      //draw the horizontal lines
      labelLineStroke?.let {
        gc.lineWidth = 1.0
        gc.stroke(it)
        gc.strokeLine(x, yUpper, gc.width, yUpper)
      }
    }

    gc.lineWidth = lineWidth
    gc.stroke(stroke)
    gc.stroke()

    //Clear the path
    gc.beginPath()
  }
}

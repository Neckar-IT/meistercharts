package com.meistercharts.algorithms.painter

import it.neckar.open.unit.other.px


/**
 * Abstract base class for xy line painters
 *
 */
abstract class AbstractXyLinePainter
protected constructor(
  snapXValues: Boolean,
  snapYValues: Boolean
) : AbstractPainter(snapXValues, snapYValues), XYPainter {
  /**
   * The stroke color for the line
   */
  var stroke: Color = Color.black
  /**
   * The shadow color
   */
  var shadow: Color? = null

  @px
  var lineWidth: Double = 1.0
}

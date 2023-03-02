package com.meistercharts.canvas

import it.neckar.open.unit.other.pct

/**
 * Represents a line spacing
 */
class LineSpacing(@pct val percentage: Double) {

  /**
   * Returns the percentage of the spacing between the lines
   */
  @pct
  val spacePercentage: Double
    get() = percentage - 1.0


  companion object {
    /**
     * "Single" line spacing - 115%
     */
    val Single: LineSpacing = LineSpacing(1.15)
    val OneAndHalf: LineSpacing = LineSpacing(1.5)
    val Double: LineSpacing = LineSpacing(2.00)
    val Triple: LineSpacing = LineSpacing(3.00)

    /**
     * Available default values
     */
    val available: List<LineSpacing> = listOf(Single, OneAndHalf, Double, Triple)
  }
}

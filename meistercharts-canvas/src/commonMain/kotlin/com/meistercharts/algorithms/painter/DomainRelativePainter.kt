package com.meistercharts.algorithms.painter

import com.meistercharts.algorithms.ChartCalculator

/**
 * A DomainRelativePainter contains a [ChartCalculator].
 * Implementations are able to paint data that is not converted to the [com.meistercharts.algorithms.Window]
 * system but instead provided as [com.meistercharts.algorithms.Domain] values.
 *
 */
@Deprecated("Do these conversions in a layer")
interface DomainRelativePainter : Painter {
  /**
   * Returns the chart calculator
   */
  val calculator: ChartCalculator
}

package com.meistercharts.algorithms.painter

import com.meistercharts.algorithms.ChartCalculator

/**
 * Abstract base class for painters. Implementing painter work with domain relative values
 *
 */
@Deprecated("Do not use - convert to layer")
abstract class AbstractDomainRelativePainter
protected constructor(
  override val calculator: ChartCalculator,
  snapXValues: Boolean,
  snapYValues: Boolean
) : AbstractPainter(snapXValues, snapYValues), DomainRelativePainter

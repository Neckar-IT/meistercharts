package com.meistercharts.painter

import com.meistercharts.algorithms.painter.AbstractPainter

/**
 * Paints lines
 */
abstract class AbstractLinePainter(
  snapXValues: Boolean,
  snapYValues: Boolean
) : AbstractPainter(snapXValues, snapYValues), LinePainter



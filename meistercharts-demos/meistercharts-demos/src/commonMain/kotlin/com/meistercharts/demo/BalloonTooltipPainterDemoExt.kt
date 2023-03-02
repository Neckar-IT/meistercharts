package com.meistercharts.demo

import com.meistercharts.algorithms.tooltip.balloon.AbsoluteNosePositionCalculator
import com.meistercharts.algorithms.tooltip.balloon.BalloonTooltipPaintable
import com.meistercharts.model.Direction


/**
 * Adds methods to configure the nose positions
 */
@DemoDeclaration
fun ChartingDemo.configurableNosePosition(tooltipPainter: BalloonTooltipPaintable) {
  configurableDouble("Relative Nose Position", 0.5, false) {
    onChange {
      tooltipPainter.configuration.relativeNosePosition(it)
      markAsDirty()
    }
  }

  configurableDouble("Absolute Nose Position", 10.0, false) {
    max = 100.0

    onChange {
      val old = tooltipPainter.configuration.nosePositionCalculator
      val oldValue = (old as? AbsoluteNosePositionCalculator)?.direction ?: Direction.TopLeft

      tooltipPainter.configuration.absoluteNosePosition(it, oldValue)
      markAsDirty()
    }
  }

  configurableEnum("Absolute Nose Pos Dir", Direction.TopLeft) {
    onChange {
      val old = tooltipPainter.configuration.nosePositionCalculator
      val oldValue = (old as? AbsoluteNosePositionCalculator)?.absoluteNosePosition ?: 10.0

      tooltipPainter.configuration.absoluteNosePosition(oldValue, it)
      markAsDirty()
    }
  }

}

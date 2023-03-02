package com.meistercharts.algorithms.layers.barchart

import com.meistercharts.algorithms.ChartCalculator
import com.meistercharts.provider.BoxProvider1

/**
 * Provides the content area
 */
object ContentAreaBoxProvider : BoxProvider1<ChartCalculator> {
  override fun getX(param0: ChartCalculator): Double {
    return param0.contentAreaRelative2windowX(0.0)
  }

  override fun getY(param0: ChartCalculator): Double {
    return param0.contentAreaRelative2windowY(0.0)
  }

  override fun getWidth(param0: ChartCalculator): Double {
    return param0.contentAreaRelative2zoomedX(1.0)
  }

  override fun getHeight(param0: ChartCalculator): Double {
    return param0.contentAreaRelative2zoomedY(1.0)
  }
}

/**
 * Provides the window bounds
 */
object WindowBoxProvider : BoxProvider1<ChartCalculator> {
  override fun getX(param0: ChartCalculator): Double {
    return 0.0
  }

  override fun getY(param0: ChartCalculator): Double {
    return 0.0
  }

  override fun getWidth(param0: ChartCalculator): Double {
    return param0.chartState.windowWidth
  }

  override fun getHeight(param0: ChartCalculator): Double {
    return param0.chartState.windowHeight
  }
}

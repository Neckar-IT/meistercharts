package com.meistercharts.fx

import com.meistercharts.canvas.CanvasFactory
import com.meistercharts.canvas.ChartSupport
import com.meistercharts.canvas.MeisterChartsFactory

/**
 * JavaFX specific factory
 *
 */
class MeisterChartsFactoryFX : MeisterChartsFactory {
  override val canvasFactory: CanvasFactory = CanvasFactoryFX()

  override fun createChart(chartSupport: ChartSupport, description: String): MeisterChartFX {
    return MeisterChartFX(chartSupport, description)
  }
}

package com.meistercharts.js

import com.meistercharts.canvas.CanvasFactory
import com.meistercharts.canvas.ChartSupport
import com.meistercharts.canvas.MeisterChartsFactory

/**
 * Html/JS specific factories for chart
 *
 */
class MeisterChartsFactoryJS : MeisterChartsFactory {
  override val canvasFactory: CanvasFactory = CanvasFactoryJS()

  override fun createChart(chartSupport: ChartSupport, description: String): MeisterChartJS {
    return MeisterChartJS(chartSupport, description)
  }
}

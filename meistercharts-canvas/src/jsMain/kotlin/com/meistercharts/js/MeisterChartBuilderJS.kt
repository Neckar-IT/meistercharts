package com.meistercharts.js

import com.meistercharts.canvas.MeisterChartBuilder
import com.meistercharts.charts.ChartId

/**
 * A builder for [MeisterChartJS]
 */
class MeisterChartBuilderJS(
  description: String,
  chartId: ChartId = ChartId.next(),
) : MeisterChartBuilder(description, chartId = chartId) {
  init {
    //Enforce a repaint on resize
    //The HTML5 canvas is cleared on resize - therefore a repaint is necessary to avoid flickering to white
    configure {
      chartSupport.canvas.sizeProperty.consume {
        chartSupport.markAsDirty()
      }
    }
  }

  override fun build(): MeisterChartJS {
    return (super.build() as MeisterChartJS)
  }

  companion object {
    /**
     * Creates a new chart builder - calls <MeisterChartsPlatform.init()>
     */
    fun create(description: String): MeisterChartBuilderJS {
      MeisterChartsPlatform.init()
      return MeisterChartBuilderJS(description)
    }
  }
}

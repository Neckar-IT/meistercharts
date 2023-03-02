package com.meistercharts.demo.descriptors

import com.meistercharts.algorithms.layers.circular.FixedPixelsGap
import com.meistercharts.charts.CircularChartGestalt
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration

/**
 */
class CircularChartGestaltSickDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Circular Chart (SICK)"

  //language=HTML
  override val description: String = "## Circular Chart - design by SICK AG"
  override val category: DemoCategory = DemoCategory.Automation

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {
        val circularChartGestalt = CircularChartGestalt().apply {
          layer.style.outerCircleWidth = 6.0
          layer.style.gapInnerOuter = 6.0
          layer.style.innerCircleWidth = 19.0
          layer.style.outerCircleValueGap = FixedPixelsGap(2.0)
        }
        circularChartGestalt.configure(this)
      }
    }
  }
}

package com.meistercharts.demo.descriptors

import com.meistercharts.charts.CircularChartGestalt
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration

/**
 */
class CircularChartGestaltDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Circular Chart Gestalt"

  //language=HTML
  override val description: String = "## Circular Chart"
  override val category: DemoCategory = DemoCategory.Gestalt

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {
        val circularChartGestalt = CircularChartGestalt()
        circularChartGestalt.configure(this)
      }
    }
  }
}

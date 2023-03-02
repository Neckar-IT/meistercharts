package com.meistercharts.demo.descriptors

import com.meistercharts.charts.ScatterPlotGestalt
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableInsetsSeparate

class ScatterPlotGestaltDemoDescriptor : ChartingDemoDescriptor<Nothing> {

  override val name: String = "Scatter Plot Gestalt"
  override val description: String = "Scatter Plot Gestalt"
  override val category: DemoCategory = DemoCategory.Gestalt

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {
        val gestalt = ScatterPlotGestalt()
        gestalt.configure(this)

        configurableInsetsSeparate("Margin", gestalt.style.marginProperty) {
          onChange {
            markAsDirty()
          }
        }
      }
    }
  }
}

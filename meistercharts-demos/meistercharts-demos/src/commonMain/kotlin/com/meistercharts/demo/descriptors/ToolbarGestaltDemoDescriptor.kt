package com.meistercharts.demo.descriptors

import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.charts.ToolbarGestalt
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration

/**
 *
 */
class ToolbarGestaltDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Toolbar Gestalt"

  override val category: DemoCategory = DemoCategory.Gestalt

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {

      meistercharts {
        ToolbarGestalt().configure(this)

        configure {
          layers.addClearBackground()
        }
      }
    }
  }
}

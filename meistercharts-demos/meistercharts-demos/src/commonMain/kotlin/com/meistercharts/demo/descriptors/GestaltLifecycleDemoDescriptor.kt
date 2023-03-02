package com.meistercharts.demo.descriptors

import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.text.TextLayer
import com.meistercharts.charts.AbstractChartGestalt
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration

/**
 */
class GestaltLifecycleDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Gestalt Life cycle"

  override val category: DemoCategory = DemoCategory.Calculations

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {
        MyDemoGestalt().configure(this)
      }
    }
  }

  class MyDemoGestalt : AbstractChartGestalt() {
    init {
      configure {
        layers.addClearBackground()

        layers.addLayer(TextLayer({ textService, i18nConfiguration ->
          val gestalt = this@MyDemoGestalt
          val gestaltChartSupport = gestalt.chartSupport()

          listOf("Gestalt Chart Support: $gestaltChartSupport")
        }))
      }
    }
  }
}

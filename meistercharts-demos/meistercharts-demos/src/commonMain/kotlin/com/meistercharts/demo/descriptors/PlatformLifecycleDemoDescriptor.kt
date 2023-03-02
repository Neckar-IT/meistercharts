package com.meistercharts.demo.descriptors

import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.text.addText
import com.meistercharts.canvas.MeisterChartsPlatformState
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration


class PlatformLifecycleDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Platform Lifecycle"
  override val description: String = "Shows the active instances of MeisterCharts as registered at MeisterChartsPlatformState"
  override val category: DemoCategory = DemoCategory.LowLevelTests

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {
        configure {
          layers.addClearBackground()
          layers.addText({ _, _ ->
            buildList {
              val activeInstances = MeisterChartsPlatformState.activeInstances()
              add("${activeInstances.size} MeisterCharts Instances:")
              addAll(activeInstances.map { "\t${it.description}" })
            }
          })
        }
      }
    }
  }
}

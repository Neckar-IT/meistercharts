package com.meistercharts.demo.descriptors

import com.meistercharts.charts.BinaryGestalt
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.Importance
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableInsetsSeparate

class BinaryGestaltDemoDescriptor : ChartingDemoDescriptor<Nothing> {

  override val name: String = "Binary Gestalt"

  //language=HTML
  override val description: String = "Binary Gestalt"
  override val category: DemoCategory = DemoCategory.Other
  override val importance: Importance = Importance.Deprecated

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {
        val gestalt = BinaryGestalt()
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

package com.meistercharts.demo.descriptors

import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.text.addTextUnresolved
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration

/**
 * A simple hello world demo.
 *
 * Can be used as template to create new demos
 */
class HelloWorldDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Hello World"

  //language=HTML
  override val description: String = "<h3>A simple Hello World demo</h3><p>Shows just a Hello World text above a white background</p>"
  override val category: DemoCategory = DemoCategory.Primitives

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {
        configure {
          layers.addClearBackground()
          layers.addTextUnresolved("Hello World", Color.darkorange)
        }
      }
    }
  }
}

package com.meistercharts.demojs.descriptors

import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.text.addText
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import it.neckar.open.formatting.decimalFormat2digits
import kotlinx.browser.window

/**
 */
class MemoryUsageDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "JS Memory Usage"
  override val category: DemoCategory = DemoCategory.Platform

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {
        configure {
          layers.addClearBackground()

          layers.addText { _, _ ->
            val performance = window.performance

            try {
              val memory = js("window.performance.memory.usedJSHeapSize") //in bytes

              return@addText listOf(
                "Memory Usage",
                "used JS heap: ${decimalFormat2digits.format(memory / 1024 / 1024.0)} MiB"
              )

            } catch (e: Throwable) {
              println("e: $e")
              println("class: ${e::class}")
              return@addText listOf(
                "Not supported by browser",
                e.message.toString()
              )
            }

          }
        }
      }
    }
  }
}

package com.meistercharts.demo.descriptors.benchmark

import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration

/**
 * Benchmark of various painting operations
 */
class BenchmarkDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Benchmark / Performance"
  override val description: String = "Benchmark of various painting operations"
  override val category = DemoCategory.Other

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {

        configure {
          layers.addClearBackground()
          layers.addLayer(BenchmarkLayer())
        }
      }
    }
  }
}


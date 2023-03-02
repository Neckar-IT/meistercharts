package com.meistercharts.demo.descriptors

import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.text.addText
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.canvas.FontDescriptorFragment
import com.meistercharts.canvas.devicePixelRatio
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import it.neckar.open.formatting.decimalFormat
import com.meistercharts.style.BoxStyle

class PhysicalCanvasSizeDemo : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Physical canvas size demo"
  override val category: DemoCategory = DemoCategory.Calculations

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {
        configure {
          layers.addClearBackground()

          layers.addText({ _, _ ->
            listOf(
              "devicePixelRatio: ${chartSupport.devicePixelRatio}",
              "Physical size: ${decimalFormat.format(chartSupport.canvas.physicalWidth)} / ${decimalFormat.format(chartSupport.canvas.physicalHeight)}",
              "Logical size: ${decimalFormat.format(chartSupport.canvas.width)} / ${decimalFormat.format(chartSupport.canvas.height)}"
            )
          }) {
            boxStyle = BoxStyle(fill = Color.rgba(255, 255, 255, 0.9), borderColor = Color.gray)
            font = FontDescriptorFragment.DefaultSize
          }
        }
      }
    }
  }
}

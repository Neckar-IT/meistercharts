package com.meistercharts.demojs.descriptors

import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.text.addTextUnresolved
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.js.CanvasJS
import org.w3c.dom.HTMLCanvasElement

class ZeroSizeDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Zero Size"
  override val description: String = "## What happens if the canvas has no size"
  override val category: DemoCategory = DemoCategory.Platform

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {
        configure {
          layers.addClearBackground()
          layers.addTextUnresolved("Hello World", Color.darkorange)

          declare {

            fun getCanvasElement(): HTMLCanvasElement {
              return (chartSupport.canvas as CanvasJS).canvasElement
            }

            button("hidden=\"hidden\"") {
              println("hidden=\"hidden\"")
              getCanvasElement().setAttribute("hidden", "hidden");
              markAsDirty()
            }

            button("width:0;height:0") {
              println("width:0;height:0")
              getCanvasElement().apply {
                style.width = "0 px"
                style.height = "0 px"
              }
              markAsDirty()
            }

            button("display:none") {
              println("display:none")
              getCanvasElement().style.display = "none"
              markAsDirty()
            }

            button("visibility:hidden") {
              println("visibility:hidden")
              getCanvasElement().style.visibility = "hidden"
              markAsDirty()
            }
          }
        }
      }
    }

  }
}

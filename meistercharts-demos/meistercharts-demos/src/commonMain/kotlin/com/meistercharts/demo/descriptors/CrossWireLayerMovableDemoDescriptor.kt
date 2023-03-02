package com.meistercharts.demo.descriptors

import com.meistercharts.algorithms.layers.crosswire.movable
import com.meistercharts.canvas.mouseCursorSupport
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.configurableDouble

/**
 * How a movable cross wire could be set up
 */
class CrossWireLayerMovableDemoDescriptor : CrossWireLayerDemoDescriptor() {
  override val name: String = "Cross wire layer - movable"

  //language=HTML
  override val description: String = "## Cross wire layer that can be moved"
  override val category: DemoCategory = DemoCategory.Layers

  override fun configureDemo(chartingDemo: ChartingDemo) {
    super.configureDemo(chartingDemo)

    with(chartingDemo) {
      meistercharts {
        configure {
          val movable = crossWireLayer.movable(chartSupport.mouseCursorSupport, chartSupport.mouseEvents)
          layers.addLayer(movable)

          configurableDouble("padding bottom", movable.style::paddingBottom) {
            min = 0.0
            max = 50.0
          }
        }
      }
    }
  }

}

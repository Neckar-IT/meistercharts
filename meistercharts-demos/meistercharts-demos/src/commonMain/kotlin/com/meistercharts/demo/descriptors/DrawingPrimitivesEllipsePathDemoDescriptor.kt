package com.meistercharts.demo.descriptors

import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.canvas.strokeCross
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableDouble

/**
 *
 */
class DrawingPrimitivesEllipsePathDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Drawing Primitives: Ellipse Center Path"

  //language=HTML
  override val description: String = "## shows how to add arcs to a path"
  override val category: DemoCategory = DemoCategory.Primitives

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {

    return ChartingDemo {
      meistercharts {
        configure {
          layers.addClearBackground()


          val layer = object : AbstractLayer() {
            override val type: LayerType
              get() = LayerType.Content

            var width = 100.0
            var height = 140.0

            override fun paint(paintingContext: LayerPaintingContext) {
              val gc = paintingContext.gc

              val x = gc.center.x
              val y = gc.center.y

              gc.beginPath()
              gc.ovalCenter(x, y, width, height)
              gc.stroke(Color.blue)
              gc.stroke()

              gc.stroke(Color.silver)
              gc.strokeCross(x, y, 5.0)
            }
          }
          layers.addLayer(
            layer
          )

          configurableDouble("width", layer::width) {
            max = 800.0
          }

          configurableDouble("height", layer::height) {
            max = 800.0
          }
        }
      }
    }
  }
}

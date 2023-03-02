package com.meistercharts.demo.descriptors

import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.canvas.CanvasRenderingContext
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.section
import com.meistercharts.model.Direction
import it.neckar.open.formatting.decimalFormat

class PaintTriangleDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Triangles"
  override val category: DemoCategory = DemoCategory.Primitives

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {
        configure {
          layers.addClearBackground()
          val layer = object : AbstractLayer() {
            override val type: LayerType = LayerType.Content

            var x0: Double = 150.0
            var x1: Double = 100.0
            var x2: Double = 200.0

            var y0: Double = 320.0
            var y1: Double = 150.0
            var y2: Double = 250.0

            var lineWidth: Double = 1.0

            override fun paint(paintingContext: LayerPaintingContext) {
              val gc = paintingContext.gc
              gc.lineWidth = lineWidth
              gc.strokeTriangle(x0, y0, x1, y1, x2, y2)

              gc.lineWidth = 1.0
              markPoint(gc, 0)
              markPoint(gc, 1)
              markPoint(gc, 2)
            }

            private fun markPoint(gc: CanvasRenderingContext, index: Int) {
              val x = when (index) {
                0 -> x0
                1 -> x1
                2 -> x2
                else -> throw IllegalArgumentException("Invalid index: $index")
              }
              val y = when (index) {
                0 -> y0
                1 -> y1
                2 -> y2
                else -> throw IllegalArgumentException("Invalid index: $index")
              }

              gc.fill(Color.orange)
              gc.fillOvalCenter(x, y, 4.0)
              gc.fill(Color.darkgray)
              gc.fillText("$index: ${decimalFormat.format(x)}/${decimalFormat.format(x)}", x, y, Direction.TopLeft, 5.0, 5.0)
            }
          }

          layers.addLayer(layer)

          section("Simple Triangle")

          configurableDouble("x0", layer::x0) {
            max = 500.0
          }
          configurableDouble("x1", layer::x1) {
            max = 500.0
          }
          configurableDouble("x2", layer::x2) {
            max = 500.0
          }

          configurableDouble("y0", layer::y0) {
            max = 500.0
          }
          configurableDouble("y1", layer::y1) {
            max = 500.0
          }
          configurableDouble("y2", layer::y2) {
            max = 500.0
          }

          configurableDouble("Line Width", layer::lineWidth) {
            max = 10.0
          }
        }
      }
    }
  }
}

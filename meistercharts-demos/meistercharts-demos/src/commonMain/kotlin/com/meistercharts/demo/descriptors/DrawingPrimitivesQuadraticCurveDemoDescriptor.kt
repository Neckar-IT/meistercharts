package com.meistercharts.demo.descriptors

import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.canvas.strokeCross
import com.meistercharts.canvas.strokeCross45Degrees
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableDouble

/**
 *
 */
class DrawingPrimitivesQuadraticCurveDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Drawing Primitives: Quadratic Curves"

  //language=HTML
  override val description: String = "## shows how to draw a quadratic curves"
  override val category: DemoCategory = DemoCategory.Primitives

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {

    return ChartingDemo {
      meistercharts {
        configure {
          layers.addClearBackground()

          var x1 = 30.0
          var y1 = 30.0
          var x2 = 220.0
          var y2 = 140.0

          var controlX1 = 120.0
          var controlY1 = 160.0


          layers.addLayer(object : AbstractLayer() {
            override val type: LayerType
              get() = LayerType.Content

            override fun paint(paintingContext: LayerPaintingContext) {
              val gc = paintingContext.gc
              gc.beginPath()

              gc.moveTo(x1, y1)
              gc.quadraticCurveTo(controlX1, controlY1, x2, y2)

              gc.stroke(Color.orangered)
              gc.stroke()

              gc.lineWidth = 1.0
              gc.strokeCross(x1, y1, 5.0)
              gc.strokeCross(x2, y2, 5.0)
              gc.strokeCross45Degrees(controlX1, controlY1, 5.0)

              gc.stroke(Color.gray)
              gc.strokeLine(x1, y1, controlX1, controlY1)
              gc.strokeLine(x2, y2, controlX1, controlY1)
            }
          }
          )

          configurableDouble("x1", x1) {
            max = 800.0
            onChange {
              x1 = it
              markAsDirty()
            }
          }
          configurableDouble("y1", y1) {
            max = 800.0
            onChange {
              y1 = it
              markAsDirty()
            }
          }

          configurableDouble("controlX1", controlX1) {
            max = 800.0
            onChange {
              controlX1 = it
              markAsDirty()
            }
          }
          configurableDouble("controlY1", controlY1) {
            max = 800.0
            onChange {
              controlY1 = it
              markAsDirty()
            }
          }

          configurableDouble("x2", x2) {
            max = 800.0
            onChange {
              x2 = it
              markAsDirty()
            }
          }
          configurableDouble("y2", y2) {
            max = 800.0
            onChange {
              y2 = it
              markAsDirty()
            }
          }
        }
      }
    }
  }
}

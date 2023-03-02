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
import com.meistercharts.demo.configurableEnum

class DrawingPrimitivesPointTypesDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Drawing Primitives: Dot types"

  //language=HTML
  override val description: String = "## shows how to draw dot types"
  override val category: DemoCategory = DemoCategory.Primitives

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {

    return ChartingDemo {
      meistercharts {
        configure {
          layers.addClearBackground()


          var size = 50.0
          var lineWidth = 5.0
          var dotType = DotType.Cross

          layers.addLayer(object : AbstractLayer() {
            override val type: LayerType
              get() = LayerType.Content

            override fun paint(paintingContext: LayerPaintingContext) {
              val gc = paintingContext.gc
              gc.stroke(Color.lightgray)

              val halfHeight = gc.height / 2.0
              gc.strokeLine(0.0, halfHeight, gc.width, halfHeight)

              val halfWidth = gc.width / 2.0
              gc.strokeLine(halfWidth, 0.0, halfWidth, gc.height)

              gc.stroke(Color.orangered)
              gc.fill(Color.orangered)
              gc.lineWidth = lineWidth
              when (dotType) {
                DotType.Cross          -> {
                  gc.strokeCross(halfWidth, halfHeight, size)
                }
                DotType.Cross45Degrees -> {
                  gc.strokeCross45Degrees(halfWidth, halfHeight, size)
                }
                DotType.Dot            -> {
                  gc.fillOvalCenter(halfWidth, halfHeight, size / 2.0, size / 2.0)
                }
              }
            }
          })

          configurableDouble("size", size) {
            max = 400.0

            onChange {
              size = it
              markAsDirty()
            }
          }

          configurableDouble("lineWidth", lineWidth) {
            max = 100.0

            onChange {
              lineWidth = it
              markAsDirty()
            }
          }

          configurableEnum("dotType", dotType, DotType.values()) {
            onChange {
              dotType = it
              markAsDirty()
            }
          }
        }
      }
    }
  }

  enum class DotType {
    Cross,
    Cross45Degrees,
    Dot
  }
}

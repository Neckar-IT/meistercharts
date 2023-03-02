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
import kotlin.math.PI

/**
 *
 */
class DrawingPrimitivesArcCenterPathDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Drawing Primitives: Arc Center Path"

  //language=HTML
  override val description: String = "## shows how to add arcs to a path"
  override val category: DemoCategory = DemoCategory.Primitives

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {

    return ChartingDemo {
      meistercharts {
        configure {
          layers.addClearBackground()

          var centerX = 330.0
          var centerY = 330.0

          var radius = 100.0
          var startAngle = 0.0
          var extend = PI / 3.0

          layers.addLayer(object : AbstractLayer() {
            override val type: LayerType
              get() = LayerType.Content

            override fun paint(paintingContext: LayerPaintingContext) {
              val gc = paintingContext.gc

              gc.beginPath()
              gc.arcCenter(centerX, centerY, radius, startAngle, extend)
              gc.stroke(Color.blue)
              gc.stroke()

              gc.stroke(Color.silver)
              gc.strokeCross(centerX, centerY, 5.0)
            }
          }
          )

          configurableDouble("Center X", centerX) {
            max = 800.0
            onChange {
              centerX = it
              markAsDirty()
            }
          }
          configurableDouble("Center Y", centerY) {
            max = 800.0
            onChange {
              centerY = it
              markAsDirty()
            }
          }

          configurableDouble("radius (px)", radius) {
            max = 800.0
            onChange {
              radius = it
              markAsDirty()
            }
          }

          configurableDouble("startAngle (rad)", startAngle) {
            min = -2 * PI
            max = 2 * PI
            onChange {
              startAngle = it
              markAsDirty()
            }
          }

          configurableDouble("extend (rad)", extend) {
            max = PI * 2
            min = -PI * 2
            onChange {
              extend = it
              markAsDirty()
            }
          }
        }
      }
    }
  }
}

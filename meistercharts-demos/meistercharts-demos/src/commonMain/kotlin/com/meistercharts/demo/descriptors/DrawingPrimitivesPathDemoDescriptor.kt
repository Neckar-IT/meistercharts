package com.meistercharts.demo.descriptors

import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.algorithms.painter.Path
import com.meistercharts.canvas.ArcType
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import kotlin.math.PI

/**
 */
class DrawingPrimitivesPathDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Drawing Primitives: Path"

  //language=HTML
  override val description: String = "## shows how to draw a path"
  override val category: DemoCategory = DemoCategory.Primitives

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {
        configure {
          layers.addClearBackground()


          var radius = 150.0
          var startAngle = 0.0
          var arcExtent = 1.5 * PI
          var arcType = ArcType.Open

          val path = Path()
          path.moveTo(10.0, 10.0)
          path.lineTo(30.0, 10.0)
          path.lineTo(30.0, 30.0)
          path.lineTo(10.0, 30.0)
          path.closePath()

          path.moveTo(40.0, 10.0)
          path.lineTo(70.0, 10.0)
          path.lineTo(70.0, 30.0)
          path.lineTo(40.0, 30.0)
          path.closePath()

          layers.addLayer(object : AbstractLayer() {
            override val type: LayerType
              get() = LayerType.Content

            override fun paint(paintingContext: LayerPaintingContext) {
              val gc = paintingContext.gc
              gc.stroke(Color.orangered)
              gc.stroke(path)
            }
          })
        }
      }
    }
  }
}

package com.meistercharts.demo.descriptors

import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.canvas.CanvasRenderingContext
import com.meistercharts.canvas.SnapConfiguration
import com.meistercharts.canvas.pixelSnapSupport
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableEnum
import com.meistercharts.model.Direction

/**
 */
class SnapDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Snapping Demo"
  override val description: String =
    """|## Visualizes Snapping
 """.trimMargin()
  override val category: DemoCategory = DemoCategory.LowLevelTests

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {
        configure {
          layers.addClearBackground()

          configurableEnum("snap", chartSupport.pixelSnapSupport.snapConfiguration, SnapConfiguration.values()) {
            onChange {
              chartSupport.pixelSnapSupport.snapConfiguration = it
              markAsDirty()
            }
          }

          layers.addLayer(object : AbstractLayer() {
            override val type: LayerType
              get() = LayerType.Content

            override fun paint(paintingContext: LayerPaintingContext) {
              val gc = paintingContext.gc
              gc.translateToCenter()
              gc.translate(1.7, 1.7)

              val translation = gc.translation
              gc.fillText("$translation", 0.0, 0.0, Direction.TopLeft, 10.0, 10.0)

              val snappedTranslateX = paintingContext.snapConfiguration.snapXValue(translation.x)
              val snappedTranslateY = paintingContext.snapConfiguration.snapYValue(translation.y)

              gc.resetTransform()
              gc.translate(snappedTranslateX, snappedTranslateY)

              strokeVerticalLines(gc)
              strokeHorizontalLines(gc)
              gc.translate(200.0, 0.0)
            }

            private fun strokeVerticalLines(gc: CanvasRenderingContext) {
              gc.stroke(Color.black)
              gc.strokeLine(0.0, 0.0, 0.0, gc.height)

              gc.translate(5.0, 0.0)
              gc.stroke(Color.gray)
              gc.strokeLine(0.0, 0.0, 0.0, gc.height)

              gc.translate(5.0, 0.0)
              gc.stroke(Color.lightgray)
              gc.strokeLine(0.0, 0.0, 0.0, gc.height)
            }

            private fun strokeHorizontalLines(gc: CanvasRenderingContext) {
              gc.stroke(Color.black)
              gc.strokeLine(0.0, 0.0, gc.width, 0.0)

              gc.translate(0.0, 5.0)
              gc.stroke(Color.gray)
              gc.strokeLine(0.0, 0.0, gc.width, 0.0)

              gc.translate(0.0, 5.0)
              gc.stroke(Color.lightgray)
              gc.strokeLine(0.0, 0.0, gc.width, 0.0)
            }
          })
        }
      }
    }
  }
}

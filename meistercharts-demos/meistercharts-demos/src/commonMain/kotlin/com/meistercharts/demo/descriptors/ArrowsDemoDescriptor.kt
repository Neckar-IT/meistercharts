package com.meistercharts.demo.descriptors

import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.painter.Arrows
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.algorithms.painter.Path
import com.meistercharts.canvas.CanvasRenderingContext
import com.meistercharts.canvas.paintMark
import com.meistercharts.canvas.saved
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableDouble
import com.meistercharts.model.Direction
import it.neckar.open.unit.other.deg

/**
 */
class ArrowsDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Arrows"
  override val category: DemoCategory = DemoCategory.Primitives

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {
        configure {
          layers.addClearBackground()

          val arrowConfig = MyArrowConfig()

          layers.addLayer(object : AbstractLayer() {
            override val type: LayerType = LayerType.Content

            override fun paint(paintingContext: LayerPaintingContext) {
              val gc = paintingContext.gc

              gc.translate(100.0, 100.0)
              gc.saved {
                paintArrow(gc, Arrows.to(Direction.TopCenter, arrowConfig.arrowLength, arrowConfig.arrowHeadHeight, arrowConfig.arrowHeadWidth))
              }

              gc.translate(100.0, 0.0)
              gc.saved {
                paintArrow(gc, Arrows.to(Direction.BottomCenter, arrowConfig.arrowLength, arrowConfig.arrowHeadHeight, arrowConfig.arrowHeadWidth))
              }

              gc.translate(100.0, 0.0)
              gc.saved {
                paintArrow(gc, Arrows.to(Direction.CenterRight, arrowConfig.arrowLength, arrowConfig.arrowHeadHeight, arrowConfig.arrowHeadWidth))
              }
              gc.translate(100.0, 0.0)
              gc.saved {
                paintArrow(gc, Arrows.to(Direction.CenterLeft, arrowConfig.arrowLength, arrowConfig.arrowHeadHeight, arrowConfig.arrowHeadWidth))
              }
            }

            private fun paintArrow(gc: CanvasRenderingContext, arrowPath: Path) {
              gc.paintMark(color = Color.gray)
              gc.stroke(Color.orange)

              gc.saved {
                gc.rotateDegrees(arrowConfig.rotation)
                gc.lineWidth = arrowConfig.lineWidth
                gc.stroke(arrowPath)
              }


              gc.translate(0.0, 100.0)
              gc.paintMark(color = Color.gray)
              gc.fill(Color.orange)

              gc.saved {
                gc.rotateDegrees(arrowConfig.rotation)
                gc.lineWidth = arrowConfig.lineWidth
                gc.fill(arrowPath)
              }
            }
          })

          configurableDouble("Arrow Length", arrowConfig::arrowLength) {
            max = 100.0
            onChange { markAsDirty() }
          }
          configurableDouble("Arrow Head Height", arrowConfig::arrowHeadHeight) {
            max = 100.0
            onChange { markAsDirty() }
          }
          configurableDouble("Arrow Head Width", arrowConfig::arrowHeadWidth) {
            max = 100.0
            onChange { markAsDirty() }
          }
          configurableDouble("Rotation", arrowConfig::rotation) {
            max = 360.0
            min = -360.0
            onChange { markAsDirty() }
          }
          configurableDouble("line width", arrowConfig::lineWidth) {
            max = 10.0
          }
        }
      }
    }
  }
}

private class MyArrowConfig {
  var rotation: @deg Double = 0.0

  var lineWidth = 1.0
  var arrowLength = 40.0
  var arrowHeadHeight = 15.0
  var arrowHeadWidth = 15.0
}

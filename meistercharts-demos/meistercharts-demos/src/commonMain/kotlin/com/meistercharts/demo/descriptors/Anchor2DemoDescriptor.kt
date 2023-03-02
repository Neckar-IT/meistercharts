package com.meistercharts.demo.descriptors

import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.canvas.paintMark
import com.meistercharts.canvas.paintTextBox
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.configurableEnum
import com.meistercharts.model.Direction
import com.meistercharts.model.Rectangle
import com.meistercharts.model.Vicinity
import com.meistercharts.style.BoxStyle

/**
 *
 */
class Anchor2DemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Anchor Advanced Demo"

  //language=HTML
  override val description: String = "<h1>How to configure the anchor</h1>"
  override val category: DemoCategory = DemoCategory.Calculations

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {
        configure {
          layers.addClearBackground()
          val layer = Anchor2DemoDescriptorLayer()
          layers.addLayer(layer)


          configurableDouble("X", layer::x) {
            min = -400.0
            max = 400.0
          }
          configurableDouble("y", layer::y) {
            min = -400.0
            max = 400.0
          }
          configurableDouble("width", layer::width) {
            min = -400.0
            max = 400.0
          }
          configurableDouble("height", layer::height) {
            min = -400.0
            max = 400.0
          }

          configurableEnum("Direction in Shape", layer::directionInShape, enumValues()) {
          }

          configurableEnum("Label side", layer::vicinity, enumValues()) {
          }

          configurableDouble("Anchor Gap Horizontal", layer::anchorGapHorizontal) {
            max = 60.0
          }
          configurableDouble("Anchor Gap Vertical", layer::anchorGapVertical) {
            max = 60.0
          }
        }
      }
    }
  }
}

private class Anchor2DemoDescriptorLayer : AbstractLayer() {
  var directionInShape: Direction = Direction.Center
  var vicinity: Vicinity = Vicinity.Outside
  var anchorGapHorizontal: Double = 0.0
  var anchorGapVertical: Double = 0.0

  var x = -75.0
  var y = -50.0
  var width = 100.0
  var height = 150.0

  override val type: LayerType
    get() = LayerType.Content

  override fun paint(paintingContext: LayerPaintingContext) {
    val gc = paintingContext.gc

    gc.translateToCenter()
    gc.stroke(Color.silver)
    gc.strokeLine(-gc.width, 0.0, gc.width, 0.0)
    gc.strokeLine(0.0, -gc.height, 0.0, gc.height)

    val rect = Rectangle(x, y, width, height)

    gc.stroke(Color.orange)
    gc.strokeRect(rect)

    //Paint the origin of the rectangle
    gc.strokeOvalCenter(x, y, 15.0, 15.0)

    val anchorX = rect.x(directionInShape.horizontalAlignment)
    val anchorY = rect.y(directionInShape.verticalAlignment)
    gc.stroke(Color.red)
    gc.paintMark(anchorX, anchorY)

    val anchorDirection = directionInShape.oppositeIf(vicinity == Vicinity.Outside)
    gc.translate(anchorX, anchorY)
    gc.paintTextBox("The Label", anchorDirection, anchorGapHorizontal, anchorGapVertical, BoxStyle.gray, Color.black)
  }
}

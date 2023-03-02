package com.meistercharts.demo.descriptors

import com.meistercharts.algorithms.axis.AxisOrientationX
import com.meistercharts.algorithms.axis.AxisOrientationY
import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.PaintableLayer
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.withCurrentChartState
import com.meistercharts.algorithms.painter.Arrows
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.algorithms.withContentAreaSize
import com.meistercharts.annotations.PaintableArea
import com.meistercharts.annotations.Zoomed
import com.meistercharts.canvas.fillRectCoordinates
import com.meistercharts.canvas.paintMark
import com.meistercharts.canvas.paintTextBox
import com.meistercharts.canvas.paintable.Paintable
import com.meistercharts.canvas.saved
import com.meistercharts.canvas.strokeBoundingBox
import com.meistercharts.canvas.strokeRectCoordinates
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.section
import com.meistercharts.model.Direction
import com.meistercharts.model.Rectangle
import com.meistercharts.style.BoxStyle
import kotlin.math.absoluteValue

/**
 */
class PaintableCalculatorDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Paintable Calculator"
  override val description: String = "Visualizes how the paintable calculator works"
  override val category: DemoCategory = DemoCategory.Calculations

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {
        configure {
          layers.addClearBackground()

          val paintable = PaintableCalculatorDemoPaintable()
          val layer = PaintableLayer(PaintableLayer.PaintableLayoutMode.Paintable) {
            paintable
          }
          layers.addLayer(layer)
          layers.addLayer(object : AbstractLayer() {
            override val type: LayerType
              get() = LayerType.Content

            override fun paint(paintingContext: LayerPaintingContext) {
              //Paint the "origin" of the paintable layer

              val gc = paintingContext.gc

              gc.stroke(Color.red)
              gc.paintMark(layer.lastX, layer.lastY)
            }
          })

          section("Paintable Location")

          configurableDouble("x", layer.offset.x) {
            min = -300.0
            max = 300.0

            onChange {
              layer.offset = layer.offset.withX(it)
              markAsDirty()
            }
          }
          configurableDouble("y", layer.offset.y) {
            min = -300.0
            max = 300.0

            onChange {
              layer.offset = layer.offset.withY(it)
              markAsDirty()
            }
          }

          section("Paintable - Bounding Box")
          configurableDouble("x", paintable.boundingBoxField.getX()) {
            min = -300.0
            max = 300.0

            onChange {
              paintable.boundingBoxField = paintable.boundingBoxField.withX(it)
              markAsDirty()
            }
          }
          configurableDouble("y", paintable.boundingBoxField.getY()) {
            min = -300.0
            max = 300.0

            onChange {
              paintable.boundingBoxField = paintable.boundingBoxField.withY(it)
              markAsDirty()
            }
          }
          configurableDouble("width", paintable.boundingBoxField.getWidth()) {
            min = -400.0
            max = 400.0

            onChange {
              paintable.boundingBoxField = paintable.boundingBoxField.withWidth(it)
              markAsDirty()
            }
          }
          configurableDouble("height", paintable.boundingBoxField.getHeight()) {
            min = -400.0
            max = 400.0

            onChange {
              paintable.boundingBoxField = paintable.boundingBoxField.withHeight(it)
              markAsDirty()
            }
          }


        }
      }
    }
  }
}

class PaintableCalculatorDemoPaintable : Paintable {
  var boundingBoxField: Rectangle = Rectangle(-10.0, -20.0, 100.0, 50.0)

  override fun boundingBox(paintingContext: LayerPaintingContext): Rectangle {
    return boundingBoxField
  }

  override fun paint(paintingContext: LayerPaintingContext, x: Double, y: Double) {
    val gc = paintingContext.gc
    gc.translate(x, y)
    //to origin

    gc.saved {
      gc.translate(boundingBoxField.location.x, boundingBoxField.location.y)
      paintingContext.withCurrentChartState({ this.withContentAreaSize(boundingBoxField.size) }) {
        paintInternal(paintingContext, x, y)
      }
    }

    //Stroke the bounding box
    gc.saved {
      strokeBoundingBox(paintingContext, 0.0, 0.0, true, Color.cyan)
    }
  }

  private fun paintInternal(paintingContext: LayerPaintingContext, x: Double, y: Double) {
    val gc = paintingContext.gc
    val calculator = paintingContext.chartCalculator

    @PaintableArea val x0 = calculator.domainRelative2zoomedX(0.0)
    @PaintableArea val y0 = calculator.domainRelative2zoomedY(0.0)
    @PaintableArea val x1 = calculator.domainRelative2zoomedX(1.0)
    @PaintableArea val y1 = calculator.domainRelative2zoomedY(1.0)

    //Background
    gc.fill(Color.lightgreen)
    gc.fillRectCoordinates(x0, y0, x1, y1)

    //arrows to visualize axis orientation
    @Zoomed val arrowWidth = calculator.contentAreaRelative2zoomedX(0.6).absoluteValue
    gc.fill(Color.black)
    Arrows.to(calculator.chartState.axisOrientationX.toDirection(), arrowWidth, 10.0, 10.0).let { path ->
      gc.saved {
        gc.translate(calculator.domainRelative2zoomedX(0.9), calculator.domainRelative2zoomedY(0.9))
        gc.fill(path)
        gc.stroke(path)
      }
    }

    @Zoomed val arrowHeight = calculator.contentAreaRelative2zoomedY(0.6).absoluteValue
    Arrows.to(calculator.chartState.axisOrientationY.toDirection(), arrowHeight, 10.0, 10.0).let { path ->
      gc.saved {
        gc.translate(calculator.domainRelative2zoomedX(0.9), calculator.domainRelative2zoomedY(0.9))
        gc.fill(path)
        gc.stroke(path)
      }
    }

    gc.stroke(Color.pink)
    gc.strokeRectCoordinates(x0, y0, x1, y1)
    gc.paintMark()

    gc.saved {
      gc.translate(x0, y0)

      gc.paintMark(0.0, 0.0, color = Color.blue)
      gc.paintTextBox("@DomainRelative 0/0", Direction.TopLeft, 5.0, boxStyle = boxStyle, textColor = Color.white)
    }

    gc.saved {
      gc.translate(x1, y1)

      gc.paintMark(0.0, 0.0, color = Color.blue)
      gc.paintTextBox("@DomainRelative 1/1", Direction.TopLeft, 5.0, boxStyle = boxStyle, textColor = Color.white)
    }
  }

  val boxStyle = BoxStyle(Color.rgba(100, 100, 100, 0.5))
}

private fun AxisOrientationX.toDirection(): Direction {
  return when (this) {
    AxisOrientationX.OriginAtLeft -> Direction.CenterRight
    AxisOrientationX.OriginAtRight -> Direction.CenterLeft
  }
}

private fun AxisOrientationY.toDirection(): Direction {
  return when (this) {
    AxisOrientationY.OriginAtBottom -> Direction.TopCenter
    AxisOrientationY.OriginAtTop -> Direction.BottomCenter
  }
}

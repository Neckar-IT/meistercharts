package com.meistercharts.demo.descriptors

import com.meistercharts.algorithms.axis.AxisSelection
import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.PaintableLayer
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.canvas.paintMark
import com.meistercharts.canvas.paintable.withFlippedAxisOrientation
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.configurableEnum
import com.meistercharts.demo.section

/**
 */
class WithAxisOrientationPaintableDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "With Axis Orientation Paintable"
  override val category: DemoCategory = DemoCategory.Paintables

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {
        configure {
          layers.addClearBackground()

          //The axis selection of the axis the orientation is changed for
          var changedAxis: AxisSelection = AxisSelection.Both

          val paintable = PaintableCalculatorDemoPaintable()

          val paintableLayer = PaintableLayer(PaintableLayer.PaintableLayoutMode.Paintable) {
            paintable.withFlippedAxisOrientation {
              changedAxis
            }
          }
          layers.addLayer(paintableLayer)
          layers.addLayer(object : AbstractLayer() {
            override val type: LayerType
              get() = LayerType.Content

            override fun paint(paintingContext: LayerPaintingContext) {
              //Paint the "origin" of the paintable layer

              val gc = paintingContext.gc

              gc.stroke(Color.red)
              gc.paintMark(paintableLayer.lastX, paintableLayer.lastY)
            }
          })

          section("Flip Axis Orientation")

          configurableEnum("Flipped Orientations for", changedAxis, enumValues()) {
            onChange {
              changedAxis = it
              markAsDirty()
            }
          }

          section("Paintable Location")

          configurableDouble("x", paintableLayer.offset.x) {
            min = -300.0
            max = 300.0

            onChange {
              paintableLayer.offset = paintableLayer.offset.withX(it)
              markAsDirty()
            }
          }
          configurableDouble("y", paintableLayer.offset.y) {
            min = -300.0
            max = 300.0

            onChange {
              paintableLayer.offset = paintableLayer.offset.withY(it)
              markAsDirty()
            }
          }

          section("Paintable")
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

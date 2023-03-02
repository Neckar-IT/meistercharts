package com.meistercharts.demo.descriptors

import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.canvas.paintMark
import com.meistercharts.canvas.paintable.TextBoxPaintable
import com.meistercharts.canvas.saved
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableDouble

class TextBoxPaintableDemoDescriptor : ChartingDemoDescriptor<Nothing> {

  override val name: String = "Text Box Paintable"

  //language=HTML
  override val category: DemoCategory = DemoCategory.Paintables

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {

        configure {
          layers.addClearBackground()

          val layer = object : AbstractLayer() {
            override val type: LayerType = LayerType.Content

            var x: Double = 100.0
            var y: Double = 100.0

            val texts = listOf("Line 1", "Line 2", "", "after empty Line")

            val paintable = TextBoxPaintable({ _, _ ->
              texts
            }) {
            }

            override fun paint(paintingContext: LayerPaintingContext) {
              val gc = paintingContext.gc
              val chartSupport = paintingContext.chartSupport

              gc.paintMark(x, y)

              gc.saved {
                val boundingBox = paintable.boundingBox(paintingContext)

                gc.translate(x, y)
                gc.fill(Color.yellow)
                gc.fillRect(boundingBox)

                gc.stroke(Color.orangered)
                gc.strokeRect(boundingBox)
              }

              paintable.paint(paintingContext, x, y)
            }
          }
          layers.addLayer(layer)

          configurableDouble("x", layer::x) {
            max = 400.0
          }
          configurableDouble("y", layer::y) {
            max = 400.0
          }
        }

      }
    }
  }
}

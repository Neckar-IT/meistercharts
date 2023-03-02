package com.meistercharts.demo.descriptors

import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.canvas.paintMark
import com.meistercharts.canvas.paintable.LabelPaintable
import com.meistercharts.canvas.saved
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableColorPickerProvider
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.configurableEnum
import com.meistercharts.demo.configurableFont
import com.meistercharts.demo.configurableList
import com.meistercharts.demo.section

class LabelPaintableDemoDescriptor : ChartingDemoDescriptor<Nothing> {

  override val name: String = "Label Paintable"

  //language=HTML
  override val category: DemoCategory = DemoCategory.Paintables

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {

        val text = "Hello World!qx°A " + "xHpÁÄgqÅÁqÜgÖfÄPß " + "與朋友交而不信乎" + " 제는 보장"

        val paintable = LabelPaintable({ _, _ ->
          text
        }) {
        }

        configure {
          layers.addClearBackground()

          val layer = object : AbstractLayer() {
            override val type: LayerType = LayerType.Content

            var x: Double = 100.0
            var y: Double = 100.0

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

          section("Anchor")
          configurableEnum("Anchor Direction", paintable.configuration::anchorDirection)
          configurableDouble("Horizontal Gap", paintable.configuration::anchorGapHorizontal) {
            min = -100.0
            max = 100.0
          }
          configurableDouble("Vertical Gap", paintable.configuration::anchorGapVertical) {
            min = -100.0
            max = 100.0
          }
          configurableDouble("Max Width", paintable.configuration::maxWidth) {
            max = 200.0
          }

          configurableColorPickerProvider("Text Color", paintable.configuration::labelColor)
          configurableFont("Label", paintable.configuration::font)

          configurableList("Text", text, listOf(text, "", null, "Hello World")) {
            onChange {
              paintable.configuration.label = { _, _ -> it }
              markAsDirty()
            }
          }
        }
      }
    }
  }
}

package com.meistercharts.demo.descriptors

import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.debug.ContentAreaDebugLayer
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.canvas.paintTextBox
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableBoolean
import com.meistercharts.demo.configurableColor
import com.meistercharts.demo.configurableDouble
import com.meistercharts.model.Direction
import com.meistercharts.resources.Icons
import com.meistercharts.style.BoxStyle
import com.meistercharts.style.Shadow

/**
 */
class ShadowDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Shadow"

  //language=HTML
  override val category: DemoCategory = DemoCategory.Primitives

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {
        configure {
          layers.addClearBackground()
          val layer = object : AbstractLayer() {
            override val type: LayerType = LayerType.Content

            var shadowEnabled: Boolean = true
            var clearShadow: Boolean = false
            var shadowColor: Color = Color.black
            var blurRadius = 10.0
            var offsetX = 0.0
            var offsetY = 0.0

            override fun paint(paintingContext: LayerPaintingContext) {
              val gc = paintingContext.gc

              if (shadowEnabled) {
                gc.shadow(shadowColor, blurRadius, offsetX, offsetY)
              }

              if (clearShadow) {
                gc.clearShadow()
              }

              gc.translate(200.0, 150.0)

              gc.fill(Color.darkgray)
              gc.fillRect(0.0, 0.0, 50.0, 58.0)


              gc.translate(0.0, 70.0)
              gc.fill(Color.black)
              gc.fillText("Hello World", 0.0, 0.0, Direction.TopLeft)

              gc.translate(0.0, 20.0)
              gc.stroke(Color.blue)
              gc.strokeText("Hello World", 0.0, 0.0, Direction.TopLeft)

              gc.translate(0.0, 40.0)
              Icons.hourglass(fill = Color.orange).paint(paintingContext)


              gc.translate(0.0, 40.0)
              paintingContext.gc.paintTextBox(listOf("This is a text!", "Shadow.Default"), Direction.TopLeft, boxStyle = BoxStyle(Color.lightgreen, borderColor = Color.red, shadow = Shadow.Default))

              gc.translate(0.0, 80.0)
              paintingContext.gc.paintTextBox(listOf("Shadow.Light"), Direction.TopLeft, boxStyle = BoxStyle(Color.lightgreen, borderColor = Color.red, shadow = Shadow.Light))

              gc.translate(0.0, 40.0)
              paintingContext.gc.paintTextBox(listOf("Shadow.LightDrop"), Direction.TopLeft, boxStyle = BoxStyle(Color.lightgreen, borderColor = Color.red, shadow = Shadow.LightDrop))
            }
          }
          layers.addLayer(layer)
          layers.addLayer(ContentAreaDebugLayer())

          configurableBoolean("Shadow enabled", layer::shadowEnabled)
          configurableBoolean("Clear Shadow", layer::clearShadow)
          configurableColor("Shadow Color", layer::shadowColor)
          configurableDouble("Blur Radius", layer::blurRadius) {
            max = 50.0
          }

          configurableDouble("Offset X", layer::offsetX) {
            min = -10.0
            max = 10.0
          }
          configurableDouble("Offset Y", layer::offsetY) {
            min = -10.0
            max = 10.0
          }
        }
      }
    }
  }
}

package com.meistercharts.demo.descriptors

import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.text.addTextUnresolved
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.canvas.FontDescriptor
import com.meistercharts.canvas.FontFamily
import com.meistercharts.canvas.FontSize
import com.meistercharts.canvas.FontStyle
import com.meistercharts.canvas.FontWeight
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.model.Direction
import com.meistercharts.model.DirectionBasedBasePointProvider
import com.meistercharts.model.Insets
import com.meistercharts.style.BoxStyle

/**
 * Demos that visualizes the functionality of the FPS layer
 */
class FontDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Fonts"

  //language=HTML
  override val description: String = "## Different fonts"
  override val category: DemoCategory = DemoCategory.Text

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {
        configure {
          layers.addClearBackground()

          val boxStyle = BoxStyle(Color.orange, Color.gray, padding = Insets.of(5.0))

          val arial = FontFamily("Arial")
          val courier = FontFamily("Courier New")
          val ubuntu = FontFamily("Ubuntu")
          val tahoma = FontFamily("Tahoma")

          layers.addTextUnresolved("Tahoma, 10") {
            font = FontDescriptor(family = tahoma, size = FontSize.Default)
            anchorDirection = Direction.Center
            margin = Insets.of(10.0)
            this.boxStyle = boxStyle
          }
          layers.addTextUnresolved("Arial 18px") {
            font = FontDescriptor(arial, FontSize(18.0))
            anchorDirection = Direction.TopRight
            anchorPointProvider = DirectionBasedBasePointProvider(anchorDirection)
            margin = Insets.of(10.0)
            this.boxStyle = boxStyle
          }
          layers.addTextUnresolved("Arial 18px bold") {
            font = FontDescriptor(arial, FontSize(18.0), FontWeight.Bold)
            anchorDirection = Direction.TopCenter
            anchorPointProvider = DirectionBasedBasePointProvider(anchorDirection)
            margin = Insets.of(10.0)
            this.boxStyle = boxStyle
          }
          layers.addTextUnresolved("Arial 18px italic") {
            font = FontDescriptor(arial, FontSize(18.0), style = FontStyle.Italic)
            anchorDirection = Direction.TopLeft
            anchorPointProvider = DirectionBasedBasePointProvider(anchorDirection)
            margin = Insets.of(10.0)
            this.boxStyle = boxStyle
          }
          layers.addTextUnresolved("Arial 18px bold italic") {
            font = FontDescriptor(arial, FontSize(18.0), FontWeight.Bold, FontStyle.Italic)
            anchorDirection = Direction.BottomRight
            anchorPointProvider = DirectionBasedBasePointProvider(anchorDirection)
            margin = Insets.of(10.0)
            this.boxStyle = boxStyle
          }
          layers.addTextUnresolved("Courier New 18px") {
            font = FontDescriptor(courier, FontSize(18.0))
            anchorDirection = Direction.BottomLeft
            anchorPointProvider = DirectionBasedBasePointProvider(anchorDirection)
            margin = Insets.of(10.0)
            this.boxStyle = boxStyle
          }
          layers.addTextUnresolved("Courier New 18px oblique") {
            font = FontDescriptor(courier, FontSize(18.0), style = FontStyle.Oblique)
            anchorDirection = Direction.BottomCenter
            anchorPointProvider = DirectionBasedBasePointProvider(anchorDirection)
            margin = Insets.of(10.0)
            this.boxStyle = boxStyle
          }
          layers.addTextUnresolved("Courier New 18px oblique bold") {
            font = FontDescriptor(
              courier,
              FontSize(18.0),
              style = FontStyle.Oblique,
              weight = FontWeight.Bold
            )
            anchorDirection = Direction.CenterLeft
            anchorPointProvider = DirectionBasedBasePointProvider(anchorDirection)
            margin = Insets.of(10.0)
            this.boxStyle = boxStyle
          }
          layers.addTextUnresolved("Ubuntu 18px italic") {
            font = FontDescriptor(
              ubuntu,
              FontSize(18.0),
              style = FontStyle.Italic,
              weight = FontWeight.Normal
            )
            anchorDirection = Direction.CenterRight
            anchorPointProvider = DirectionBasedBasePointProvider(anchorDirection)
            margin = Insets.of(10.0)
            this.boxStyle = boxStyle
          }
        }
      }
    }
  }
}

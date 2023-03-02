package com.meistercharts.demo.descriptors

import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.painter.CanvasPaintProvider
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.algorithms.painter.LinearGradient
import com.meistercharts.canvas.StyleDsl
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableBoolean
import com.meistercharts.demo.configurableColorPicker
import com.meistercharts.demo.configurableDouble

/**
 *
 */
class LinearColorGradientDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Linear Color Gradient"
  override val category: DemoCategory = DemoCategory.LowLevelTests

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {

    return ChartingDemo {
      meistercharts {

        configure {
          layers.addClearBackground()

          val gradientLayer = MyLinearGradientLayer()
          layers.addLayer(gradientLayer)

          declare {
            section("Translate")
          }
          configurableDouble("X", gradientLayer.style::translateX) {
            max = 500.0
          }
          configurableDouble("Y", gradientLayer.style::translateY) {
            max = 500.0
          }

          declare {
            section("Rectangle")
          }

          configurableDouble("X", gradientLayer.style::x) {
            max = 500.0
          }
          configurableDouble("Y", gradientLayer.style::y) {
            max = 500.0
          }

          declare {
            section("Gradient")
          }

          var startColor: Color = Color.azure
          var endColor: Color = Color.darkviolet
          var flat = false

          fun updateFill() {
            gradientLayer.style.fill = if (flat) {
              startColor
            } else {
              LinearGradient(startColor, endColor)
            }
          }

          configurableBoolean("flat") {
            value = flat

            onChange {
              flat = it
              updateFill()
              markAsDirty()
            }
          }

          configurableColorPicker("Start", startColor) {
            onChange {
              startColor = it
              updateFill()
              markAsDirty()
            }
          }
          configurableColorPicker("End", endColor) {
            onChange {
              endColor = it
              updateFill()
              markAsDirty()
            }
          }

          configurableDouble("X0", gradientLayer.style::gradientX0) {
            max = 500.0
          }
          configurableDouble("Y0", gradientLayer.style::gradientY0) {
            max = 500.0
          }
          configurableDouble("X1", gradientLayer.style::gradientX1) {
            max = 500.0
          }
          configurableDouble("Y1", gradientLayer.style::gradientY1) {
            max = 500.0
          }
        }
      }
    }
  }
}

private class MyLinearGradientLayer : AbstractLayer() {
  val style: Style = Style()

  override val type: LayerType
    get() = LayerType.Content

  override fun paint(paintingContext: LayerPaintingContext) {
    val fill = style.fill.toCanvasPaint(
      style.gradientX0, style.gradientY0,
      style.gradientX1, style.gradientY1
    )

    val gc = paintingContext.gc
    gc.translate(style.translateX, style.translateY)
    gc.fill(fill)
    gc.fillRect(style.x, style.y, 200.0, 200.0)
  }

  @StyleDsl
  class Style {
    var fill: CanvasPaintProvider = Color.orange

    var translateX = 0.0
    var translateY = 0.0

    var x = 10.0
    var y = 10.0

    var gradientX0 = 0.0
    var gradientY0 = 0.0
    var gradientX1 = 200.0
    var gradientY1 = 0.0
  }
}

/*
 * Copyright 2023 Neckar IT GmbH, Mössingen, Germany
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.meistercharts.demo.descriptors

import com.meistercharts.demo.configurable
import com.meistercharts.demo.section
import kotlin.uuid.Uuid
import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.color.CanvasPaintProvider
import com.meistercharts.color.Color
import com.meistercharts.color.RadialGradient
import com.meistercharts.canvas.StyleDsl
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.VariabilityType
import com.meistercharts.demo.configurableColorPicker
import com.meistercharts.demo.configurableDouble

/**
 *
 */
class RadialColorGradientDemoDescriptor : MeisterchartsDemoDescriptor<Nothing> {
  override val uuid: Uuid = Uuid.parse("55f14e48-5645-4e5f-86e0-390b1788bcdb")
  override val name: String = "Radial Color Gradient"
  override val category: DemoCategory = DemoCategory.LowLevelTests
  override val quality: DemoQuality = DemoQuality.High
  override val variabilityType: VariabilityType = VariabilityType.Stable

  override fun prepareDemo(configuration: PredefinedConfiguration<Nothing>?): MeisterchartsDemo {

    return MeisterchartsDemo(this) {
      meistercharts {

        configure {
          layers.addClearBackground()

          val gradientLayer = MyRadialGradientLayer()
          layers.addLayer(gradientLayer)

          section("Translate")
          configurable("X", gradientLayer.style::translateX) {
            max = 500.0
          }
          configurable("Y", gradientLayer.style::translateY) {
            max = 500.0
          }

          section("Rectangle")

          configurable("X", gradientLayer.style::x) {
            max = 500.0
          }
          configurable("Y", gradientLayer.style::y) {
            max = 500.0
          }

          section("Gradient")

          var startColor: Color = Color.azure()
          var endColor: Color = Color.darkviolet()

          fun updateFill() {
            gradientLayer.style.fill = RadialGradient(startColor, endColor)
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

          configurable("X0", gradientLayer.style::gradientX0) {
            max = 500.0
          }
          configurable("Y0", gradientLayer.style::gradientY0) {
            max = 500.0
          }
          configurable("X1", gradientLayer.style::gradientX1) {
            max = 500.0
          }
          configurable("Y1", gradientLayer.style::gradientY1) {
            max = 500.0
          }
        }
      }
    }
  }
}


private class MyRadialGradientLayer : AbstractLayer() {
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
    var fill: CanvasPaintProvider = Color.orange()

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

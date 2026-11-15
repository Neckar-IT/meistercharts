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
import kotlin.uuid.Uuid
import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.color.Color
import com.meistercharts.canvas.StrokeLocation
import com.meistercharts.canvas.ConfigurationDsl
import com.meistercharts.canvas.fill
import com.meistercharts.canvas.roundedRect
import com.meistercharts.canvas.stroke
import com.meistercharts.color.ColorProvider
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.VariabilityType
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.configurableEnum
import it.neckar.open.unit.other.px

/**
 *
 */
class RoundedRectsDemoDescriptor : MeisterchartsDemoDescriptor<Nothing> {
  override val uuid: Uuid = Uuid.parse("2a1f9563-4159-47fa-b63f-ac4dea0124b3")
  override val name: String = "Rounded Rectangles"
  override val description: String = "Draws rectangles with rounded corners"
  override val category: DemoCategory = DemoCategory.Primitives
  override val quality: DemoQuality = DemoQuality.High
  override val variabilityType: VariabilityType = VariabilityType.Stable

  override fun prepareDemo(configuration: PredefinedConfiguration<Nothing>?): MeisterchartsDemo {

    return MeisterchartsDemo(this) {
      meistercharts {

        configure {
          layers.addClearBackground()

          val gradientLayer = RoundedRectsLayer()
          layers.addLayer(gradientLayer)

          configurable("radius", gradientLayer.configuration::radius) {
            max = 50.0
          }
          configurable("lineWidth", gradientLayer.configuration::lineWidth) {
            max = 20.0
          }

          configurable("Stroke Location", gradientLayer.configuration::strokeLocation, kotlin.enums.enumEntries<StrokeLocation>().toList())

          configurable("width", gradientLayer::width) {
            min = -100.0
            max = 500.0
          }
          configurable("height", gradientLayer::height) {
            min = -100.0
            max = 500.0
          }
        }
      }
    }
  }
}

private class RoundedRectsLayer : AbstractLayer() {
  val configuration: Configuration = Configuration()

  var width = 200.0
  var height = 300.0

  override val type: LayerType
    get() = LayerType.Content

  override fun paint(paintingContext: LayerPaintingContext) {
    val gc = paintingContext.gc
    gc.lineWidth = configuration.lineWidth
    gc.translateToCenter()
    gc.roundedRect(0.0, 0.0, width, height, configuration.radius, configuration.strokeLocation)
    gc.fill(configuration.fill)
    gc.fill()
    gc.stroke(configuration.stroke)
    gc.stroke()
  }


  @ConfigurationDsl
  class Configuration {
    var fill: ColorProvider = Color.orange
    var stroke: ColorProvider = Color.red

    var lineWidth = 1.0

    /**
     * The radius for the rounded rect in pixels
     */
    var radius: @px Double = 30.0

    var strokeLocation: StrokeLocation = StrokeLocation.Center

  }
}

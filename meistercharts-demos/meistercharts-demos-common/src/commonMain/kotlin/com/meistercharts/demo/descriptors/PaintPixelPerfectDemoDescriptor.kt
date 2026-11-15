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
import com.meistercharts.canvas.Image
import com.meistercharts.canvas.devicePixelRatio
import com.meistercharts.canvas.loadImage
import com.meistercharts.canvas.paintLocation
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.VariabilityType
import com.meistercharts.demo.configurableBoolean
import com.meistercharts.demo.configurableDouble
import it.neckar.open.http.Url

/**
 *
 */
class PaintPixelPerfectDemoDescriptor : MeisterchartsDemoDescriptor<Nothing> {
  override val uuid: Uuid = Uuid.parse("e69dcf5e-6fa7-463b-be3a-25a3a47fcf12")
  override val name: String = "Paint Pixel Perfect"
  override val description: String = "Paints a 360x360 pixel image using paintImagePixelPerfect"
  override val category: DemoCategory = DemoCategory.LowLevelTests
  override val quality: DemoQuality = DemoQuality.High
  override val variabilityType: VariabilityType = VariabilityType.Stable

  override fun prepareDemo(configuration: PredefinedConfiguration<Nothing>?): MeisterchartsDemo {
    return MeisterchartsDemo(this) {
      meistercharts {
        configure {
          layers.addClearBackground()

          val layer = object : AbstractLayer() {
            override val type: LayerType = LayerType.Content

            var x = 10.0
            var y = 10.0
            var pixelPerfect = true

            var image: Image? = null

            init {
              loadImage(Url.relative("nit-logo-n-neg_360x360_facebook_insta.png")) {
                image = it
                markAsDirty()
              }
            }

            override fun paint(paintingContext: LayerPaintingContext) {
              val gc = paintingContext.gc

              val devicePixelRatio = paintingContext.chartSupport.devicePixelRatio

              gc.paintLocation(x, y)

              image?.let {
                if (pixelPerfect) {
                  gc.paintImagePixelPerfect(it, x, y)
                } else {
                  gc.paintImage(it, x, y, it.size.width / devicePixelRatio, it.size.height / devicePixelRatio)
                }
              }
            }
          }
          layers.addLayer(layer)

          configurable("X", layer::x) {
            step = 0.1
            max = 1000.0
          }
          configurable("Y", layer::y) {
            step = 0.1
            max = 1000.0
          }
          configurable("Pixel Perfect", layer::pixelPerfect)
        }
      }
    }
  }
}

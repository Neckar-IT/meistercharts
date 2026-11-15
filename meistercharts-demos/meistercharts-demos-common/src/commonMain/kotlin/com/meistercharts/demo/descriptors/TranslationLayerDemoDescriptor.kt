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
import com.meistercharts.algorithms.layers.translate
import com.meistercharts.canvas.fill
import com.meistercharts.color.Color
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.VariabilityType
import com.meistercharts.demo.configurableDouble

/**
 */
class TranslationLayerDemoDescriptor : MeisterchartsDemoDescriptor<Nothing> {
  override val uuid: Uuid = Uuid.parse("14977eef-b5f6-4e3b-a301-60892503fc35")
  override val name: String = "Translation layer"
  override val category: DemoCategory = DemoCategory.Layers
  override val description: String = "Translates another layer (in this case a red rectangle of size 100x100)"
  override val quality: DemoQuality = DemoQuality.High
  override val variabilityType: VariabilityType = VariabilityType.Stable

  override fun prepareDemo(configuration: PredefinedConfiguration<Nothing>?): MeisterchartsDemo {
    return MeisterchartsDemo(this) {
      meistercharts {
        configure {
          val config = object {
            var translateX = 10.0
            var translateY = 10.0
          }

          layers.addClearBackground()
          val translationLayer = MyRectangleLayer().translate({ config.translateX }, { config.translateY })
          layers.addLayer(translationLayer)

          configurable("translate-X", config::translateX) {
            min = -1000.0
            max = 1000.0
          }
          configurable("translate-Y", config::translateY) {
            min = -1000.0
            max = 1000.0
          }
        }
      }
    }
  }

  private class MyRectangleLayer : AbstractLayer() {
    override val type: LayerType = LayerType.Content

    override fun paint(paintingContext: LayerPaintingContext) {
      val gc = paintingContext.gc
      gc.fill(Color.indianred)
      gc.fillRect(0.0, 0.0, 100.0, 100.0)
    }
  }
}

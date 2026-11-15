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
import com.meistercharts.algorithms.layers.text.TextsLayer
import com.meistercharts.color.Color
import com.meistercharts.canvas.paintMark
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.VariabilityType
import com.meistercharts.demo.configurableColorProvider
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.configurableEnum
import com.meistercharts.demo.configurableFont
import it.neckar.open.kotlin.lang.fastFor

/**
 * Very simple demo that shows how to work with a value axis layer
 */
class TextsLayerDemoDescriptor : MeisterchartsDemoDescriptor<Nothing> {
  override val uuid: Uuid = Uuid.parse("800a644f-f1e9-4d18-b69d-21541f5c6ac0")
  override val name: String = "Texts Demo"
  override val category: DemoCategory = DemoCategory.Text
  override val quality: DemoQuality = DemoQuality.High
  override val variabilityType: VariabilityType = VariabilityType.Stable

  //language=HTML
  override val description: String = "Places a list of texts on the specified coordinates."

  override fun prepareDemo(configuration: PredefinedConfiguration<Nothing>?): MeisterchartsDemo {
    return MeisterchartsDemo(this) {
      val textsLayer = TextsLayer()

      meistercharts {
        configure {
          layers.addClearBackground()
          layers.addLayer(object : AbstractLayer() {
            override val type: LayerType = LayerType.Background

            override fun paint(paintingContext: LayerPaintingContext) {
              val gc = paintingContext.gc
              textsLayer.configuration.texts.size().fastFor {
                val location = textsLayer.configuration.locationProvider.valueAt(it)
                gc.paintMark(location, color = Color.silver())
              }
            }
          })

          layers.addLayer(textsLayer)

          section("Layout")

          configurable("Anchor Direction", textsLayer.configuration::anchorDirection)
          configurable("Anchor Gap Horizontal", textsLayer.configuration::anchorGapHorizontal) {
            max = 100.0
          }
          configurable("Anchor Gap Vertical", textsLayer.configuration::anchorGapVertical) {
            max = 100.0
          }
          configurable("Font", textsLayer.configuration::font)
          configurableColorProvider("Text Color", textsLayer.configuration::textColor)
        }
      }
    }
  }
}

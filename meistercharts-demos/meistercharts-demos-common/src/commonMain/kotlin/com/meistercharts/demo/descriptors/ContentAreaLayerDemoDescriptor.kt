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


import kotlin.uuid.Uuid
import com.meistercharts.zoom.ZoomAndTranslationDefaults
import com.meistercharts.algorithms.layers.ContentAreaLayer
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.VariabilityType
import com.meistercharts.demo.configurableBoolean
import com.meistercharts.demo.configurableColorProvider
import it.neckar.open.kotlin.lang.asProvider

/**
 * A demo for the [ContentAreaLayer]
 *
 */
class ContentAreaLayerDemoDescriptor : MeisterchartsDemoDescriptor<Nothing> {
  override val uuid: Uuid = Uuid.parse("5fbb238b-8b0e-426b-87d2-4c2b81e98ac3")
  override val name: String = "Content Area Layer Border"
  override val quality: DemoQuality = DemoQuality.High
  override val variabilityType: VariabilityType = VariabilityType.Stable

  //language=HTML
  override val description: String = "Shows the border of the content area"
  override val category: DemoCategory = DemoCategory.Layers

  override fun prepareDemo(configuration: PredefinedConfiguration<Nothing>?): MeisterchartsDemo {
    return MeisterchartsDemo(this) {
      meistercharts {
        zoomAndTranslationDefaults {
          ZoomAndTranslationDefaults.tenPercentMargin
        }

        configure {
          layers.addClearBackground()
          val contentAreaLayer = ContentAreaLayer()
          layers.addLayer(contentAreaLayer)

          configurableBoolean("Left side") {
            value = contentAreaLayer.configuration.sidesToPaint.leftSelected
            onChange {
              contentAreaLayer.configuration.sidesToPaint = contentAreaLayer.configuration.sidesToPaint.copy(leftSelected = it)
              markAsDirty()
            }
          }

          configurableBoolean("Top side") {
            value = contentAreaLayer.configuration.sidesToPaint.topSelected
            onChange {
              contentAreaLayer.configuration.sidesToPaint = contentAreaLayer.configuration.sidesToPaint.copy(topSelected = it)
              markAsDirty()
            }
          }

          configurableBoolean("Right side") {
            value = contentAreaLayer.configuration.sidesToPaint.rightSelected
            onChange {
              contentAreaLayer.configuration.sidesToPaint = contentAreaLayer.configuration.sidesToPaint.copy(rightSelected = it)
              markAsDirty()
            }
          }

          configurableBoolean("Bottom side") {
            value = contentAreaLayer.configuration.sidesToPaint.bottomSelected
            onChange {
              contentAreaLayer.configuration.sidesToPaint = contentAreaLayer.configuration.sidesToPaint.copy(bottomSelected = it)
              markAsDirty()
            }
          }

          configurableColorProvider("Stroke", contentAreaLayer.configuration::color) {
            onChange {
              contentAreaLayer.configuration.color = it.asProvider()
              markAsDirty()
            }
          }
        }
      }
    }
  }
}

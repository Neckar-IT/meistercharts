/**
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

import com.meistercharts.algorithms.impl.ZoomAndTranslationDefaults
import com.meistercharts.algorithms.layers.ContentAreaLayer
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableBoolean
import com.meistercharts.demo.configurableColor

/**
 * A demo for the [ContentAreaLayer]
 *
 */
class ContentAreaLayerDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Content Area Layer Border"

  //language=HTML
  override val description: String = "## Shows a value area"
  override val category: DemoCategory = DemoCategory.Layers

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {
        zoomAndTranslationDefaults {
          ZoomAndTranslationDefaults.tenPercentMargin
        }

        configure {
          layers.addClearBackground()
          val contentAreaLayer = ContentAreaLayer()
          layers.addLayer(contentAreaLayer)

          configurableBoolean("Left side") {
            value = contentAreaLayer.style.sidesToPaint.leftSelected
            onChange {
              contentAreaLayer.style.sidesToPaint = contentAreaLayer.style.sidesToPaint.copy(leftSelected = it)
              markAsDirty()
            }
          }

          configurableBoolean("Top side") {
            value = contentAreaLayer.style.sidesToPaint.topSelected
            onChange {
              contentAreaLayer.style.sidesToPaint = contentAreaLayer.style.sidesToPaint.copy(topSelected = it)
              markAsDirty()
            }
          }

          configurableBoolean("Right side") {
            value = contentAreaLayer.style.sidesToPaint.rightSelected
            onChange {
              contentAreaLayer.style.sidesToPaint = contentAreaLayer.style.sidesToPaint.copy(rightSelected = it)
              markAsDirty()
            }
          }

          configurableBoolean("Bottom side") {
            value = contentAreaLayer.style.sidesToPaint.bottomSelected
            onChange {
              contentAreaLayer.style.sidesToPaint = contentAreaLayer.style.sidesToPaint.copy(bottomSelected = it)
              markAsDirty()
            }
          }

          configurableColor("Stroke", contentAreaLayer.style::color) {
            onChange {
              contentAreaLayer.style.color = it
              markAsDirty()
            }
          }
        }
      }
    }
  }
}

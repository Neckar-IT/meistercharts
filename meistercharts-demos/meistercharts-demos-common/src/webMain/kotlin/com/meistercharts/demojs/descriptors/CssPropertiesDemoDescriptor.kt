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
package com.meistercharts.demojs.descriptors

import kotlin.uuid.Uuid
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.text.addTexts
import com.meistercharts.canvas.font
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.VariabilityType
import com.meistercharts.js.CanvasJS
import kotlinx.browser.window
import org.w3c.dom.HTMLCanvasElement

class CssPropertiesDemoDescriptor : MeisterchartsDemoDescriptor<Nothing> {
  override val uuid: Uuid = Uuid.parse("87be47d5-8171-4d96-beac-d0d6c7cd8280")
  override val name: String = "CSS Properties"
  override val category: DemoCategory = DemoCategory.Platform
  override val quality: DemoQuality = DemoQuality.High
  override val variabilityType: VariabilityType = VariabilityType.Stable

  override fun prepareDemo(configuration: PredefinedConfiguration<Nothing>?): MeisterchartsDemo {
    return MeisterchartsDemo(this) {
      meistercharts {
        configure {
          val chartSupport = this.chartSupport

          layers.addClearBackground()

          val canvas = chartSupport.canvas
          val canvasElement = (canvas as CanvasJS).canvasElement
          requireNotNull(canvasElement) { "canvasElement is null" }

          fun getCanvasElement(): HTMLCanvasElement {
            val canvasElement = (canvas as CanvasJS).canvasElement
            return canvasElement
          }

          layers.addTexts { textService, i18nConfiguration ->
            buildList {
              add("CSS properties")

              val computedStyle = window.getComputedStyle(canvasElement)
              add("computedStyle ${computedStyle}")
              add("computedStyle.font ${computedStyle.font}")
              add("extractedFont ${canvasElement.font()}")
            }
          }

          declare {
            button("Font Family: Comic Sans MS") {
              getCanvasElement().style.fontFamily = "Comic Sans MS"
              markAsDirty()
            }
          }
        }
      }
    }

  }
}

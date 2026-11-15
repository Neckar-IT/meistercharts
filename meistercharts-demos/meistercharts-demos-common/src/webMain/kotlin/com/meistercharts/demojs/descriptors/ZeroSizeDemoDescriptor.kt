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
import com.meistercharts.algorithms.layers.text.addTextUnresolvedColored
import com.meistercharts.color.Color
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.VariabilityType
import com.meistercharts.js.CanvasJS
import org.w3c.dom.HTMLCanvasElement

class ZeroSizeDemoDescriptor : MeisterchartsDemoDescriptor<Nothing> {
  override val uuid: Uuid = Uuid.parse("3ee206cf-6e56-46dd-82c0-122539b995ed")
  override val name: String = "Zero Size"
  override val description: String = "## What happens if the canvas has no size"
  override val category: DemoCategory = DemoCategory.Platform
  override val quality: DemoQuality = DemoQuality.High
  override val variabilityType: VariabilityType = VariabilityType.Stable

  override fun prepareDemo(configuration: PredefinedConfiguration<Nothing>?): MeisterchartsDemo {
    return MeisterchartsDemo(this) {
      meistercharts {
        configure {
          layers.addClearBackground()
          layers.addTextUnresolvedColored("Hello World", Color.darkorange)

          declare {

            fun getCanvasElement(): HTMLCanvasElement {
              return (chartSupport.canvas as CanvasJS).canvasElement
            }

            button("hidden=\"hidden\"") {
              println("hidden=\"hidden\"")
              getCanvasElement().setAttribute("hidden", "hidden");
              markAsDirty()
            }

            button("width:0;height:0") {
              println("width:0;height:0")
              getCanvasElement().apply {
                style.width = "0 px"
                style.height = "0 px"
              }
              markAsDirty()
            }

            button("display:none") {
              println("display:none")
              getCanvasElement().style.display = "none"
              markAsDirty()
            }

            button("visibility:hidden") {
              println("visibility:hidden")
              getCanvasElement().style.visibility = "hidden"
              markAsDirty()
            }
          }
        }
      }
    }

  }
}

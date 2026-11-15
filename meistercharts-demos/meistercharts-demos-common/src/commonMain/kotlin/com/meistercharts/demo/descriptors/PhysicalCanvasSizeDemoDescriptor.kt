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
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.text.addText
import com.meistercharts.color.Color
import com.meistercharts.font.FontDescriptorFragment
import com.meistercharts.canvas.devicePixelRatio
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.VariabilityType
import it.neckar.open.formatting.decimalFormat
import com.meistercharts.style.BoxStyle
import it.neckar.open.kotlin.lang.asProvider

class PhysicalCanvasSizeDemoDescriptor : MeisterchartsDemoDescriptor<Nothing> {
  override val uuid: Uuid = Uuid.parse("e003a44d-f2a6-43bf-ad68-544d600b8301")
  override val name: String = "Physical canvas size demo"
  override val category: DemoCategory = DemoCategory.Calculations
  override val quality: DemoQuality = DemoQuality.High
  override val variabilityType: VariabilityType = VariabilityType.Stable

  override fun prepareDemo(configuration: PredefinedConfiguration<Nothing>?): MeisterchartsDemo {
    return MeisterchartsDemo(this) {
      meistercharts {
        configure {
          layers.addClearBackground()

          layers.addText({ _, _ ->
            listOf(
              "devicePixelRatio: ${chartSupport.devicePixelRatio}",
              "Physical size: ${decimalFormat.format(chartSupport.canvas.physicalWidth)} / ${decimalFormat.format(chartSupport.canvas.physicalHeight)}",
              "Logical size: ${decimalFormat.format(chartSupport.canvas.width)} / ${decimalFormat.format(chartSupport.canvas.height)}"
            )
          }) {
            boxStyle = BoxStyle(fill = Color.rgba(255, 255, 255, 0.9).asProvider(), borderColor = Color.gray)
            font = FontDescriptorFragment.DefaultSize.asProvider()
          }
        }
      }
    }
  }
}

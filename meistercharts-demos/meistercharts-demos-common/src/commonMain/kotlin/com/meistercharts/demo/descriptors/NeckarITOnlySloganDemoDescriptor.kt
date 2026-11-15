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
import it.neckar.geometry.AxisOrientationY
import com.meistercharts.algorithms.layers.FillBackgroundLayer
import com.meistercharts.algorithms.layers.SloganLayer
import com.meistercharts.canvas.themeSupport
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.VariabilityType
import com.meistercharts.demo.configurableDouble
import com.meistercharts.design.NeckarITTheme

/**
 *
 */
class NeckarITOnlySloganDemoDescriptor : MeisterchartsDemoDescriptor<String?> {
  override val uuid: Uuid = Uuid.parse("c52b636d-95a1-4d42-8609-c230bc07cc45")
  override val name: String = "Neckar IT only Slogan"
  override val category: DemoCategory = DemoCategory.NeckarIT
  override val quality: DemoQuality = DemoQuality.High
  override val variabilityType: VariabilityType = VariabilityType.Stable

  override val predefinedConfigurations: List<PredefinedConfiguration<String?>> = listOf(
    PredefinedConfiguration(Uuid.parse("5e9f5f80-4f7f-4866-9049-c364e6b14300"), payload = null),
    PredefinedConfiguration(Uuid.parse("814e16b8-42c5-4e10-82dc-09aa73877a2a"), "Sparkling Charts!", "Sparkling Charts!"),
    PredefinedConfiguration(Uuid.parse("5c93e195-c8b1-4260-939f-aa1f34088b54"), "Ferner gibt es auch niemanden, der den Schmerz um seiner selbst willen liebt, der ihn sucht und haben will, einfach, weil es Schmerz ist", "Long Text"),
  )

  override fun prepareDemo(configuration: PredefinedConfiguration<String?>?): MeisterchartsDemo {
    return MeisterchartsDemo(this) {
      meistercharts {
        configure {
          chartSupport.themeSupport.selectedTheme = NeckarITTheme()
          chartSupport.rootChartState.axisOrientationY = AxisOrientationY.OriginAtBottom
          layers.addLayer(FillBackgroundLayer() {
            secondary()
          })

          val data: SloganLayer.Configuration = configuration?.payload?.let {
            SloganLayer.Configuration { _, _ -> it }
          } ?: SloganLayer.Configuration()

          val layer = SloganLayer(data)
          layers.addLayer(layer)

          configurable("Max Percentage", layer.configuration::maxPercentage)
        }
      }
    }
  }
}

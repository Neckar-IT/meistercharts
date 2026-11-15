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
import com.meistercharts.algorithms.layers.text.addTexts
import com.meistercharts.canvas.i18nConfiguration
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.VariabilityType
import com.meistercharts.environment
import it.neckar.open.formatting.decimalFormat
import it.neckar.open.i18n.SystemLocale
import it.neckar.open.i18n.SystemTimeZone

class EnvironmentLayerDemoDescriptor : MeisterchartsDemoDescriptor<Nothing> {
  override val uuid: Uuid = Uuid.parse("a3df5b52-ee35-44fc-ab04-d8f0a5732f72")
  override val name: String = "Environment"
  override val quality: DemoQuality = DemoQuality.High
  override val variabilityType: VariabilityType = VariabilityType.Stable

  //language=HTML
  override val description: String = "Debug info about the environment"
  override val category: DemoCategory = DemoCategory.LowLevelTests

  override fun prepareDemo(configuration: PredefinedConfiguration<Nothing>?): MeisterchartsDemo {
    return MeisterchartsDemo(this) {
      meistercharts {
        configure {
          layers.addClearBackground()

          layers.addTexts { _, _ ->
            val size = chartSupport.canvas.size
            listOf(
              "multiTouchSupported: ${environment.multiTouchSupported}",
              "devicePixelRatio: ${environment.devicePixelRatio}",
              "Canvas size: ${decimalFormat.format(size.width, chartSupport.i18nConfiguration)} / ${decimalFormat.format(size.height, chartSupport.i18nConfiguration)}",
              "System Locale $SystemLocale",
              "System TimeZone $SystemTimeZone",
            )
          }
        }
      }
    }
  }
}

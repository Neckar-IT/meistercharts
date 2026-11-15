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
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.VariabilityType
import it.neckar.open.formatting.decimalFormat2digits
import kotlinx.browser.window

/**
 */
class MemoryUsageDemoDescriptor : MeisterchartsDemoDescriptor<Nothing> {
  override val uuid: Uuid = Uuid.parse("8a625f3b-eea5-45a4-b9bd-8bdb8861d687")
  override val name: String = "JS Memory Usage"
  override val category: DemoCategory = DemoCategory.Platform
  override val quality: DemoQuality = DemoQuality.High
  override val variabilityType: VariabilityType = VariabilityType.Stable

  override fun prepareDemo(configuration: PredefinedConfiguration<Nothing>?): MeisterchartsDemo {
    return MeisterchartsDemo(this) {
      meistercharts {
        configure {
          layers.addClearBackground()

          layers.addTexts { _, _ ->
            val performance = window.performance

            try {
              val memory = js("window.performance.memory.usedJSHeapSize") //in bytes

              return@addTexts listOf(
                "Memory Usage",
                "used JS heap: ${decimalFormat2digits.format(memory / 1024 / 1024.0)} MiB"
              )

            } catch (e: Throwable) {
              println("e: $e")
              println("class: ${e::class}")
              return@addTexts listOf(
                "Not supported by browser",
                e.message.toString()
              )
            }

          }
        }
      }
    }
  }
}

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
import com.meistercharts.font.FontDescriptorFragment
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.VariabilityType
import it.neckar.open.kotlin.lang.asProvider
import it.neckar.open.unit.si.ms
import kotlinx.browser.window
import kotlin.reflect.KProperty0

/**
 */
class TimingJSDemoDescriptor : MeisterchartsDemoDescriptor<Nothing> {
  override val uuid: Uuid = Uuid.parse("0670c943-cc74-4615-8397-f75ab7494da9")
  override val name: String = "JS Timing"
  override val category: DemoCategory = DemoCategory.Platform
  override val quality: DemoQuality = DemoQuality.High
  override val variabilityType: VariabilityType = VariabilityType.Unstable

  override fun prepareDemo(configuration: PredefinedConfiguration<Nothing>?): MeisterchartsDemo {
    return MeisterchartsDemo(this) {
      meistercharts {
        configure {
          layers.addClearBackground()

          layers.addTexts { _, _ ->
            val performance = window.performance
            val timing = performance.timing

            @ms val base = timing.navigationStart.toDouble()

            listOf(
              createLine(timing::navigationStart, base),
              createLine(timing::redirectStart, base),
              createLine(timing::redirectEnd, base),
              createLine(timing::unloadEventStart, base),
              createLine(timing::unloadEventEnd, base),
              createLine(timing::fetchStart, base),
              createLine(timing::domainLookupStart, base),
              createLine(timing::domainLookupEnd, base),
              createLine(timing::connectStart, base),
              createLine(timing::secureConnectionStart, base),
              createLine(timing::connectEnd, base),
              createLine(timing::requestStart, base),
              createLine(timing::responseStart, base),
              createLine(timing::responseEnd, base),
              createLine(timing::domLoading, base),
              createLine(timing::domInteractive, base),
              createLine(timing::domContentLoadedEventStart, base),
              createLine(timing::domContentLoadedEventEnd, base),
              createLine(timing::domComplete, base),
              createLine(timing::loadEventStart, base),
              createLine(timing::loadEventEnd, base)
            )
          }.also {
            it.configuration.font = FontDescriptorFragment.S.asProvider()
          }
        }
      }
    }
  }

  /**
   * Creates a loading line
   */
  private fun createLine(property: KProperty0<Number>, base: Double): String {
    val value = property.get().toDouble()
    val delta = value - base

    return "${property.name}: after $delta ms"
  }
}

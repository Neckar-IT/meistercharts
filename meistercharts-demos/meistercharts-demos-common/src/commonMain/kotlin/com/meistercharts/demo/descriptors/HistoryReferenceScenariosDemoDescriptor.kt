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
import com.meistercharts.canvas.MeisterchartBuilder
import com.meistercharts.canvas.translateOverTime
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.VariabilityType

/**
 * A simple hello world demo.
 *
 * Can be used as template to create new demos
 */
class HistoryReferenceScenariosDemoDescriptor : MeisterchartsDemoDescriptor<MeisterchartBuilder.() -> Unit> {
  override val uuid: Uuid = Uuid.parse("7ca8be4f-5cbb-4a0e-8415-13e34d918b58")
  override val name: String = "History Reference Scenarios"

  override val category: DemoCategory = DemoCategory.ShowCase
  override val quality: DemoQuality = DemoQuality.High
  override val variabilityType: VariabilityType = VariabilityType.Stable

  override val predefinedConfigurations: List<PredefinedConfiguration<MeisterchartBuilder.() -> Unit>> = listOf(
    PredefinedConfiguration(Uuid.parse("fc889af2-70f2-4051-af39-b90e91a12635"), {
      val ciBuilds = HistoryReferenceScenarios.CiBuilds()
      val gestalt = ciBuilds.createTimelineChartGestalt()
      gestalt.onDispose(ciBuilds)

      gestalt.configure(this)

      configure {
        chartSupport.translateOverTime.animated = true
      }
    }, "Timeline Chart"),

    PredefinedConfiguration(Uuid.parse("5d9d4add-cbbf-417a-acfb-170d6cc81b9a"), {
      val ciBuilds = HistoryReferenceScenarios.CiBuilds()
      val gestalt = ciBuilds.createDiscreteTimelineChartGestalt()
      gestalt.onDispose(ciBuilds)

      gestalt.configure(this)

      configure {
        chartSupport.translateOverTime.animated = true
      }
    }, "Ref Entries Chart"),
  )

  override fun prepareDemo(configuration: PredefinedConfiguration<MeisterchartBuilder.() -> Unit>?): MeisterchartsDemo {
    requireNotNull(configuration) { "HistoryReferenceScenariosDemoDescriptor.prepareDemo requires a predefined configuration" }

    val config = configuration.payload

    return MeisterchartsDemo(this) {
      meistercharts {
        config()
      }
    }
  }
}

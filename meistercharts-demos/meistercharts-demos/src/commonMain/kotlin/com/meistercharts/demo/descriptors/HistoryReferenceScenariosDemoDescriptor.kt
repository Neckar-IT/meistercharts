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

import com.meistercharts.canvas.MeisterChartBuilder
import com.meistercharts.canvas.translateOverTime
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration

/**
 * A simple hello world demo.
 *
 * Can be used as template to create new demos
 */
class HistoryReferenceScenariosDemoDescriptor : ChartingDemoDescriptor<MeisterChartBuilder.() -> Unit> {
  override val name: String = "History Reference Scenarios"

  override val category: DemoCategory = DemoCategory.ShowCase

  override val predefinedConfigurations: List<PredefinedConfiguration<MeisterChartBuilder.() -> Unit>> = listOf(
    PredefinedConfiguration({
      val ciBuilds = HistoryReferenceScenarios.CiBuilds()
      val gestalt = ciBuilds.createTimelineChartGestalt(chartId)
      gestalt.onDispose(ciBuilds)

      gestalt.configure(this)

      configure {
        chartSupport.translateOverTime.animated = true
      }
    }, "Timeline Chart"),

    PredefinedConfiguration({
      val ciBuilds = HistoryReferenceScenarios.CiBuilds()
      val gestalt = ciBuilds.createDiscreteTimelineChartGestalt()
      gestalt.onDispose(ciBuilds)

      gestalt.configure(this)

      configure {
        chartSupport.translateOverTime.animated = true
      }
    }, "Ref Entries Chart"),
  )

  override fun createDemo(configuration: PredefinedConfiguration<MeisterChartBuilder.() -> Unit>?): ChartingDemo {
    requireNotNull(configuration)

    val config = configuration.payload

    return ChartingDemo {
      meistercharts {
        config()
      }
    }
  }
}

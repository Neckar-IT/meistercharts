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

import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.text.addText
import com.meistercharts.canvas.MeisterChartsPlatformState
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration


class PlatformLifecycleDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Platform Lifecycle"
  override val description: String = "Shows the active instances of MeisterCharts as registered at MeisterChartsPlatformState"
  override val category: DemoCategory = DemoCategory.LowLevelTests

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {
        configure {
          layers.addClearBackground()
          layers.addText({ _, _ ->
            buildList {
              val activeInstances = MeisterChartsPlatformState.activeInstances()
              add("${activeInstances.size} MeisterCharts Instances:")
              addAll(activeInstances.map { "\t${it.description}" })
            }
          })
        }
      }
    }
  }
}

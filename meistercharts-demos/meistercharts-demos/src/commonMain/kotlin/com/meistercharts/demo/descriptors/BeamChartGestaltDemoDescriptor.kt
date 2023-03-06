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

import com.meistercharts.algorithms.layers.text.addText
import com.meistercharts.charts.sick.beams.BeamChartGestalt
import com.meistercharts.charts.sick.beams.ExampleBeamProvider
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableBoolean
import com.meistercharts.demo.configurableEnum
import com.meistercharts.demo.configurableInt
import com.meistercharts.model.VerticalAlignment

/**
 */
class BeamChartGestaltDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Beam Chart Gestalt"
  override val category: DemoCategory = DemoCategory.Automation

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {
        val gestalt = BeamChartGestalt()
        gestalt.configure(this)


        configure {
          var lastClickedBeam = -1

          gestalt.beamsLayer.onBeamClicked { modelBeamIndex, _ ->
            lastClickedBeam = modelBeamIndex
            markAsDirty()
          }

          layers.addText { _, _ ->
            if (lastClickedBeam >= 0) {
              listOf("Last Clicked beam: $lastClickedBeam")
            } else {
              listOf()
            }
          }
        }


        configurableBoolean("Zones visible", gestalt.style::zonesLayerVisible)

        configurableInt("Beams Count", (gestalt.data.beamProvider as ExampleBeamProvider)::count) {
          max = 240
        }

        configurableEnum("Connector Location", gestalt.beamsLayer.style::connectorLocation, arrayOf(VerticalAlignment.Bottom, VerticalAlignment.Top))
      }
    }
  }
}

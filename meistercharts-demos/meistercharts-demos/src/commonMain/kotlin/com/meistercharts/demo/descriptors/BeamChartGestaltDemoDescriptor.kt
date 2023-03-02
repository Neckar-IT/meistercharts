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

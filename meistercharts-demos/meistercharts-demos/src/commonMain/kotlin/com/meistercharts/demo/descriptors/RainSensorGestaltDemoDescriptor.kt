package com.meistercharts.demo.descriptors

import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableDouble
import it.neckar.charting.custom.rainsensor.RainSensorGestalt
import com.meistercharts.custom.rainsensor.WindowAction

/**
 *
 */
class RainSensorGestaltDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Rain Sensor"
  override val category: DemoCategory = DemoCategory.NeckarIT

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {

    return ChartingDemo {
      meistercharts {
        val rainSensorGestalt = RainSensorGestalt()
        rainSensorGestalt.configure(this)

        configurableDouble("Window Open Angle °", rainSensorGestalt.data.model.openAngleProperty) {
          min = -30.0
          max = 0.0
        }

        configure {
          declare {
            button("Close") {
              rainSensorGestalt.data.model.nextAction = WindowAction.Close
            }

            button("Open") {
              rainSensorGestalt.data.model.nextAction = WindowAction.Open
            }
          }
        }
      }
    }
  }
}

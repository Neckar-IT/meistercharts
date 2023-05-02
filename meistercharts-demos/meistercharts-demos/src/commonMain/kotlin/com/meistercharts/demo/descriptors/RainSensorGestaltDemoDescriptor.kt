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

import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableDouble
import com.meistercharts.custom.rainsensor.RainSensorGestalt
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

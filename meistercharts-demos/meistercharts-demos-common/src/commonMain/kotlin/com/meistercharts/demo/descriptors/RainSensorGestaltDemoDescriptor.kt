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
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableDouble
import com.meistercharts.custom.rainsensor.RainSensorGestalt
import com.meistercharts.custom.rainsensor.WindowAction
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.VariabilityType

/**
 *
 */
class RainSensorGestaltDemoDescriptor : MeisterchartsDemoDescriptor<Nothing> {
  override val uuid: Uuid = Uuid.parse("aebd68cb-2013-475d-8bf5-fe052bf1ed1f")
  override val name: String = "Rain Sensor"
  override val category: DemoCategory = DemoCategory.NeckarIT
  override val quality: DemoQuality = DemoQuality.High
  override val variabilityType: VariabilityType = VariabilityType.Stable

  override fun prepareDemo(configuration: PredefinedConfiguration<Nothing>?): MeisterchartsDemo {

    return MeisterchartsDemo(this) {
      meistercharts {
        val rainSensorGestalt = RainSensorGestalt()
        rainSensorGestalt.configure(this)

        configurableDouble("Window Open Angle °", rainSensorGestalt.configuration.model.openAngleProperty) {
          min = -30.0
          max = 0.0
        }

        configure {
          declare {
            button("Close") {
              rainSensorGestalt.configuration.model.nextAction = WindowAction.Close
            }

            button("Open") {
              rainSensorGestalt.configuration.model.nextAction = WindowAction.Open
            }
          }
        }
      }
    }
  }
}

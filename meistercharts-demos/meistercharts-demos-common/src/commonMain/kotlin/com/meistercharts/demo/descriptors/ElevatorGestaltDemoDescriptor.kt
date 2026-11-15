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

import com.meistercharts.demo.configurable
import com.meistercharts.demo.section
import kotlin.uuid.Uuid
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.VariabilityType
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.configurableInt
import com.meistercharts.demo.elevator.gestalt.ElevatorGestalt
import it.neckar.open.kotlin.lang.fastFor

/**
 * Playground for the elevator.
 *
 * The content area has a fixed with of 1000.0
 * The height is 200 per floor
 *
 */
class ElevatorGestaltDemoDescriptor : MeisterchartsDemoDescriptor<Nothing> {
  override val uuid: Uuid = Uuid.parse("7797fb78-6140-41c8-b8d5-c5e13cd3f6e8")
  override val name: String = "Elevator"
  override val quality: DemoQuality = DemoQuality.High
  override val variabilityType: VariabilityType = VariabilityType.Stable

  override val description: String = "Simulates an elevator serving manually selected floors in order."

  override val category: DemoCategory = DemoCategory.Gestalt

  override fun prepareDemo(configuration: PredefinedConfiguration<Nothing>?): MeisterchartsDemo {
    return MeisterchartsDemo(this) {

      val elevatorGestalt = ElevatorGestalt()

      meistercharts {
        elevatorGestalt.configure(this)

        val model = elevatorGestalt.model

        configurable("Number of elevators", model::numberOfElevators) {
          min = 1
        }
        configurable("Number of waiting rooms", model::numberOfWaitingRooms)

        configurable("Elevator Location", model::elevatorLocation) {
          max = model.floorRange.numberOfFloors.toDouble() - 1.0
        }

        configure {
          declare {
            section("Elevator Panel")

            model.floorRange.numberOfFloors.fastFor { targetFloor ->
              button("$targetFloor") {
                elevatorGestalt.elevatorAnimationManager.pressedFloorButton(targetFloor)
              }
            }
          }
        }
      }
    }
  }
}


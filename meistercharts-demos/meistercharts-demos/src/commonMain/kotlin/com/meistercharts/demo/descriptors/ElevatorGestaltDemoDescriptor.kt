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
class ElevatorGestaltDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Elevator"

  //language=HTML
  override val description: String = """
      <h3>Elevator</h3>
      <p>Aktuell kann man die Stockwerke manuell auswählen.</p>
      <p>Die Stockwerke werden der Reihe nach abgefahren - jeweils in derselben Richtung.</p>
  """.trimIndent()

  override val category: DemoCategory = DemoCategory.Gestalt

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {

      val elevatorGestalt = ElevatorGestalt()

      meistercharts {
        elevatorGestalt.configure(this)

        val model = elevatorGestalt.model

        configurableInt("Number of elevators", model::numberOfElevators) {
          min = 1
        }
        configurableInt("Number of waiting rooms", model::numberOfWaitingRooms)

        configurableDouble("Elevator Location", model::elevatorLocation) {
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


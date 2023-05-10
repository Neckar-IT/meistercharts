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
package com.meistercharts.custom.rainsensor

import it.neckar.open.observable.ObservableDouble
import it.neckar.open.observable.ObservableObject
import it.neckar.open.unit.other.deg

/**
 *
 */
class RainSensorModel {
  /**
   * The open angle of the window.
   * Should be between 0.0 (closed) and -30° (wide open)
   */
  val openAngleProperty: @deg ObservableDouble = ObservableDouble(WindowAction.Open.targetAngle)

  /**
   * The open angle of the window
   */
  var openAngle: @deg Double by openAngleProperty

  /**
   * The next window action that is scheduled
   */
  var nextAction: WindowAction? = null
    set(value) {
      field = value
      if (value != null) {
        currentAction = value
      }
    }

  /**
   * The current action that is active.
   * This property is used when recording the model for the category line chart
   */
  var currentAction: WindowAction = WindowAction.Open

  val weatherProperty: ObservableObject<Weather> = ObservableObject(Weather.Sunny)
  var weather: Weather by weatherProperty

}

enum class WindowAction(val targetAngle: @deg Double) {
  Open(-30.0),
  Close(0.0);
}

/**
 * The current weather
 */
enum class Weather {
  Sunny,
  Rain,
  Snow

}

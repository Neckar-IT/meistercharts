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

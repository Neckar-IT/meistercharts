package com.meistercharts.demo

import com.meistercharts.algorithms.painter.Color
import it.neckar.open.observable.ObservableObject

/**
 * Provides a way to define the demo configuration UI
 */
interface DemoConfiguration {

  /**
   * Adds a section
   */
  fun section(name: String, configure: Section.() -> Unit = {})

  /**
   * Adds a combo box for an enum property
   */
  fun <T : Enum<T>> comboBox(
    name: String,
    property: ObservableObject<T>,
    possibleValues: Array<T>
  )

  fun <T> comboBox(
    name: String,
    valueProperty: ObservableObject<T>,
    possibleValues: List<T>,
    converter: (T) -> String,
  )

  /**
   * Creates a slider - for doubles
   */
  fun slider(
    name: String,
    valueProperty: ObservableObject<Double>,
    min: Double,
    max: Double,
    step: Double?,
  )

  fun sliderNan(
    name: String,
    valueProperty: ObservableObject<Double>,
    isNanProperty: ObservableObject<Boolean>,
    min: Double,
    max: Double,
    step: Double?,
  )

  /**
   * Creates a slider - for ints
   */
  fun slider(
    name: String,
    valueProperty: ObservableObject<Int>,
    min: Int,
    max: Int,
    step: Int?,
  )

  /**
   * Creates a check box
   */
  fun checkBox(
    name: String,
    property: ObservableObject<Boolean>
  )

  /**
   * Creates a button
   */
  fun button(
    name: String,
    action: () -> Unit
  )

  /**
   * Creates a color picker
   */
  fun colorPicker(
    name: String,
    valueProperty: ObservableObject<Color>,
    customColors: List<Color>,
  )
}

interface Section



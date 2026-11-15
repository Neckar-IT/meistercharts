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
package com.meistercharts.demo

import com.meistercharts.color.Color
import com.meistercharts.color.ColorProvider
import it.neckar.open.observable.ObservableObject
import it.neckar.open.observable.ObservableString

/**
 * Provides a way to define the demo configuration UI
 */
interface DemoConfiguration {

  /**
   * Adds a section header
   */
  fun section(name: String)

  /**
   * Adds a combo box for an enum property
   */
  fun <T : Enum<T>> comboBox(
    name: String,
    property: ObservableObject<T>,
    possibleValues: List<T>,
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
   * Creates a checkbox
   */
  fun checkBox(
    name: String,
    property: ObservableObject<Boolean>,
  )

  /**
   * Creates a text input
   */
  fun textInput(
    name: String,
    property: ObservableString,
  )

  /**
   * Creates a button
   */
  fun button(
    name: String,
    action: () -> Unit,
  ): DemoButton

  /**
   * Creates a toggle button
   */
  fun toggleButton(
    name: String,
    onToggle: (button: DemoToggleButton, selected: Boolean) -> Unit,
  ): DemoToggleButton

  fun colorPicker(
    name: String,
    valueProperty: ObservableObject<Color>,
    customColors: List<ColorProvider>,
  )
}



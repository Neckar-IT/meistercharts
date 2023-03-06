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
package com.meistercharts.fx

import javafx.scene.control.CheckBox as FxCheckBox
import com.meistercharts.algorithms.environment
import com.meistercharts.canvas.components.CheckBox
import com.meistercharts.canvas.components.Choice
import com.meistercharts.canvas.components.ComboBox
import com.meistercharts.canvas.components.NativeComponent
import com.meistercharts.canvas.components.NativeComponentsHandler
import com.meistercharts.canvas.components.NativeComponentsSupport
import com.meistercharts.canvas.components.TextInput
import com.meistercharts.fx.binding.toJavaFx
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control.ChoiceBox
import javafx.scene.control.TextField
import javafx.scene.layout.StackPane
import javafx.util.StringConverter

/**
 * Supports the native components for JavaFX
 */
class NativeComponentsFX(
  nativeComponentsSupport: NativeComponentsSupport
) : StackPane() {
  init {
    //Ensure the mouse events are handled correctly. Only clicks on the elements are processed. All others are passed through to underlying nodes
    isPickOnBounds = false

    //Create the native text input elements
    nativeComponentsSupport.onComponent(object : NativeComponentsHandler {
      override fun textInput(textInput: TextInput) {
        registerPlattformComponent(textInput, TextField()) {
          //Bind the text
          it.textProperty().bindBidirectional(textInput.textProperty.toJavaFx())
        }
      }

      override fun checkBox(checkBox: CheckBox) {
        registerPlattformComponent(checkBox, FxCheckBox()) {
          //Bind the selected state
          it.selectedProperty().bindBidirectional(checkBox.selectedProperty.toJavaFx())
        }
      }

      override fun comboBox(comboBox: ComboBox) {
        registerPlattformComponent(comboBox, ChoiceBox<Choice>()) { choiceBox ->

          //Bind the choices
          comboBox.choicesProperty.consumeImmediately {
            choiceBox.items.clear()
            choiceBox.items.addAll(it)
          }

          //Bind the current value
          choiceBox.valueProperty().bindBidirectional(comboBox.selectedProperty.toJavaFx())

          //Bind the converter
          comboBox.converterProperty.consumeImmediately {
            choiceBox.converter = object : StringConverter<Choice>() {
              override fun toString(value: Choice): String {
                return it(value)
              }

              override fun fromString(string: String?): Choice {
                throw UnsupportedOperationException("Not supported")
              }
            }
          }
        }
      }
    })
  }

  /**
   * Registers a plattform component
   */
  private fun <T : Node> registerPlattformComponent(nativeComponent: NativeComponent, plattformComponent: T, binder: (plattformComponent: T) -> Unit) {
    //Assign the native element as ide
    nativeComponent.id = plattformComponent

    //Update the location
    plattformComponent.bindLocation(nativeComponent)

    children.add(plattformComponent)

    nativeComponent.onDispose {
      children.remove(plattformComponent)
    }

    //Set the alignment
    setAlignment(plattformComponent, Pos.TOP_LEFT)

    binder(plattformComponent)
  }
}

/**
 * Binds the location of the FX component to the location of the native component
 */
private fun Node.bindLocation(nativeComponent: NativeComponent) {
  nativeComponent.locationProperty.consumeImmediately { coordinates ->
    StackPane.setMargin(this, Insets(coordinates.y * environment.devicePixelRatio, 0.0, 0.0, coordinates.x * environment.devicePixelRatio))
  }
}

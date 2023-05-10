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
package com.meistercharts.canvas.components

import it.neckar.open.annotations.Experiment

/**
 * Offers support for native components (e.g. text fields) that are placed *above* the canvas
 *
 *
 */
@Experiment
class NativeComponentsSupport {
  /**
   * All listeners
   */
  private val nativeComponentsHandlers: MutableList<NativeComponentsHandler> = mutableListOf()

  /**
   * Registers a listener that is notified when native component are added
   *
   * The given listener is called for each *existing* element and (later) for all newly added elements.
   */
  fun onComponent(listener: NativeComponentsHandler) {
    nativeComponentsHandlers.add(listener)

    //notify about existing elements
    textInputs.forEach {
      listener.textInput(it)
    }
    checkBoxes.forEach {
      listener.checkBox(it)
    }
    comboBoxes.forEach {
      listener.comboBox(it)
    }
  }

  /**
   * Creates a new text input field
   */
  fun addInput(config: TextInput.() -> Unit): TextInput {
    return TextInput()
      .also(config)
      .also { input ->
        textInputs.add(input)
        input.onDispose {
          textInputs.remove(input)
        }

        nativeComponentsHandlers.forEach {
          it.textInput(input)
        }
      }
  }

  /**
   * Creates a new checkbox
   */
  fun addCheckBox(config: CheckBox.() -> Unit): CheckBox {
    return CheckBox()
      .also(config)
      .also { checkBox ->
        checkBoxes.add(checkBox)
        checkBox.onDispose {
          checkBoxes.remove(checkBox)
        }

        nativeComponentsHandlers.forEach {
          it.checkBox(checkBox)
        }
      }
  }

  /**
   * Creates a new combo box
   */
  fun addComboBox(config: ComboBox.() -> Unit): ComboBox {
    return ComboBox()
      .also(config)
      .also { comboBox ->
        comboBoxes.add(comboBox)
        comboBox.onDispose {
          comboBoxes.remove(comboBox)
        }

        nativeComponentsHandlers.forEach {
          it.comboBox(comboBox)
        }
      }
  }

  private val textInputs: MutableList<TextInput> = mutableListOf()
  private val checkBoxes: MutableList<CheckBox> = mutableListOf()
  private val comboBoxes: MutableList<ComboBox> = mutableListOf()
}

/**
 * Is called for each native component that has been / is created
 */
interface NativeComponentsHandler {
  fun textInput(textInput: TextInput)
  fun checkBox(checkBox: CheckBox)
  fun comboBox(comboBox: ComboBox)
}

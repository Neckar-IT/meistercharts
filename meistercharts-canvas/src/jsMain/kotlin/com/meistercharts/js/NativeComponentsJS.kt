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
package com.meistercharts.js

import com.meistercharts.canvas.ChartSupport
import com.meistercharts.canvas.components.CheckBox
import com.meistercharts.canvas.components.Choice
import com.meistercharts.canvas.components.ComboBox
import com.meistercharts.canvas.components.NativeComponent
import com.meistercharts.canvas.components.NativeComponentsHandler
import com.meistercharts.canvas.components.TextInput
import com.meistercharts.canvas.nativeComponentsSupport
import it.neckar.open.observable.ObservableBoolean
import it.neckar.open.observable.ObservableObject
import kotlinx.browser.document
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.HTMLOptionElement
import org.w3c.dom.HTMLSelectElement
import org.w3c.dom.get

/**
 * Supports the native components for HTML/JS
 */
class NativeComponentsJS(chartSupport: ChartSupport) {
  /**
   * The div that holds all elements
   */
  val div: HTMLDivElement = (document.createElement("DIV") as HTMLDivElement).also { div ->
    //Set the style to 100%
    div.style.setProperty("width", "100%")
    div.style.setProperty("height", "100%")
    div.style.setProperty("position", "absolute")
    div.style.setProperty("top", "0")
    div.style.setProperty("left", "0")

    //Mouse events are not consumed. Children have to get the property set to "auto"
    div.style.setProperty("pointer-events", "none")
  }

  init {
    chartSupport.nativeComponentsSupport.onComponent(object : NativeComponentsHandler {
      override fun textInput(textInput: TextInput) {
        registerPlattformComponent(textInput, document.createElement("input") as HTMLInputElement) {
          //Configure as text
          it.setAttribute("type", "text")

          //Bind the text
          it.bindTextBidirectional(textInput.textProperty)
        }
      }

      override fun checkBox(checkBox: CheckBox) {
        registerPlattformComponent(checkBox, document.createElement("input") as HTMLInputElement) {
          //Configure as text
          it.setAttribute("type", "checkbox")

          //Bind the text
          it.bindSelectedBidirectional(checkBox.selectedProperty)
        }
      }

      override fun comboBox(comboBox: ComboBox) {
        registerPlattformComponent(comboBox, document.createElement("select") as HTMLSelectElement) {
          //Configure as text
          //it.setAttribute("type", "checkbox")

          comboBox.choicesProperty
            .consumeImmediately { choices ->
              val options = it.options

              //clear first
              while (options.length > 0) {
                options[0]?.removeFromParent()
              }

              //TODO find better implementation that avoid many reflows. see https://developers.google.com/speed/docs/insights/browser-reflow
              choices.forEach { choice ->
                val optionElement = document.createElement("option") as HTMLOptionElement
                optionElement.text = comboBox.converter(choice)
                options.add(optionElement)
              }
            }

          //Bind the selection
          it.bindChoiceBidirectional(comboBox.selectedProperty) {
            comboBox.choices
          }
        }
      }
    })
  }

  /**
   * Registers a plattform component
   */
  private fun <T : HTMLElement> registerPlattformComponent(nativeComponent: NativeComponent, plattformComponent: T, binder: (plattformComponent: T) -> Unit) {
    //Assign the native element as ide
    nativeComponent.id = plattformComponent

    //Ensure events are processed by this elements
    plattformComponent.style.setProperty("pointer-events", "auto")

    //Update the location
    plattformComponent.style.setProperty("position", "absolute")
    plattformComponent.bindLocation(nativeComponent)

    //Add the element
    div.appendChild(plattformComponent)

    nativeComponent.onDispose {
      //Remove if disposed
      plattformComponent.removeFromParent()
    }

    binder(plattformComponent)
  }
}

/**
 * Binds the location of the FX component to the location of the native component
 */
private fun HTMLElement.bindLocation(nativeComponent: NativeComponent) {
  nativeComponent.locationProperty.consumeImmediately { coordinates ->
    this.style.setProperty("left", "${coordinates.x}px")
    this.style.setProperty("top", "${coordinates.y}px")
  }
}

/**
 * Binds the text bidirectional.
 * Copies the value from the given property to this initially
 */
private fun HTMLInputElement.bindTextBidirectional(property: ObservableObject<String>) {
  //On update write the value to the property
  oninput = {
    property.value = value
    Unit
  }

  property.consumeImmediately {
    value = it
  }
}

private fun HTMLInputElement.bindSelectedBidirectional(property: ObservableBoolean) {
  //On update write the value to the property
  oninput = {
    property.value = checked
    Unit
  }

  property.consumeImmediately {
    checked = it
  }
}

private fun HTMLSelectElement.bindChoiceBidirectional(property: ObservableObject<Choice?>, choicesProvider: () -> List<Choice>) {
  oninput = {
    val choice = choicesProvider()[selectedIndex]
    property.value = choice
    Unit
  }

  property.consumeImmediately {
    selectedIndex = choicesProvider().indexOf(it)
  }
}

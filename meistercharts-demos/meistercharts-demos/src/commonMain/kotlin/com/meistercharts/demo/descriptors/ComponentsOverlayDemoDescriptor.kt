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

import it.neckar.open.annotations.Experiment
import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.canvas.components.CheckBox
import com.meistercharts.canvas.components.Choice
import com.meistercharts.canvas.components.TextInput
import com.meistercharts.canvas.paintMark
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableBoolean
import com.meistercharts.demo.configurableDouble
import com.meistercharts.model.Coordinates
import com.meistercharts.model.Direction
import it.neckar.open.observable.ObservableBoolean
import it.neckar.open.observable.ObservableObject
import it.neckar.open.observable.ObservableString

/**
 */
@Experiment
class ComponentsOverlayDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Components overlay"

  //language=HTML
  override val description: String = """<h3>Native components overlay</h3>
    | An overlay with native components that are bound to properties used on the canvas
  """.trimMargin()

  //language=HTML
  override val category: DemoCategory = DemoCategory.Interaction

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {
        configure {

          val modelTextProperty = ObservableString("Hello World").also { it.consume { markAsDirty() } }
          val modelSelectedProperty = ObservableBoolean().also { it.consume { markAsDirty() } }

          val choices = listOf(Choice("a"), Choice("b"), Choice("c"))
          val selectedChoice: ObservableObject<Choice?> = ObservableObject<Choice?>(choices[0]).also { it.consume { markAsDirty() } }

          //Add a text input
          var textInput0: TextInput? = null
          val textInput1: TextInput = chartSupport.canvas.nativeComponentsSupport.addInput {
            textProperty.bindBidirectional(modelTextProperty)
            location = Coordinates(100.0, 120.0)
          }

          val checkBox0: CheckBox = chartSupport.canvas.nativeComponentsSupport.addCheckBox {
            selectedProperty.bindBidirectional(modelSelectedProperty)
          }
          val checkBox1: CheckBox = chartSupport.canvas.nativeComponentsSupport.addCheckBox {
            selectedProperty.bindBidirectional(modelSelectedProperty)
          }

          val comboBox0 = chartSupport.canvas.nativeComponentsSupport.addComboBox {
            this.choices = choices
            this.selectedProperty.bindBidirectional(selectedChoice)
            this.converter
          }
          val comboBox1 = chartSupport.canvas.nativeComponentsSupport.addComboBox {
            this.choices = choices
            this.selectedProperty.bindBidirectional(selectedChoice)
            this.converter
          }

          layers.addClearBackground()
          layers.addLayer(object : AbstractLayer() {
            override val type: LayerType
              get() = LayerType.Content

            override fun paint(paintingContext: LayerPaintingContext) {
              val gc = paintingContext.gc
              gc.stroke(Color.red)

              textInput0?.let {
                gc.paintMark(it.location)
                gc.fillText(it.text, Coordinates.origin, Direction.TopLeft, 10.0)
              }

              gc.paintMark(textInput1.location)
              gc.paintMark(checkBox0.location)
              gc.paintMark(checkBox1.location)
              gc.paintMark(comboBox0.location)
              gc.paintMark(comboBox1.location)

              gc.fillText(modelSelectedProperty.toString(), Coordinates.origin, Direction.TopLeft, 20.0)
              gc.fillText(selectedChoice.toString(), Coordinates.origin, Direction.TopLeft, 40.0)
            }
          })

          configurableBoolean("visible") {
            value = true

            onChange {
              if (it) {
                textInput0 = chartSupport.canvas.nativeComponentsSupport.addInput {
                  textProperty.bindBidirectional(modelTextProperty)
                  location = Coordinates(100.0, 100.0)
                  modelTextProperty.consume {
                    markAsDirty()
                  }
                }
              } else {
                textInput0?.dispose()
                textInput0 = null
              }

              markAsDirty()
            }
          }

          configurableDouble("X", 100.0) {
            max = 500.0
            onChange { newValue ->
              textInput0?.let {
                it.location = it.location.withX(newValue)
              }
              textInput1.location = textInput1.location.withX(newValue)
              checkBox0.location = checkBox0.location.withX(newValue)
              checkBox1.location = checkBox1.location.withX(newValue + 20)
              comboBox0.location = comboBox0.location.withX(newValue)
              comboBox1.location = comboBox1.location.withX(newValue + 100.0)
              markAsDirty()
            }
          }

          configurableDouble("Y", 100.0) {
            max = 500.0
            onChange { newValue ->
              textInput0?.let {
                it.location = it.location.withY(newValue)
              }
              textInput1.location = textInput1.location.withY(newValue + 30.0)
              checkBox0.location = checkBox0.location.withY(newValue + 50.0)
              checkBox1.location = checkBox1.location.withY(newValue + 50.0)
              comboBox0.location = comboBox0.location.withY(newValue + 80.0)
              comboBox1.location = comboBox1.location.withY(newValue + 80.0)
              markAsDirty()
            }
          }
        }
      }
    }
  }
}

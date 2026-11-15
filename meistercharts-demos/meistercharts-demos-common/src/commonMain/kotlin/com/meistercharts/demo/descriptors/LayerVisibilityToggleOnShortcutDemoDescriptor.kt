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


import kotlin.uuid.Uuid
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.text.TextLayer
import com.meistercharts.algorithms.layers.toggleShortcut
import com.meistercharts.algorithms.layers.visibleIf
import com.meistercharts.algorithms.layers.visibleIfWithState
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.VariabilityType
import it.neckar.events.KeyCode
import it.neckar.events.KeyStroke
import it.neckar.events.ModifierCombination
import it.neckar.open.observable.ObservableBoolean

class LayerVisibilityToggleOnShortcutDemoDescriptor : MeisterchartsDemoDescriptor<Nothing> {
  override val uuid: Uuid = Uuid.parse("c438fa70-106b-4d72-9901-4a2aa4bf85f7")
  override val name: String = "Toggle Visibility on shortcut"
  override val description: String = "Toggles the visibility of the text layer with Ctrl-Alt-V"
  override val category: DemoCategory = DemoCategory.Interaction
  override val quality: DemoQuality = DemoQuality.High
  override val variabilityType: VariabilityType = VariabilityType.Stable

  override fun prepareDemo(configuration: PredefinedConfiguration<Nothing>?): MeisterchartsDemo {
    return MeisterchartsDemo(this) {
      meistercharts {
        val textLayerVisible = ObservableBoolean(true)

        configure {
          layers.addClearBackground()
          layers.addLayer(
            TextLayer({ _, _ -> listOf("Press Ctrl-Alt-V to toggle this text") })
              .visibleIfWithState(textLayerVisible)
              .toggleShortcut(KeyStroke(KeyCode('V'), ModifierCombination.CtrlAlt))
          )
        }
      }
    }
  }
}

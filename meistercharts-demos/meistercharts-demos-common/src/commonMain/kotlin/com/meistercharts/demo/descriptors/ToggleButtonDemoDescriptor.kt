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

import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.text.addText
import com.meistercharts.canvas.DirtyReason
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.VariabilityType
import com.meistercharts.demo.section
import it.neckar.open.observable.ObservableBoolean
import kotlin.uuid.Uuid

/**
 */
class ToggleButtonDemoDescriptor : MeisterchartsDemoDescriptor<Nothing> {
  override val uuid: Uuid = Uuid.parse("45e0bccd-997e-47df-b9ab-d1beefaab1f8")
  override val name: String = "Toggle Button"
  override val variabilityType: VariabilityType = VariabilityType.Stable

  //language=HTML
  override val description: String = "How to use a toggle button in the demo configuration"
  override val category: DemoCategory = DemoCategory.Framework
  override val quality: DemoQuality = DemoQuality.High

  override fun prepareDemo(configuration: PredefinedConfiguration<Nothing>?): MeisterchartsDemo {
    return MeisterchartsDemo(this) {
      meistercharts {
        configure {
          val selectedObservable = ObservableBoolean(false)

          layers.addClearBackground()

          layers.addText { _, _ ->
            "Current selected state: ${selectedObservable.get()}"
          }

          declare {
            section("Toggle Button")

            toggleButton("Toggle me") { button, selected ->
              button.text = if (selected) "Selected" else "Not selected"

              selectedObservable.value = selected
              markAsDirty(reason = DirtyReason.UserInteraction)
            }
          }
        }
      }
    }
  }
}
